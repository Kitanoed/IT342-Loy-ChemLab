package edu.cit.loy.chemlab.features.auth.service;

import edu.cit.loy.chemlab.entity.User;
import edu.cit.loy.chemlab.features.auth.dto.AuthResponse;
import edu.cit.loy.chemlab.features.auth.dto.LoginRequest;
import edu.cit.loy.chemlab.features.auth.dto.RegisterRequest;
import edu.cit.loy.chemlab.repository.UserRepository;
import edu.cit.loy.chemlab.repository.RefreshTokenRepository;
import edu.cit.loy.chemlab.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtUtil, 86400000L);
    }

    @Test
    void registerCreatesUserAndTokens() {
        RegisterRequest request = new RegisterRequest("student@example.com", "student1", "secret12", "Stu", "Dent");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });
        when(jwtUtil.generateAccessToken("student@example.com", "STUDENT")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("student@example.com")).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("access-token", response.getData().getAccessToken());
        assertEquals("refresh-token", response.getData().getRefreshToken());
        assertEquals("student@example.com", response.getData().getUser().getEmail());

        verify(userRepository).save(userCaptor.capture());
        assertEquals("hashed-secret", userCaptor.getValue().getPassword());
        assertEquals(User.Role.STUDENT, userCaptor.getValue().getRole());
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User();
        user.setEmail("student@example.com");
        user.setPassword("hashed-secret");
        user.setRole(User.Role.STUDENT);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-secret")).thenReturn(false);

        AuthResponse response = authService.login(new LoginRequest("student@example.com", "wrong"));

        assertFalse(response.isSuccess());
        assertEquals("AUTH-001", response.getError().getCode());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
    }
}