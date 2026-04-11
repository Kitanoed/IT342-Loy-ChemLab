package edu.cit.loy.chemlab.exception;

import edu.cit.loy.chemlab.dto.AuthResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<AuthResponse> handleAccessDenied(Exception ex) {
        AuthResponse response = AuthResponse.error(
                "AUTH-403",
                "Access denied",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InventoryApiException.class)
    public ResponseEntity<Map<String, Object>> handleInventoryApiException(InventoryApiException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", ex.getStatus());
        body.put("error", HttpStatus.valueOf(ex.getStatus()).getReasonPhrase());
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        body.put("details", ex.getDetails());
        return ResponseEntity.status(ex.getStatus()).header(HttpHeaders.CONTENT_TYPE, "application/json").body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        AuthResponse response = AuthResponse.error(
                "SYSTEM-001",
                "Internal server error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<AuthResponse> handleNotFound(NoHandlerFoundException ex) {
        AuthResponse response = AuthResponse.error(
                "DB-001",
                "Resource not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgument(IllegalArgumentException ex) {
        AuthResponse response = AuthResponse.error(
                "VALID-001",
                "Validation failed",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
