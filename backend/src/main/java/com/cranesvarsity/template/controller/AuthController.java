package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.LoginRequest;
import com.cranesvarsity.template.dto.LoginResponse;
import com.cranesvarsity.template.service.AuthException;
import com.cranesvarsity.template.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Real login against the legacy "admission" table.
 *
 *   POST http://localhost:8181/api/auth/login
 *   body: { "email": "student@example.com", "password": "..." }
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please enter your email address"));
        }
        if (request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please enter your password"));
        }

        try {
            LoginResponse response = authService.login(request.email(), request.password(), httpRequest);
            return ResponseEntity.ok(response);
        } catch (AuthException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
        }
    }
}
