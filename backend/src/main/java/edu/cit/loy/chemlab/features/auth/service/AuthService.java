package edu.cit.loy.chemlab.features.auth.service;

import edu.cit.loy.chemlab.entity.User;
import edu.cit.loy.chemlab.features.auth.dto.AuthResponse;
import edu.cit.loy.chemlab.features.auth.dto.LoginRequest;
import edu.cit.loy.chemlab.features.auth.dto.RegisterRequest;
import edu.cit.loy.chemlab.features.user.dto.UserDTO;
import edu.cit.loy.chemlab.repository.UserRepository;
import edu.cit.loy.chemlab.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

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
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        return AuthResponse.success(toDTO(savedUser), accessToken, refreshToken);
    }

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
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.success(toDTO(user), accessToken, refreshToken);
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