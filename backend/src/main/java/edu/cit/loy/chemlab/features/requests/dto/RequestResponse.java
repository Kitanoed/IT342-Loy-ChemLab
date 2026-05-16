package edu.cit.loy.chemlab.features.requests.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterUsername;
    private String requesterEmail;
    private String status;
    private String remarks;
    private List<RequestItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<RequestItemResponse> getItems() { return items; }
    public void setItems(List<RequestItemResponse> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class RequestItemResponse {
        private Long id;
        private Long inventoryItemId;
        private String itemName;
        private String itemCode;
        private int quantity;
        private String unitSnapshot;
        private LocalDate expirationSnapshot;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getInventoryItemId() { return inventoryItemId; }
        public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getItemCode() { return itemCode; }
        public void setItemCode(String itemCode) { this.itemCode = itemCode; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getUnitSnapshot() { return unitSnapshot; }
        public void setUnitSnapshot(String unitSnapshot) { this.unitSnapshot = unitSnapshot; }

        public LocalDate getExpirationSnapshot() { return expirationSnapshot; }
        public void setExpirationSnapshot(LocalDate expirationSnapshot) { this.expirationSnapshot = expirationSnapshot; }
    }
}
