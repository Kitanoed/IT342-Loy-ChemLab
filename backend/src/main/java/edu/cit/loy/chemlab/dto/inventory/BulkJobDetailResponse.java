package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.BulkJobStatus;
import edu.cit.loy.chemlab.entity.BulkOperationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class BulkJobDetailResponse {

    private UUID jobId;
    private BulkOperationType operationType;
    private BulkJobStatus status;
    private String reasonCode;
    private Long createdBy;
    private Long labId;
    private Counts counts;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public BulkOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(BulkOperationType operationType) {
        this.operationType = operationType;
    }

    public BulkJobStatus getStatus() {
        return status;
    }

    public void setStatus(BulkJobStatus status) {
        this.status = status;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public Counts getCounts() {
        return counts;
    }

    public void setCounts(Counts counts) {
        this.counts = counts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static class Counts {
        private Integer total;
        private Integer valid;
        private Integer success;
        private Integer failed;

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getValid() {
            return valid;
        }

        public void setValid(Integer valid) {
            this.valid = valid;
        }

        public Integer getSuccess() {
            return success;
        }

        public void setSuccess(Integer success) {
            this.success = success;
        }

        public Integer getFailed() {
            return failed;
        }

        public void setFailed(Integer failed) {
            this.failed = failed;
        }
    }
}
