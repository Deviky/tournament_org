package com.deviky.Auth_Service.controllers;

import com.deviky.Auth_Service.dto.*;
import com.deviky.Auth_Service.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/player")
    public ResponseEntity<ApiResponse<String>> registerPlayer(@RequestBody RegisterPlayerRequest request, HttpServletRequest http) {
        String appUrl = http.getScheme() + "://" + http.getServerName() + ":5173";
        ApiResponse<String> response = authService.register(request, appUrl);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }

    @PostMapping("/register/organization")
    public ResponseEntity<ApiResponse<String>> registerOrganization(@RequestBody RegisterOrganizationRequest request, HttpServletRequest http) {
        String appUrl = http.getScheme() + "://" + http.getServerName() + ":5173";
        ApiResponse<String> response = authService.register(request, appUrl);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }
    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<AuthResponse>> confirm(@RequestParam String token) {
        ApiResponse<AuthResponse> response = authService.confirmEmailAndLogin(token);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }

    @PostMapping("/resend_confirmation")
    public ResponseEntity<ApiResponse<String>> resendConfirmation(
            @RequestParam String email,
            HttpServletRequest http
    ) {
        try {
            String appUrl = http.getScheme() + "://" + http.getServerName() + ":" + http.getServerPort();
            ApiResponse<String> response = authService.resendConfirmationEmail(email, appUrl);
            return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ApiResponse<>(e.getMessage(), null, true));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.isError() ? 401 : 200).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshRequest request) {
        ApiResponse<AuthResponse> response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.status(response.isError() ? 401 : 200).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody RefreshRequest request
    ) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Missing or invalid Authorization header", null, true));
        }

        String accessToken = bearerToken.substring(7); // убираем "Bearer "
        String refreshToken = request.getRefreshToken();

        ApiResponse<String> response = authService.logout(accessToken, refreshToken);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }

    @PostMapping("/forgot_password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestParam String email,
            HttpServletRequest http
    ) {
        String appUrl = http.getScheme() + "://" + http.getServerName() + ":" + http.getServerPort();
        ApiResponse<String> response = authService.sendPasswordResetEmail(email, appUrl);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }

    @PostMapping("/reset_password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestBody ResetPasswordRequest request
    ) {
        ApiResponse<String> response = authService.resetPassword(token, request);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }

    @PostMapping("/create_moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> createModerator(@RequestBody ModeratorCreateRequest request) {
        ApiResponse<AuthResponse> response = authService.createModerator(request);
        return ResponseEntity.status(response.isError() ? 400 : 200).body(response);
    }
}