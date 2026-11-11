package com.mediator.auth.controller;

import com.mediator.auth.dto.request.LoginRequest;
import com.mediator.auth.dto.request.RegisterStudentRequest;
import com.mediator.auth.dto.request.RegisterTutorRequest;
import com.mediator.auth.dto.response.LoginResponse;
import com.mediator.auth.service.AuthService;
import com.mediator.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Student/tutor registration, login and logout")
public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping("/register/student")
        @SecurityRequirements
        @Operation(summary = "Register a student", description = "Creates a student account and returns a JWT token with user details.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student registered successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or mobile number already registered", content = @Content)
        })
        public ResponseEntity<ApiResponse<LoginResponse>> registerStudent(
                        @Valid @RequestBody RegisterStudentRequest request) {
                LoginResponse response = authService.registerStudent(request);
                return ResponseEntity.ok(ApiResponse.success("Student registered successfully.", response));
        }

        @PostMapping("/register/tutor")
        @SecurityRequirements
        @Operation(summary = "Register a tutor", description = "Creates a tutor account with PENDING verification and DRAFT profile status.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutor registered successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or mobile number already registered", content = @Content)
        })
        public ResponseEntity<ApiResponse<LoginResponse>> registerTutor(
                        @Valid @RequestBody RegisterTutorRequest request) {
                LoginResponse response = authService.registerTutor(request);
                return ResponseEntity.ok(ApiResponse.success("Tutor registered successfully.", response));
        }

        @PostMapping("/login")
        @SecurityRequirements
        @Operation(summary = "Login", description = "Authenticates with email and password and returns a JWT Bearer token.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content)
        })
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request) {
                LoginResponse response = authService.login(request);
                return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout", description = "Returns a logout acknowledgement. JWT is stateless; the client must discard its token. This endpoint does not revoke the token.")
        @SecurityRequirement(name = "Bearer Authentication")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout acknowledgement returned"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
        })
        public ResponseEntity<ApiResponse<Void>> logout() {
                return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", null));
        }
}
