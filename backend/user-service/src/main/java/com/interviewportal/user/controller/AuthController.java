package com.interviewportal.user.controller;

import com.interviewportal.user.dto.AuthResponse;
import com.interviewportal.user.dto.GoogleLoginRequest;
import com.interviewportal.user.dto.LoginRequest;
import com.interviewportal.user.dto.RefreshTokenRequest;
import com.interviewportal.user.dto.SignupRequest;
import com.interviewportal.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints. All are unauthenticated by definition (they mint credentials).
 *
 * <p>Controllers stay thin: validate input ({@code @Valid}), delegate to the service, and wrap the
 * result in a {@link ResponseEntity} with the correct HTTP status. No business logic here.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup, login, Google login, token refresh and logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new local account and receive a token pair")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with username/email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    @Operation(summary = "Authenticate with a Google ID token obtained in the browser")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Exchange a valid refresh token for a new access/refresh pair")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
