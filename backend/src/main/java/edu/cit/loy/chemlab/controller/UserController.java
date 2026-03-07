package edu.cit.loy.chemlab.controller;

import edu.cit.loy.chemlab.dto.AuthResponse;
import edu.cit.loy.chemlab.dto.UserDTO;
import edu.cit.loy.chemlab.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401)
                    .body(AuthResponse.error("AUTH-001", "Not authenticated", "No valid session found"));
        }

        UserDTO user = userService.getCurrentUser(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(AuthResponse.error("AUTH-001", "User not found", "Authenticated user no longer exists"));
        }

        return ResponseEntity.ok(user);
    }
}
