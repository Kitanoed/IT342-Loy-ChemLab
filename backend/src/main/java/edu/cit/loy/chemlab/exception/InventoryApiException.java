package edu.cit.loy.chemlab.exception;

public class InventoryApiException extends RuntimeException {

    private final String code;
    private final int status;
    private final Object details;

    public InventoryApiException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = null;
    }

    public InventoryApiException(String code, int status, String message, Object details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
