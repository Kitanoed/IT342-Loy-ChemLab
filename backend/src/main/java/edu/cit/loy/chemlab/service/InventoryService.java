package edu.cit.loy.chemlab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.loy.chemlab.dto.inventory.*;
import edu.cit.loy.chemlab.entity.*;
import edu.cit.loy.chemlab.exception.InventoryApiException;
import edu.cit.loy.chemlab.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryBulkJobRepository bulkJobRepository;
    private final InventoryBulkJobRowRepository bulkJobRowRepository;
    private final InventoryAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            InventoryBulkJobRepository bulkJobRepository,
            InventoryBulkJobRowRepository bulkJobRowRepository,
            InventoryAuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.bulkJobRepository = bulkJobRepository;
        this.bulkJobRowRepository = bulkJobRowRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Page<InventoryItemResponse> searchInventory(String search, InventoryItemType type, InventoryStatus status, Long labId, Pageable pageable) {
        return inventoryItemRepository.search(null, type, status, labId, pageable).map(this::toItemResponse);
    }

    public InventoryItemResponse getInventoryItem(Long itemId, String actorEmail) {
        User actor = getActor(actorEmail);
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new InventoryApiException("ITEM_NOT_FOUND", 404, "Inventory item not found."));

        if (actor.getRole() != User.Role.ADMIN && actor.getRole() != User.Role.STUDENT) {
            ensureLabAccess(actor, item.getLabId());
        }

        return toItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse createInventoryItem(InventoryUpsertRequest request, String actorEmail, HttpServletRequest httpRequest) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        validateUpsertRequest(request, true);
        ensureLabAccess(actor, request.getLabId());

        if (inventoryItemRepository.existsByItemCodeIgnoreCase(request.getItemCode())) {
            throw new InventoryApiException("ITEM_CODE_EXISTS", 409, "Item code already exists.");
        }

        InventoryItem item = new InventoryItem();
        applyUpsertFields(item, request);
        item.setCreatedBy(actor.getId());
        item.setUpdatedBy(actor.getId());
        refreshLowStockStatus(item);

        InventoryItem saved = inventoryItemRepository.save(item);

        writeAudit(
                "INVENTORY_ITEM",
                String.valueOf(saved.getId()),
                "CREATE",
                actor,
                saved.getLabId(),
                "CREATE_ITEM",
                UUID.randomUUID(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"),
                null,
                asJson(itemSnapshot(saved)),
                Map.of("itemCode", saved.getItemCode())
        );

        return toItemResponse(saved);
    }

    @Transactional
    public InventoryItemResponse updateInventoryItem(Long itemId, InventoryUpsertRequest request, String actorEmail, HttpServletRequest httpRequest) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new InventoryApiException("ITEM_NOT_FOUND", 404, "Inventory item not found."));

        validateUpsertRequest(request, false);
        ensureLabAccess(actor, item.getLabId());

        String before = asJson(itemSnapshot(item));
        applyUpsertFields(item, request);
        item.setUpdatedBy(actor.getId());
        refreshLowStockStatus(item);

        InventoryItem saved = inventoryItemRepository.save(item);

        writeAudit(
                "INVENTORY_ITEM",
                String.valueOf(saved.getId()),
                "UPDATE",
                actor,
                saved.getLabId(),
                "UPDATE_ITEM",
                UUID.randomUUID(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"),
                before,
                asJson(itemSnapshot(saved)),
                Map.of("itemCode", saved.getItemCode())
        );

        return toItemResponse(saved);
    }

    @Transactional
    public BulkJobCreateResponse createBulkJob(CreateBulkJobRequest request, String actorEmail) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        validateCreateRequest(request);
        ensureLabAccess(actor, request.getLabId());

        InventoryBulkJob job = new InventoryBulkJob();
        job.setOperationType(request.getOperationType());
        job.setStatus(BulkJobStatus.PENDING_VALIDATION);
        job.setLabId(request.getLabId());
        job.setCreatedBy(actor.getId());
        job.setReasonCode(request.getReasonCode());
        job.setNote(request.getNote());
        job.setSourceType(request.getSourceType() == null || request.getSourceType().isBlank() ? "MANUAL" : request.getSourceType());
        job.setTotalRows(request.getRows().size());
        job.setPayloadJson(asJson(request));

        InventoryBulkJob savedJob = bulkJobRepository.save(job);

        for (CreateBulkJobRequest.RowInput input : request.getRows()) {
            InventoryBulkJobRow row = new InventoryBulkJobRow();
            row.setJob(savedJob);
            row.setRowNo(input.getRowNo());
            row.setItemId(input.getItemId());
            row.setRequestedChange(asJson(input));
            row.setRowStatus(BulkRowStatus.PENDING);
            bulkJobRowRepository.save(row);
        }

        return new BulkJobCreateResponse(savedJob.getId(), savedJob.getStatus(), savedJob.getTotalRows(), savedJob.getCreatedAt());
    }

    @Transactional
    public BulkJobValidationResponse validateBulkJob(UUID jobId, String actorEmail) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryBulkJob job = getJob(jobId);
        ensureLabAccess(actor, job.getLabId());

        List<InventoryBulkJobRow> rows = bulkJobRowRepository.findByJob_IdOrderByRowNoAsc(jobId);
        int validCount = 0;
        int invalidCount = 0;

        BulkJobValidationResponse response = new BulkJobValidationResponse();
        response.setJobId(jobId);
        response.setRowResults(new ArrayList<>());

        for (InventoryBulkJobRow row : rows) {
            List<BulkJobValidationResponse.Message> messages = validateRow(job, row);
            BulkJobValidationResponse.RowResult rowResult = new BulkJobValidationResponse.RowResult();
            rowResult.setRowNo(row.getRowNo());
            rowResult.setMessages(messages);

            if (messages.isEmpty()) {
                row.setRowStatus(BulkRowStatus.VALID);
                row.setErrorCode(null);
                row.setErrorMessage(null);
                rowResult.setStatus(BulkRowStatus.VALID);
                validCount++;
            } else {
                row.setRowStatus(BulkRowStatus.INVALID);
                row.setErrorCode(messages.get(0).getCode());
                row.setErrorMessage(messages.get(0).getMessage());
                rowResult.setStatus(BulkRowStatus.INVALID);
                invalidCount++;
            }
            bulkJobRowRepository.save(row);
            response.getRowResults().add(rowResult);
        }

        job.setValidRows(validCount);
        job.setStatus(invalidCount == 0 ? BulkJobStatus.READY : BulkJobStatus.PENDING_VALIDATION);
        bulkJobRepository.save(job);

        BulkJobValidationResponse.Summary summary = new BulkJobValidationResponse.Summary();
        summary.setTotalRows(rows.size());
        summary.setValidRows(validCount);
        summary.setInvalidRows(invalidCount);
        summary.setWarnings(0);

        response.setStatus(job.getStatus());
        response.setSummary(summary);
        return response;
    }

    @Transactional
    public BulkJobExecuteResponse executeBulkJob(UUID jobId, BulkJobExecuteRequest request, String idempotencyKey, String actorEmail, HttpServletRequest httpRequest) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryBulkJob job = getJob(jobId);
        ensureLabAccess(actor, job.getLabId());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<InventoryBulkJob> existing = bulkJobRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent() && !existing.get().getId().equals(jobId)) {
                throw new InventoryApiException("IDEMPOTENCY_CONFLICT", 409, "Idempotency key already used for another job.");
            }
            job.setIdempotencyKey(idempotencyKey);
        }

        List<InventoryBulkJobRow> rows = bulkJobRowRepository.findByJob_IdOrderByRowNoAsc(jobId);
        job.setStatus(BulkJobStatus.EXECUTING);
        job.setStartedAt(java.time.LocalDateTime.now());
        bulkJobRepository.save(job);

        int success = 0;
        int failed = 0;
        int skipped = 0;
        UUID correlationId = UUID.randomUUID();

        for (InventoryBulkJobRow row : rows) {
            if (request != null && request.isExecuteOnlyValidRows() && row.getRowStatus() != BulkRowStatus.VALID) {
                row.setRowStatus(BulkRowStatus.SKIPPED);
                bulkJobRowRepository.save(row);
                skipped++;
                continue;
            }

            try {
                CreateBulkJobRequest.RowInput payload = objectMapper.readValue(row.getRequestedChange(), CreateBulkJobRequest.RowInput.class);
                InventoryItem item = inventoryItemRepository.findById(payload.getItemId()).orElseThrow(
                        () -> new InventoryApiException("ITEM_NOT_FOUND", 404, "Item " + payload.getItemId() + " not found.")
                );

                if (!Objects.equals(item.getLabId(), job.getLabId())) {
                    throw new InventoryApiException("LAB_MISMATCH", 400, "Item does not belong to the selected lab.");
                }

                String before = asJson(itemSnapshot(item));
                applyOperation(job.getOperationType(), item, payload);
                item.setUpdatedBy(actor.getId());
                inventoryItemRepository.save(item);

                row.setBeforeSnapshot(before);
                row.setAfterSnapshot(asJson(itemSnapshot(item)));
                row.setRowStatus(BulkRowStatus.SUCCESS);
                row.setErrorCode(null);
                row.setErrorMessage(null);
                bulkJobRowRepository.save(row);

                writeAudit("INVENTORY_ITEM", String.valueOf(item.getId()), "BULK_" + job.getOperationType(), actor, job.getLabId(), job.getReasonCode(), correlationId,
                        httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"), before, row.getAfterSnapshot(),
                        Map.of("jobId", job.getId(), "rowNo", row.getRowNo()));

                success++;
            } catch (InventoryApiException ex) {
                row.setRowStatus(BulkRowStatus.FAILED);
                row.setErrorCode(ex.getCode());
                row.setErrorMessage(ex.getMessage());
                bulkJobRowRepository.save(row);
                failed++;
            } catch (JsonProcessingException ex) {
                row.setRowStatus(BulkRowStatus.FAILED);
                row.setErrorCode("INVALID_ROW_JSON");
                row.setErrorMessage("Row payload is invalid.");
                bulkJobRowRepository.save(row);
                failed++;
            }
        }

        job.setSuccessRows(success);
        job.setFailedRows(failed);
        job.setCompletedAt(java.time.LocalDateTime.now());
        job.setStatus(failed == 0 ? BulkJobStatus.SUCCESS : (success > 0 ? BulkJobStatus.PARTIAL_SUCCESS : BulkJobStatus.FAILED));
        bulkJobRepository.save(job);

        writeAudit("BULK_JOB", String.valueOf(job.getId()), "BULK_EXECUTE", actor, job.getLabId(), job.getReasonCode(), correlationId,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"), null, null,
                Map.of("successRows", success, "failedRows", failed, "skippedRows", skipped));

        BulkJobExecuteResponse response = new BulkJobExecuteResponse();
        response.setJobId(job.getId());
        response.setStatus(job.getStatus());

        BulkJobExecuteResponse.Summary summary = new BulkJobExecuteResponse.Summary();
        summary.setTotalRows(rows.size());
        summary.setSuccessRows(success);
        summary.setFailedRows(failed);
        summary.setSkippedRows(skipped);

        response.setSummary(summary);
        response.setCorrelationId(correlationId);
        response.setCompletedAt(job.getCompletedAt());
        return response;
    }

    public BulkJobDetailResponse getBulkJob(UUID jobId, String actorEmail) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryBulkJob job = getJob(jobId);
        ensureLabAccess(actor, job.getLabId());

        BulkJobDetailResponse response = new BulkJobDetailResponse();
        response.setJobId(job.getId());
        response.setOperationType(job.getOperationType());
        response.setStatus(job.getStatus());
        response.setReasonCode(job.getReasonCode());
        response.setCreatedBy(job.getCreatedBy());
        response.setLabId(job.getLabId());
        response.setCreatedAt(job.getCreatedAt());
        response.setCompletedAt(job.getCompletedAt());

        BulkJobDetailResponse.Counts counts = new BulkJobDetailResponse.Counts();
        counts.setTotal(job.getTotalRows());
        counts.setValid(job.getValidRows());
        counts.setSuccess(job.getSuccessRows());
        counts.setFailed(job.getFailedRows());
        response.setCounts(counts);

        return response;
    }

    public BulkJobErrorsResponse getBulkJobErrors(UUID jobId, String actorEmail) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryBulkJob job = getJob(jobId);
        ensureLabAccess(actor, job.getLabId());

        List<InventoryBulkJobRow> failedRows = bulkJobRowRepository.findByJob_IdAndRowStatusOrderByRowNoAsc(jobId, BulkRowStatus.FAILED);
        BulkJobErrorsResponse response = new BulkJobErrorsResponse();
        response.setJobId(jobId);

        for (InventoryBulkJobRow row : failedRows) {
            BulkJobErrorsResponse.RowError error = new BulkJobErrorsResponse.RowError();
            error.setRowNo(row.getRowNo());
            error.setItemId(row.getItemId());
            error.setErrorCode(row.getErrorCode());
            error.setErrorMessage(row.getErrorMessage());
            response.getErrors().add(error);
        }

        return response;
    }

    public Page<AuditLogResponse> getItemAuditLogs(Long itemId, Pageable pageable, String actorEmail) {
        User actor = getActor(actorEmail);
        if (actor.getRole() == User.Role.STUDENT) {
            throw new InventoryApiException("FORBIDDEN", 403, "Insufficient permissions.");
        }

        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("INVENTORY_ITEM", String.valueOf(itemId), pageable)
                .map(this::toAuditResponse);
    }

    public Page<AuditLogResponse> getAuditByJob(UUID correlationId, Pageable pageable, String actorEmail) {
        User actor = getActor(actorEmail);
        if (actor.getRole() != User.Role.ADMIN) {
            throw new InventoryApiException("FORBIDDEN", 403, "Only admin can access cross-entity audit logs.");
        }

        return auditLogRepository
                .findByEntityTypeAndCorrelationIdOrderByCreatedAtDesc("BULK_JOB", correlationId, pageable)
                .map(this::toAuditResponse);
    }

    @Transactional
    public InventoryItemResponse updateSingleItem(Long itemId, InventoryUpdateRequest request, String actorEmail, HttpServletRequest httpRequest) {
        User actor = getActor(actorEmail);
        requireTechnicianOrAdmin(actor);

        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new InventoryApiException("ITEM_NOT_FOUND", 404, "Inventory item not found."));

        ensureLabAccess(actor, item.getLabId());

        if (request.getReasonCode() == null || request.getReasonCode().isBlank()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "reasonCode is required.");
        }
        if (request.getVersion() != null && !request.getVersion().equals(item.getVersion())) {
            throw new InventoryApiException("VERSION_MISMATCH", 409, "Item was updated by another user.", Map.of("expected", item.getVersion(), "actual", request.getVersion()));
        }

        String before = asJson(itemSnapshot(item));

        if (request.getQuantity() != null) {
            if (request.getQuantity().compareTo(BigDecimal.ZERO) < 0 && !item.isBackorderAllowed()) {
                throw new InventoryApiException("NEGATIVE_STOCK", 400, "Quantity cannot be negative.");
            }
            item.setQuantity(request.getQuantity());
        }

        if (request.getStatus() != null) {
            if (item.getItemType() == InventoryItemType.CHEMICAL && request.getStatus() == InventoryStatus.AVAILABLE
                    && item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
                throw new InventoryApiException("EXPIRED_ITEM", 400, "Expired chemical cannot be marked available.");
            }
            item.setStatus(request.getStatus());
        }

        refreshLowStockStatus(item);
        item.setUpdatedBy(actor.getId());
        inventoryItemRepository.save(item);

        writeAudit("INVENTORY_ITEM", String.valueOf(item.getId()), "SINGLE_UPDATE", actor, item.getLabId(), request.getReasonCode(), UUID.randomUUID(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"), before, asJson(itemSnapshot(item)), Map.of("note", request.getNote()));

        return toItemResponse(item);
    }

    private List<BulkJobValidationResponse.Message> validateRow(InventoryBulkJob job, InventoryBulkJobRow row) {
        List<BulkJobValidationResponse.Message> messages = new ArrayList<>();

        CreateBulkJobRequest.RowInput input;
        try {
            input = objectMapper.readValue(row.getRequestedChange(), CreateBulkJobRequest.RowInput.class);
        } catch (JsonMappingException e) {
            messages.add(new BulkJobValidationResponse.Message("INVALID_ROW_JSON", "row", "Row payload is invalid."));
            return messages;
        } catch (JsonProcessingException e) {
            messages.add(new BulkJobValidationResponse.Message("INVALID_ROW_JSON", "row", "Row payload is invalid."));
            return messages;
        }

        if (input.getItemId() == null) {
            messages.add(new BulkJobValidationResponse.Message("ITEM_ID_REQUIRED", "itemId", "itemId is required."));
            return messages;
        }

        Optional<InventoryItem> maybeItem = inventoryItemRepository.findById(input.getItemId());
        if (maybeItem.isEmpty()) {
            messages.add(new BulkJobValidationResponse.Message("ITEM_NOT_FOUND", "itemId", "Item not found."));
            return messages;
        }

        InventoryItem item = maybeItem.get();
        if (!Objects.equals(item.getLabId(), job.getLabId())) {
            messages.add(new BulkJobValidationResponse.Message("LAB_MISMATCH", "labId", "Item does not belong to selected lab."));
        }

        if (job.getOperationType() == BulkOperationType.ADJUST_QUANTITY) {
            if (input.getDeltaQuantity() == null) {
                messages.add(new BulkJobValidationResponse.Message("DELTA_REQUIRED", "deltaQuantity", "deltaQuantity is required for ADJUST_QUANTITY."));
            } else {
                BigDecimal result = item.getQuantity().add(input.getDeltaQuantity());
                if (result.compareTo(BigDecimal.ZERO) < 0 && !item.isBackorderAllowed()) {
                    messages.add(new BulkJobValidationResponse.Message("NEGATIVE_STOCK", "deltaQuantity", "Resulting quantity cannot be negative."));
                }
            }
        }

        if (job.getOperationType() == BulkOperationType.SET_QUANTITY) {
            if (input.getSetQuantity() == null) {
                messages.add(new BulkJobValidationResponse.Message("SET_QUANTITY_REQUIRED", "setQuantity", "setQuantity is required for SET_QUANTITY."));
            } else if (input.getSetQuantity().compareTo(BigDecimal.ZERO) < 0 && !item.isBackorderAllowed()) {
                messages.add(new BulkJobValidationResponse.Message("NEGATIVE_STOCK", "setQuantity", "Resulting quantity cannot be negative."));
            }
        }

        if (input.getExpiryDate() != null && item.getReceivedDate() != null && input.getExpiryDate().isBefore(item.getReceivedDate())) {
            messages.add(new BulkJobValidationResponse.Message("INVALID_EXPIRY", "expiryDate", "expiryDate cannot be earlier than receivedDate."));
        }

        return messages;
    }

    private void applyOperation(BulkOperationType operationType, InventoryItem item, CreateBulkJobRequest.RowInput payload) {
        switch (operationType) {
            case ADJUST_QUANTITY -> {
                if (payload.getDeltaQuantity() == null) {
                    throw new InventoryApiException("DELTA_REQUIRED", 400, "deltaQuantity is required.");
                }
                BigDecimal updated = item.getQuantity().add(payload.getDeltaQuantity());
                if (updated.compareTo(BigDecimal.ZERO) < 0 && !item.isBackorderAllowed()) {
                    throw new InventoryApiException("NEGATIVE_STOCK", 400, "Resulting quantity cannot be negative.");
                }
                item.setQuantity(updated);
            }
            case SET_QUANTITY -> {
                if (payload.getSetQuantity() == null) {
                    throw new InventoryApiException("SET_QUANTITY_REQUIRED", 400, "setQuantity is required.");
                }
                if (payload.getSetQuantity().compareTo(BigDecimal.ZERO) < 0 && !item.isBackorderAllowed()) {
                    throw new InventoryApiException("NEGATIVE_STOCK", 400, "Resulting quantity cannot be negative.");
                }
                item.setQuantity(payload.getSetQuantity());
            }
            case UPDATE_METADATA -> {
                if (payload.getStorageLocation() != null) {
                    item.setStorageLocation(payload.getStorageLocation());
                }
                if (payload.getLotNumber() != null) {
                    item.setLotNumber(payload.getLotNumber());
                }
                if (payload.getExpiryDate() != null) {
                    item.setExpiryDate(payload.getExpiryDate());
                }
                if (payload.getStatus() != null) {
                    item.setStatus(InventoryStatus.valueOf(payload.getStatus()));
                }
            }
            case TRANSFER_LOCATION -> {
                if (payload.getStorageLocation() == null || payload.getStorageLocation().isBlank()) {
                    throw new InventoryApiException("LOCATION_REQUIRED", 400, "storageLocation is required for TRANSFER_LOCATION.");
                }
                item.setStorageLocation(payload.getStorageLocation());
            }
        }

        if (item.getItemType() == InventoryItemType.CHEMICAL && item.getStatus() == InventoryStatus.AVAILABLE
                && item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InventoryApiException("EXPIRED_ITEM", 400, "Expired chemical cannot be marked available.");
        }

        refreshLowStockStatus(item);
    }

    private void refreshLowStockStatus(InventoryItem item) {
        if (item.getQuantity() == null) {
            item.setQuantity(BigDecimal.ZERO);
        }

        if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            item.setStatus(InventoryStatus.OUT_OF_STOCK);
            return;
        }

        if (item.getMinThreshold() != null && item.getQuantity().compareTo(item.getMinThreshold()) <= 0) {
            item.setStatus(InventoryStatus.LOW_STOCK);
        } else if (item.getStatus() == InventoryStatus.LOW_STOCK || item.getStatus() == InventoryStatus.OUT_OF_STOCK) {
            item.setStatus(InventoryStatus.AVAILABLE);
        }
    }

    private Map<String, Object> itemSnapshot(InventoryItem item) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", item.getId());
        snapshot.put("itemCode", item.getItemCode());
        snapshot.put("itemName", item.getItemName());
        snapshot.put("quantity", item.getQuantity());
        snapshot.put("status", item.getStatus());
        snapshot.put("location", item.getStorageLocation());
        snapshot.put("lotNumber", item.getLotNumber());
        snapshot.put("pubchemCid", item.getPubchemCid());
        snapshot.put("molecularFormula", item.getMolecularFormula());
        snapshot.put("molecularWeight", item.getMolecularWeight());
        snapshot.put("iupacName", item.getIupacName());
        snapshot.put("expiryDate", item.getExpiryDate());
        snapshot.put("version", item.getVersion());
        return snapshot;
    }

    private InventoryItemResponse toItemResponse(InventoryItem item) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(item.getId());
        response.setItemCode(item.getItemCode());
        response.setItemName(item.getItemName());
        response.setItemType(item.getItemType());
        response.setCategory(item.getCategory());
        response.setQuantity(item.getQuantity());
        response.setUnit(item.getUnit());
        response.setMinThreshold(item.getMinThreshold());
        response.setStatus(item.getStatus());
        response.setLotNumber(item.getLotNumber());
        response.setDescription(item.getDescription());
        response.setSafetyNotes(item.getSafetyNotes());
        response.setPubchemCid(item.getPubchemCid());
        response.setMolecularFormula(item.getMolecularFormula());
        response.setMolecularWeight(item.getMolecularWeight());
        response.setIupacName(item.getIupacName());
        response.setExpiryDate(item.getExpiryDate());
        response.setLabId(item.getLabId());
        response.setStorageLocation(item.getStorageLocation());
        response.setVersion(item.getVersion());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    private void validateUpsertRequest(InventoryUpsertRequest request, boolean create) {
        if (request.getItemName() == null || request.getItemName().isBlank()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "itemName is required.");
        }
        if (request.getItemType() == null) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "itemType is required.");
        }
        if (request.getUnit() == null || request.getUnit().isBlank()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "unit is required.");
        }
        if (request.getQuantity() == null) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "quantity is required.");
        }
        if (request.getQuantity().compareTo(BigDecimal.ZERO) < 0 && (request.getBackorderAllowed() == null || !request.getBackorderAllowed())) {
            throw new InventoryApiException("NEGATIVE_STOCK", 400, "Quantity cannot be negative unless backorder is allowed.");
        }
        if (create && (request.getItemCode() == null || request.getItemCode().isBlank())) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "itemCode is required.");
        }
        if (create && request.getLabId() == null) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "labId is required.");
        }
    }

    private void applyUpsertFields(InventoryItem item, InventoryUpsertRequest request) {
        if (request.getItemCode() != null && !request.getItemCode().isBlank()) {
            item.setItemCode(request.getItemCode().trim());
        }
        item.setItemName(request.getItemName().trim());
        item.setItemType(request.getItemType());
        item.setCategory(request.getCategory());
        item.setCasNumber(request.getCasNumber());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setMinThreshold(request.getMinThreshold() == null ? BigDecimal.ZERO : request.getMinThreshold());
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        item.setLotNumber(request.getLotNumber());
        item.setDescription(request.getDescription());
        item.setSafetyNotes(request.getSafetyNotes());
        item.setPubchemCid(request.getPubchemCid());
        item.setMolecularFormula(request.getMolecularFormula());
        item.setMolecularWeight(request.getMolecularWeight());
        item.setIupacName(request.getIupacName());
        item.setExpiryDate(request.getExpiryDate());
        item.setReceivedDate(request.getReceivedDate());
        if (request.getLabId() != null) {
            item.setLabId(request.getLabId());
        }
        item.setStorageLocation(request.getStorageLocation());
        item.setSupplierName(request.getSupplierName());
        item.setBackorderAllowed(Boolean.TRUE.equals(request.getBackorderAllowed()));
    }

    private AuditLogResponse toAuditResponse(InventoryAuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setAction(log.getAction());
        response.setActorUserId(log.getActorUserId());
        response.setActorRole(log.getActorRole());
        response.setReasonCode(log.getReasonCode());
        response.setBeforeData(log.getBeforeData());
        response.setAfterData(log.getAfterData());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }

    private void validateCreateRequest(CreateBulkJobRequest request) {
        if (request.getOperationType() == null) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "operationType is required.");
        }
        if (request.getLabId() == null) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "labId is required.");
        }
        if (request.getReasonCode() == null || request.getReasonCode().isBlank()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "reasonCode is required.");
        }
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new InventoryApiException("VALIDATION_ERROR", 400, "rows are required.");
        }

        Set<Integer> rowNos = new HashSet<>();
        for (CreateBulkJobRequest.RowInput row : request.getRows()) {
            if (row.getRowNo() == null || row.getRowNo() < 1) {
                throw new InventoryApiException("VALIDATION_ERROR", 400, "Each row must have a positive rowNo.");
            }
            if (!rowNos.add(row.getRowNo())) {
                throw new InventoryApiException("VALIDATION_ERROR", 400, "Duplicate rowNo found: " + row.getRowNo());
            }
        }
    }

    private void writeAudit(String entityType, String entityId, String action, User actor, Long labId, String reasonCode, UUID correlationId,
                            String ipAddress, String userAgent, String beforeData, String afterData, Object metadata) {
        InventoryAuditLog log = new InventoryAuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setActorUserId(actor.getId());
        log.setActorRole(actor.getRole().name());
        log.setLabId(labId);
        log.setReasonCode(reasonCode);
        log.setCorrelationId(correlationId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setMetadata(asJson(metadata));
        auditLogRepository.save(log);
    }

    private String asJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new InventoryApiException("JSON_ERROR", 500, "Failed to serialize payload.");
        }
    }

    private InventoryBulkJob getJob(UUID jobId) {
        return bulkJobRepository.findById(jobId)
                .orElseThrow(() -> new InventoryApiException("JOB_NOT_FOUND", 404, "Bulk job not found."));
    }

    private User getActor(String actorEmail) {
        return userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new InventoryApiException("AUTH_USER_NOT_FOUND", 401, "Authenticated user not found."));
    }

    private void requireTechnicianOrAdmin(User user) {
        if (user.getRole() == User.Role.STUDENT) {
            throw new InventoryApiException("FORBIDDEN", 403, "Insufficient permissions.");
        }
    }

    private void ensureLabAccess(User actor, Long targetLabId) {
        if (actor.getRole() == User.Role.ADMIN) {
            return;
        }

        if (actor.getLabId() == null) {
            throw new InventoryApiException("TECHNICIAN_LAB_UNASSIGNED", 403, "Technician has no assigned lab.");
        }

        if (!Objects.equals(actor.getLabId(), targetLabId)) {
            throw new InventoryApiException("FORBIDDEN_LAB_SCOPE", 403, "Technician can only modify assigned lab inventory.");
        }
    }
}
