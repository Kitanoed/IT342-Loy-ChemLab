package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.BulkJobStatus;
import edu.cit.loy.chemlab.entity.BulkRowStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BulkJobValidationResponse {

    private UUID jobId;
    private BulkJobStatus status;
    private Summary summary;
    private List<RowResult> rowResults = new ArrayList<>();

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

    public List<RowResult> getRowResults() {
        return rowResults;
    }

    public void setRowResults(List<RowResult> rowResults) {
        this.rowResults = rowResults;
    }

    public static class Summary {
        private Integer totalRows;
        private Integer validRows;
        private Integer invalidRows;
        private Integer warnings;

        public Integer getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(Integer totalRows) {
            this.totalRows = totalRows;
        }

        public Integer getValidRows() {
            return validRows;
        }

        public void setValidRows(Integer validRows) {
            this.validRows = validRows;
        }

        public Integer getInvalidRows() {
            return invalidRows;
        }

        public void setInvalidRows(Integer invalidRows) {
            this.invalidRows = invalidRows;
        }

        public Integer getWarnings() {
            return warnings;
        }

        public void setWarnings(Integer warnings) {
            this.warnings = warnings;
        }
    }

    public static class RowResult {
        private Integer rowNo;
        private BulkRowStatus status;
        private List<Message> messages = new ArrayList<>();

        public Integer getRowNo() {
            return rowNo;
        }

        public void setRowNo(Integer rowNo) {
            this.rowNo = rowNo;
        }

        public BulkRowStatus getStatus() {
            return status;
        }

        public void setStatus(BulkRowStatus status) {
            this.status = status;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }

    public static class Message {
        private String code;
        private String field;
        private String message;

        public Message() {
        }

        public Message(String code, String field, String message) {
            this.code = code;
            this.field = field;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
