package com.mediator.auth.controller;

import com.mediator.auth.dto.request.LoginRequest;
import com.mediator.auth.dto.request.RegisterStudentRequest;
import com.mediator.auth.dto.request.RegisterTutorRequest;
import com.mediator.auth.dto.response.LoginResponse;
import com.mediator.auth.service.AuthService;
import com.mediator.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<LoginResponse>> registerStudent(
            @Valid @RequestBody RegisterStudentRequest request) {

        LoginResponse response = authService.registerStudent(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student registered successfully.",
                        response));
    }

    @PostMapping("/register/tutor")
    public ResponseEntity<ApiResponse<LoginResponse>> registerTutor(
            @Valid @RequestBody RegisterTutorRequest request) {

        LoginResponse response = authService.registerTutor(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tutor registered successfully.",
                        response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful.",
                        response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {

        // JWT is stateless.
        // Client simply removes the token.
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out successfully.",
                        null));
    }
}