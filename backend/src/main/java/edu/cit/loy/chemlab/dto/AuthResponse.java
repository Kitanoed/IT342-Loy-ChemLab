package edu.cit.loy.chemlab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private boolean success;
    private AuthData data;
    private ErrorInfo error;
    private String timestamp;

    public AuthResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    // Success factory
    public static AuthResponse success(UserDTO user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.success = true;
        response.data = new AuthData(user, accessToken, refreshToken);
        response.error = null;
        return response;
    }

    // Error factory
    public static AuthResponse error(String code, String message, Object details) {
        AuthResponse response = new AuthResponse();
        response.success = false;
        response.data = null;
        response.error = new ErrorInfo(code, message, details);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public AuthData getData() { return data; }
    public void setData(AuthData data) { this.data = data; }

    public ErrorInfo getError() { return error; }
    public void setError(ErrorInfo error) { this.error = error; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Inner classes
    public static class AuthData {
        private UserDTO user;
        private String accessToken;
        private String refreshToken;

        public AuthData() {}

        public AuthData(UserDTO user, String accessToken, String refreshToken) {
            this.user = user;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class ErrorInfo {
        private String code;
        private String message;
        private Object details;

        public ErrorInfo() {}

        public ErrorInfo(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }
    }
}
