package com.bookstore.auth.controller;

import com.bookstore.auth.service.AuthService;
import com.bookstore.common.dto.AuthResponse;
import com.bookstore.common.dto.LoginRequest;
import com.bookstore.common.dto.SignupRequest;
import com.bookstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("POST /api/auth/signup - Register new user: {}", request.getEmail());
        
        try {
            AuthResponse authResponse = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(authResponse, "User registered successfully")
            );
        } catch (RuntimeException e) {
            log.error("Signup error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Authenticate user and get JWT token
     */
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> signin(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/signin - User login: {}", request.getEmail());
        
        try {
            AuthResponse authResponse = authService.signin(request);
            return ResponseEntity.ok(
                    ApiResponse.success(authResponse, "Login successful")
            );
        } catch (Exception e) {
            log.error("Signin error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Invalid email or password")
            );
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running", "Health check successful")
        );
    }
}

// Made with Bob