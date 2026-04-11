package edu.cit.loy.chemlab.dto.inventory;

import edu.cit.loy.chemlab.entity.InventoryStatus;

import java.math.BigDecimal;

public class InventoryUpdateRequest {

    private BigDecimal quantity;
    private InventoryStatus status;
    private String reasonCode;
    private String note;
    private Long version;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
