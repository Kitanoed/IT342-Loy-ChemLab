package edu.cit.loy.chemlab.features.requests.controller;

import edu.cit.loy.chemlab.entity.RequestStatus;
import edu.cit.loy.chemlab.features.requests.dto.RequestActionDTO;
import edu.cit.loy.chemlab.features.requests.dto.RequestCreateDTO;
import edu.cit.loy.chemlab.features.requests.dto.RequestResponse;
import edu.cit.loy.chemlab.features.requests.service.RequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<RequestResponse> createRequest(
            @RequestBody RequestCreateDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.createRequest(request, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<Page<RequestResponse>> listRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(requestService.listRequests(status, authentication.getName(), pageable));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<RequestResponse> getRequest(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(requestService.getRequestById(requestId, authentication.getName()));
    }

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<RequestResponse> approveRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) RequestActionDTO action,
            Authentication authentication
    ) {
        return ResponseEntity.ok(requestService.approveRequest(requestId, action, authentication.getName()));
    }

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<RequestResponse> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) RequestActionDTO action,
            Authentication authentication
    ) {
        return ResponseEntity.ok(requestService.rejectRequest(requestId, action, authentication.getName()));
    }
}
