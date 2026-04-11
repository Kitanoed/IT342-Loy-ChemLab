package edu.cit.loy.chemlab.controller;

import edu.cit.loy.chemlab.dto.inventory.*;
import edu.cit.loy.chemlab.entity.InventoryItemType;
import edu.cit.loy.chemlab.entity.InventoryStatus;
import edu.cit.loy.chemlab.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<Page<InventoryItemResponse>> getInventory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InventoryItemType type,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(required = false) Long labId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        return ResponseEntity.ok(inventoryService.searchInventory(search, type, status, labId, pageable));
    }

    @GetMapping("/inventory/{itemId}")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<InventoryItemResponse> getInventoryItem(@PathVariable Long itemId, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(itemId, authentication.getName()));
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @RequestBody InventoryUpsertRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(inventoryService.createInventoryItem(request, authentication.getName(), httpServletRequest));
    }

    @PutMapping("/inventory/{itemId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(
            @PathVariable Long itemId,
            @RequestBody InventoryUpsertRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(itemId, request, authentication.getName(), httpServletRequest));
    }

    @PostMapping("/inventory/bulk-jobs")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<BulkJobCreateResponse> createBulkJob(@RequestBody CreateBulkJobRequest request, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.createBulkJob(request, authentication.getName()));
    }

    @PostMapping("/inventory/bulk-jobs/{jobId}/validate")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<BulkJobValidationResponse> validateBulkJob(@PathVariable UUID jobId, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.validateBulkJob(jobId, authentication.getName()));
    }

    @PostMapping("/inventory/bulk-jobs/{jobId}/execute")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<BulkJobExecuteResponse> executeBulkJob(
            @PathVariable UUID jobId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) BulkJobExecuteRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        BulkJobExecuteRequest effectiveRequest = request == null ? new BulkJobExecuteRequest() : request;
        return ResponseEntity.ok(inventoryService.executeBulkJob(jobId, effectiveRequest, idempotencyKey, authentication.getName(), httpServletRequest));
    }

    @GetMapping("/inventory/bulk-jobs/{jobId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<BulkJobDetailResponse> getBulkJob(@PathVariable UUID jobId, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.getBulkJob(jobId, authentication.getName()));
    }

    @GetMapping("/inventory/bulk-jobs/{jobId}/errors")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<BulkJobErrorsResponse> getBulkJobErrors(@PathVariable UUID jobId, Authentication authentication) {
        return ResponseEntity.ok(inventoryService.getBulkJobErrors(jobId, authentication.getName()));
    }

    @GetMapping("/inventory/{itemId}/audit-logs")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getItemAuditLogs(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inventoryService.getItemAuditLogs(itemId, pageable, authentication.getName()));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByJob(
            @RequestParam String entity,
            @RequestParam UUID jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        if (!"inventory".equalsIgnoreCase(entity)) {
            return ResponseEntity.badRequest().build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inventoryService.getAuditByJob(jobId, pageable, authentication.getName()));
    }

    @PatchMapping("/inventory/{itemId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<InventoryItemResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody InventoryUpdateRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(inventoryService.updateSingleItem(itemId, request, authentication.getName(), httpServletRequest));
    }
}
