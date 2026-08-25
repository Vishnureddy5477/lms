package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ForgotPasswordResetRequest;
import com.cranesvarsity.template.dto.ForgotPasswordSendOtpRequest;
import com.cranesvarsity.template.dto.ForgotPasswordVerifyOtpRequest;
import com.cranesvarsity.template.service.ForgotPasswordException;
import com.cranesvarsity.template.service.ForgotPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public (no JWT) — mirrors legacy login.jsp's forgot-password modal, reachable before authentication. */
@RestController
@RequestMapping("/auth/forgot-password")
public class ForgotPasswordController {

    private final ForgotPasswordService service;

    public ForgotPasswordController(ForgotPasswordService service) {
        this.service = service;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody ForgotPasswordSendOtpRequest request) {
        try {
            service.sendOtp(request.email());
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully to your email"));
        } catch (ForgotPasswordException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody ForgotPasswordVerifyOtpRequest request) {
        try {
            service.verifyOtp(request.email(), request.otp());
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
        } catch (ForgotPasswordException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ForgotPasswordResetRequest request) {
        try {
            service.resetPassword(request.email(), request.newPassword(), request.confirmPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (ForgotPasswordException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
