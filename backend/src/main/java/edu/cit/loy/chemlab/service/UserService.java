package edu.cit.loy.chemlab.service;

import edu.cit.loy.chemlab.dto.*;
import edu.cit.loy.chemlab.entity.User;
import edu.cit.loy.chemlab.repository.UserRepository;
import edu.cit.loy.chemlab.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // Validate required fields
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

        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("DB-002", "Email already exists", "An account with this email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("DB-002", "Username already taken", "This username is already in use");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.STUDENT);

        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        return AuthResponse.success(toDTO(savedUser), accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        // Validate required fields
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return AuthResponse.error("VALID-001", "Validation failed", "Password is required");
        }

        // Find user
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return AuthResponse.error("AUTH-001", "Invalid credentials", "Email or password is incorrect");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.error("AUTH-001", "Invalid credentials", "Email or password is incorrect");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.success(toDTO(user), accessToken, refreshToken);
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        return toDTO(user);
    }

    public UserDTO toDTO(User user) {
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
