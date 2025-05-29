package org.kuraterut.controller;

import jakarta.validation.Valid;
import org.kuraterut.model.dto.AuthResponse;
import org.kuraterut.model.dto.ConfirmationRequest;
import org.kuraterut.model.dto.RegistrationRequest;
import org.kuraterut.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest request) {
        authService.registerUser(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<AuthResponse> confirm(@RequestBody @Valid ConfirmationRequest request) {
        String token = authService.confirmUser(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/secure")
    public ResponseEntity<String> secureEndpoint() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok("Secure endpoint. Authenticated as: " + email);
    }
}