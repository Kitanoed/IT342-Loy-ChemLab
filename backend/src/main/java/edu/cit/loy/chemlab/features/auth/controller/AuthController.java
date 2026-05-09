package edu.cit.loy.chemlab.features.auth.controller;

import edu.cit.loy.chemlab.features.auth.dto.AuthResponse;
import edu.cit.loy.chemlab.features.auth.dto.LoginRequest;
import edu.cit.loy.chemlab.features.auth.dto.RegisterRequest;
import edu.cit.loy.chemlab.features.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        if (!response.isSuccess()) {
            String errorCode = response.getError().getCode();
            if (errorCode.equals("VALID-001")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else if (errorCode.equals("DB-002")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (!response.isSuccess()) {
            String errorCode = response.getError().getCode();
            if (errorCode.equals("VALID-001")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else if (errorCode.equals("AUTH-001")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setData(null);
        response.setError(null);
        return ResponseEntity.ok(response);
    }
}