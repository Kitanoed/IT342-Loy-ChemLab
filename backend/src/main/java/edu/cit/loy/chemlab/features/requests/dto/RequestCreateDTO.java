package edu.cit.loy.chemlab.features.requests.dto;

import java.util.List;

public class RequestCreateDTO {

    private List<RequestItemInput> items;
    private String remarks;

    public RequestCreateDTO() {}

    public List<RequestItemInput> getItems() { return items; }
    public void setItems(List<RequestItemInput> items) { this.items = items; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public static class RequestItemInput {
        private Long inventoryItemId;
        private int quantity;

        public RequestItemInput() {}

        public Long getInventoryItemId() { return inventoryItemId; }
        public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
