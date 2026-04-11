package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.BulkJobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class BulkJobExecuteResponse {

    private UUID jobId;
    private BulkJobStatus status;
    private Summary summary;
    private UUID correlationId;
    private LocalDateTime completedAt;

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

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static class Summary {
        private Integer totalRows;
        private Integer successRows;
        private Integer failedRows;
        private Integer skippedRows;

        public Integer getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(Integer totalRows) {
            this.totalRows = totalRows;
        }

        public Integer getSuccessRows() {
            return successRows;
        }

        public void setSuccessRows(Integer successRows) {
            this.successRows = successRows;
        }

        public Integer getFailedRows() {
            return failedRows;
        }

        public void setFailedRows(Integer failedRows) {
            this.failedRows = failedRows;
        }

        public Integer getSkippedRows() {
            return skippedRows;
        }

        public void setSkippedRows(Integer skippedRows) {
            this.skippedRows = skippedRows;
        }
    }
}
