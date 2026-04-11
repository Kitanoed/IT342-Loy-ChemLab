package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.BulkJobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class BulkJobCreateResponse {

    private UUID jobId;
    private BulkJobStatus status;
    private Integer totalRows;
    private LocalDateTime createdAt;

    public BulkJobCreateResponse() {
    }

    public BulkJobCreateResponse(UUID jobId, BulkJobStatus status, Integer totalRows, LocalDateTime createdAt) {
        this.jobId = jobId;
        this.status = status;
        this.totalRows = totalRows;
        this.createdAt = createdAt;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public BulkJobStatus getStatus() {
        return status;
    }

    public void setStatus(BulkJobStatus status) {
        this.status = status;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
