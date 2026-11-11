package com.mediator.student.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediator.common.dto.ApiResponse;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.student.dto.dashboard.StudentDashboardResponse;
import com.mediator.student.dto.preference.StudentPreferenceDto;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
@Tag(name = "Student", description = "Student profile, dashboard, and subject preference APIs.")
@SecurityRequirement(name = "Bearer Authentication")
public class StudentController {

        private final StudentService studentService;

        /**
         * Get logged-in student profile.
         */
        @Operation(summary = "Get student profile", description = "Returns the profile of the currently authenticated student.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student profile fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/profile")
        public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile() {

                StudentProfileResponse response = studentService.getProfile();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student profile fetched successfully.",
                                                response));
        }

        /**
         * Update student profile.
         */
        @Operation(summary = "Create or update student profile", description = "Creates or updates the profile of the currently authenticated student. "
                        + "Address and preferred-mode objects are validated recursively.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student profile updated successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request validation failed.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "A referenced class level, board, or student resource was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @PutMapping("/profile")
        public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Student profile information.", required = true, content = @Content(schema = @Schema(implementation = StudentProfileRequest.class))) @Valid @RequestBody StudentProfileRequest request) {

                StudentProfileResponse response = studentService.updateProfile(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student profile updated successfully.",
                                                response));
        }

        /**
         * Student dashboard.
         */
        @Operation(summary = "Get student dashboard", description = "Returns the logged-in student's profile summary and match counts.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/dashboard")
        public ResponseEntity<ApiResponse<StudentDashboardResponse>> getDashboard() {

                StudentDashboardResponse response = studentService.getDashboard();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Dashboard fetched successfully.",
                                                response));
        }

        /**
         * Update preferred subjects.
         */
        @Operation(summary = "Update preferred subjects", description = "Replaces the currently authenticated student's preferred subject selection. "
                        + "At least one subject ID must be supplied.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student preferences updated successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No subjects supplied or request validation failed.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student or one or more subjects were not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @PutMapping("/preferences")
        public ResponseEntity<ApiResponse<List<SubjectResponse>>> updatePreferences(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of subject IDs to set as the student's preferences.", required = true, content = @Content(schema = @Schema(implementation = StudentPreferenceDto.class))) @Valid @RequestBody StudentPreferenceDto request) {

                List<SubjectResponse> response = studentService.updatePreferences(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student preferences updated successfully.",
                                                response));
        }

        /**
         * Get preferred subjects.
         */
        @Operation(summary = "Get preferred subjects", description = "Returns the preferred subjects configured by the currently authenticated student.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student preferences fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/preferences")
        public ResponseEntity<ApiResponse<List<SubjectResponse>>> getPreferences() {

                List<SubjectResponse> response = studentService.getPreferences();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student preferences fetched successfully.",
                                                response));
        }
}