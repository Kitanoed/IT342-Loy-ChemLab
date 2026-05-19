package edu.cit.loy.chemlab.features.auth.service;

import edu.cit.loy.chemlab.entity.RefreshToken;
import edu.cit.loy.chemlab.entity.User;
import edu.cit.loy.chemlab.features.auth.dto.AuthResponse;
import edu.cit.loy.chemlab.features.auth.dto.LoginRequest;
import edu.cit.loy.chemlab.features.auth.dto.RegisterRequest;
import edu.cit.loy.chemlab.features.user.dto.UserDTO;
import edu.cit.loy.chemlab.repository.RefreshTokenRepository;
import edu.cit.loy.chemlab.repository.UserRepository;
import edu.cit.loy.chemlab.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final long refreshTokenExpirationMs;
    private final String googleClientId;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMs,
                       @Value("${google.client.id}") String googleClientId) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.googleClientId = googleClientId;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Password is required");
        }
        if (request.getPassword().length() < 6) {
            return AuthResponse.error("VALID-001", "Validation failed", "Password must be at least 6 characters");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("DB-002", "Email already exists", "An account with this email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("DB-002", "Username already taken", "This username is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.STUDENT);

        User savedUser = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getEmail(), savedUser.getRole().name());
        String refreshTokenStr = jwtUtil.generateRefreshToken(savedUser.getEmail());

        // Persist refresh token to database
        persistRefreshToken(savedUser, refreshTokenStr);

        return AuthResponse.success(toDTO(savedUser), accessToken, refreshTokenStr);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Password is required");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return AuthResponse.error("AUTH-001", "Invalid credentials", "Email or password is incorrect");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.error("AUTH-001", "Invalid credentials", "Email or password is incorrect");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshTokenStr = jwtUtil.generateRefreshToken(user.getEmail());

        // Persist refresh token to database
        persistRefreshToken(user, refreshTokenStr);

        return AuthResponse.success(toDTO(user), accessToken, refreshTokenStr);
    }

    @Transactional
    public AuthResponse googleLogin(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "ID Token is required");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                
                User user = userRepository.findByEmail(email).orElse(null);
                
                if (user == null) {
                    // Create a new user if one doesn't exist
                    user = new User();
                    user.setEmail(email);
                    
                    // Generate a random username base, or use email prefix
                    String baseUsername = email.split("@")[0];
                    String username = baseUsername;
                    int counter = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + counter++;
                    }
                    user.setUsername(username);
                    
                    // Generate a random secure password for Google users since they use Google to auth
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    
                    user.setFirstName((String) payload.get("given_name"));
                    user.setLastName((String) payload.get("family_name"));
                    user.setRole(User.Role.STUDENT);
                    
                    user = userRepository.save(user);
                }
                
                String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
                String refreshTokenStr = jwtUtil.generateRefreshToken(user.getEmail());
                
                persistRefreshToken(user, refreshTokenStr);
                
                return AuthResponse.success(toDTO(user), accessToken, refreshTokenStr);
            } else {
                return AuthResponse.error("AUTH-003", "Invalid ID token", "Google ID token could not be verified.");
            }
        } catch (Exception e) {
            return AuthResponse.error("AUTH-003", "Invalid ID token", "Error verifying Google ID token: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Refresh token is required");
        }

        // Validate the JWT signature and expiration
        if (!jwtUtil.isTokenValid(refreshTokenStr)) {
            return AuthResponse.error("AUTH-002", "Invalid refresh token", "Refresh token is expired or invalid");
        }

        // Check database revocation
        RefreshToken dbToken = refreshTokenRepository.findByToken(refreshTokenStr).orElse(null);
        if (dbToken == null || !dbToken.isUsable()) {
            return AuthResponse.error("AUTH-002", "Invalid refresh token", "Refresh token has been revoked or expired");
        }

        // Revoke the old token
        dbToken.setRevoked(true);
        refreshTokenRepository.save(dbToken);

        User user = dbToken.getUser();

        // Issue new token pair
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshTokenStr = jwtUtil.generateRefreshToken(user.getEmail());
        persistRefreshToken(user, newRefreshTokenStr);

        return AuthResponse.success(toDTO(user), newAccessToken, newRefreshTokenStr);
    }

    @Transactional
    public AuthResponse logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.revokeByToken(refreshTokenStr);
        }

        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setData(null);
        response.setError(null);
        return response;
    }

    private void persistRefreshToken(User user, String tokenStr) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);
        RefreshToken refreshToken = new RefreshToken(user, tokenStr, expiresAt);
        refreshTokenRepository.save(refreshToken);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
