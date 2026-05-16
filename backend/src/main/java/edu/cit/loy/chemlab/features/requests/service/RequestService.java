package edu.cit.loy.chemlab.features.requests.service;

import edu.cit.loy.chemlab.entity.*;
import edu.cit.loy.chemlab.exception.InventoryApiException;
import edu.cit.loy.chemlab.features.requests.dto.RequestActionDTO;
import edu.cit.loy.chemlab.features.requests.dto.RequestCreateDTO;
import edu.cit.loy.chemlab.features.requests.dto.RequestResponse;
import edu.cit.loy.chemlab.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RequestService {

    private final ItemRequestRepository requestRepository;
    private final RequestItemRepository requestItemRepository;
    private final TransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public RequestService(ItemRequestRepository requestRepository,
                          RequestItemRepository requestItemRepository,
                          TransactionRepository transactionRepository,
                          InventoryItemRepository inventoryItemRepository,
                          UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.requestItemRepository = requestItemRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RequestResponse createRequest(RequestCreateDTO dto, String actorEmail) {
        User requester = getUser(actorEmail);

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "At least one item is required.");
        }

        ItemRequest request = new ItemRequest();
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        request.setRemarks(dto.getRemarks());
        ItemRequest savedRequest = requestRepository.save(request);

        List<RequestItem> requestItems = new ArrayList<>();
        for (RequestCreateDTO.RequestItemInput input : dto.getItems()) {
            if (input.getInventoryItemId() == null) {
                throw new InventoryApiException("VALIDATION_ERROR", 400, "inventoryItemId is required for each item.");
            }
            if (input.getQuantity() <= 0) {
                throw new InventoryApiException("VALIDATION_ERROR", 400, "Quantity must be positive.");
            }

            InventoryItem invItem = inventoryItemRepository.findById(input.getInventoryItemId())
                    .orElseThrow(() -> new InventoryApiException("ITEM_NOT_FOUND", 404,
                            "Inventory item " + input.getInventoryItemId() + " not found."));

            RequestItem ri = new RequestItem();
            ri.setRequest(savedRequest);
            ri.setInventoryItem(invItem);
            ri.setQuantity(input.getQuantity());
            ri.setUnitSnapshot(invItem.getUnit());
            ri.setExpirationSnapshot(invItem.getExpiryDate());
            requestItems.add(requestItemRepository.save(ri));
        }

        savedRequest.setItems(requestItems);
        return toResponse(savedRequest);
    }

    public Page<RequestResponse> listRequests(RequestStatus status, String actorEmail, Pageable pageable) {
        User actor = getUser(actorEmail);

        // Students can only see their own requests
        if (actor.getRole() == User.Role.STUDENT) {
            return requestRepository.findByRequesterIdOrderByCreatedAtDesc(actor.getId(), pageable)
                    .map(this::toResponse);
        }

        // Technician/Admin see all, optionally filtered by status
        return requestRepository.findFiltered(status, null, pageable)
                .map(this::toResponse);
    }

    public RequestResponse getRequestById(Long requestId, String actorEmail) {
        User actor = getUser(actorEmail);
        ItemRequest request = getRequest(requestId);

        // Students can only see their own
        if (actor.getRole() == User.Role.STUDENT && !request.getRequester().getId().equals(actor.getId())) {
            throw new InventoryApiException("FORBIDDEN", 403, "You can only view your own requests.");
        }

        return toResponse(request);
    }

    /**
     * Approve: PENDING → APPROVED → auto-deduct inventory → write ledger → auto-RELEASED → auto-COMPLETED
     */
    @Transactional
    public RequestResponse approveRequest(Long requestId, RequestActionDTO dto, String actorEmail) {
        User actor = getUser(actorEmail);
        requireTechnicianOrAdmin(actor);

        ItemRequest request = getRequest(requestId);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InventoryApiException("INVALID_STATE", 400,
                    "Only PENDING requests can be approved. Current status: " + request.getStatus());
        }

        // Validate stock availability for all items first
        List<RequestItem> items = requestItemRepository.findByRequestIdOrderByIdAsc(requestId);
        for (RequestItem ri : items) {
            InventoryItem invItem = ri.getInventoryItem();
            BigDecimal requestedQty = BigDecimal.valueOf(ri.getQuantity());
            if (invItem.getQuantity().compareTo(requestedQty) < 0) {
                throw new InventoryApiException("INSUFFICIENT_STOCK", 400,
                        "Insufficient stock for " + invItem.getItemName() +
                        ". Available: " + invItem.getQuantity() + ", Requested: " + ri.getQuantity());
            }
        }

        // Deduct inventory and write transaction ledger
        for (RequestItem ri : items) {
            InventoryItem invItem = ri.getInventoryItem();
            BigDecimal requestedQty = BigDecimal.valueOf(ri.getQuantity());
            BigDecimal newBalance = invItem.getQuantity().subtract(requestedQty);

            invItem.setQuantity(newBalance);
            invItem.setUpdatedBy(actor.getId());

            // Auto-update stock status
            if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
                invItem.setStatus(InventoryStatus.OUT_OF_STOCK);
            } else if (invItem.getMinThreshold() != null && newBalance.compareTo(invItem.getMinThreshold()) <= 0) {
                invItem.setStatus(InventoryStatus.LOW_STOCK);
            }

            inventoryItemRepository.save(invItem);

            // Write immutable transaction ledger entry
            Transaction tx = new Transaction();
            tx.setRequest(request);
            tx.setInventoryItem(invItem);
            tx.setChangeType("REQUEST_DEDUCTION");
            tx.setQuantityChange(-ri.getQuantity());
            tx.setBalanceAfter(newBalance.intValue());
            tx.setActor(actor);
            tx.setRemarks("Auto-deducted on request #" + requestId + " approval");
            transactionRepository.save(tx);
        }

        // Transition: PENDING → APPROVED → RELEASED → COMPLETED (automatic)
        String combinedRemarks = buildRemarks(request.getRemarks(), dto != null ? dto.getRemarks() : null,
                "Approved by " + actor.getUsername());
        request.setRemarks(combinedRemarks);
        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);

        // Auto-transition to RELEASED
        request.setStatus(RequestStatus.RELEASED);
        requestRepository.save(request);

        // Auto-transition to COMPLETED
        request.setStatus(RequestStatus.COMPLETED);
        requestRepository.save(request);

        return toResponse(request);
    }

    @Transactional
    public RequestResponse rejectRequest(Long requestId, RequestActionDTO dto, String actorEmail) {
        User actor = getUser(actorEmail);
        requireTechnicianOrAdmin(actor);

        ItemRequest request = getRequest(requestId);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InventoryApiException("INVALID_STATE", 400,
                    "Only PENDING requests can be rejected. Current status: " + request.getStatus());
        }

        String combinedRemarks = buildRemarks(request.getRemarks(), dto != null ? dto.getRemarks() : null,
                "Rejected by " + actor.getUsername());
        request.setRemarks(combinedRemarks);
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        return toResponse(request);
    }

    // Helper methods
    private RequestResponse toResponse(ItemRequest request) {
        RequestResponse resp = new RequestResponse();
        resp.setId(request.getId());
        resp.setRequesterId(request.getRequester().getId());
        resp.setRequesterUsername(request.getRequester().getUsername());
        resp.setRequesterEmail(request.getRequester().getEmail());
        resp.setStatus(request.getStatus().name());
        resp.setRemarks(request.getRemarks());
        resp.setCreatedAt(request.getCreatedAt());
        resp.setUpdatedAt(request.getUpdatedAt());

        List<RequestItem> items = requestItemRepository.findByRequestIdOrderByIdAsc(request.getId());
        List<RequestResponse.RequestItemResponse> itemResponses = new ArrayList<>();
        for (RequestItem ri : items) {
            RequestResponse.RequestItemResponse ir = new RequestResponse.RequestItemResponse();
            ir.setId(ri.getId());
            ir.setInventoryItemId(ri.getInventoryItem().getId());
            ir.setItemName(ri.getInventoryItem().getItemName());
            ir.setItemCode(ri.getInventoryItem().getItemCode());
            ir.setQuantity(ri.getQuantity());
            ir.setUnitSnapshot(ri.getUnitSnapshot());
            ir.setExpirationSnapshot(ri.getExpirationSnapshot());
            itemResponses.add(ir);
        }
        resp.setItems(itemResponses);

        return resp;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InventoryApiException("AUTH_USER_NOT_FOUND", 401, "Authenticated user not found."));
    }

    private ItemRequest getRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new InventoryApiException("REQUEST_NOT_FOUND", 404, "Request not found."));
    }

    private void requireTechnicianOrAdmin(User user) {
        if (user.getRole() == User.Role.STUDENT) {
            throw new InventoryApiException("FORBIDDEN", 403, "Only Technician or Admin can perform this action.");
        }
    }

    private String buildRemarks(String existing, String actionRemarks, String systemNote) {
        StringBuilder sb = new StringBuilder();
        if (existing != null && !existing.isBlank()) {
            sb.append(existing).append("\n");
        }
        if (actionRemarks != null && !actionRemarks.isBlank()) {
            sb.append(actionRemarks).append("\n");
        }
        sb.append("[SYSTEM] ").append(systemNote);
        return sb.toString().trim();
    }
}
