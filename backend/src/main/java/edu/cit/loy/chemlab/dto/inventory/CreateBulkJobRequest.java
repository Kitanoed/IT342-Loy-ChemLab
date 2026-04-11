package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.BulkOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateBulkJobRequest {

    private BulkOperationType operationType;
    private Long labId;
    private String reasonCode;
    private String note;
    private String sourceType;
    private List<RowInput> rows = new ArrayList<>();

    public BulkOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(BulkOperationType operationType) {
        this.operationType = operationType;
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public List<RowInput> getRows() {
        return rows;
    }

    public void setRows(List<RowInput> rows) {
        this.rows = rows;
    }

    public static class RowInput {
        private Integer rowNo;
        private Long itemId;
        private BigDecimal deltaQuantity;
        private BigDecimal setQuantity;
        private String unit;
        private String storageLocation;
        private String status;
        private String lotNumber;
        private LocalDate expiryDate;

        public Integer getRowNo() {
            return rowNo;
        }

        public void setRowNo(Integer rowNo) {
            this.rowNo = rowNo;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public BigDecimal getDeltaQuantity() {
            return deltaQuantity;
        }

        public void setDeltaQuantity(BigDecimal deltaQuantity) {
            this.deltaQuantity = deltaQuantity;
        }

        public BigDecimal getSetQuantity() {
            return setQuantity;
        }

        public void setSetQuantity(BigDecimal setQuantity) {
            this.setQuantity = setQuantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getStorageLocation() {
            return storageLocation;
        }

        public void setStorageLocation(String storageLocation) {
            this.storageLocation = storageLocation;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public void setLotNumber(String lotNumber) {
            this.lotNumber = lotNumber;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }
}
