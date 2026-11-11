package com.mediator.tutor.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.mediator.common.dto.ApiResponse;
import com.mediator.tutor.dto.dashboard.TutorDashboardResponse;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.dto.request.TutorDocumentsRequest;
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorTeachingPreferenceRequest;
import com.mediator.tutor.service.TutorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tutor", description = "Tutor profile, teaching preferences, document management, and dashboard APIs.")
@SecurityRequirement(name = "Bearer Authentication")
public class TutorController {

        private final TutorService tutorService;

        /**
         * Get Tutor Profile
         */
        @Operation(summary = "Get tutor profile", description = "Fetches the profile of the currently authenticated tutor.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutor profile fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/profile")
        public ResponseEntity<ApiResponse<TutorProfileResponse>> getProfile(
                        @Parameter(hidden = true) Principal principal) {

                TutorProfileResponse response = tutorService.getProfile(principal.getName());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutor profile fetched successfully.",
                                                response));
        }

        /**
         * Update Tutor Profile
         */
        @Operation(summary = "Create or update tutor profile", description = "Creates or updates the authenticated tutor's profile, including academic details, "
                        + "teaching experience, address, preferred teaching modes, salary expectations, and demo video URL.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutor profile updated successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request validation failed.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "A referenced resource or tutor profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @PutMapping("/profile")
        public ResponseEntity<ApiResponse<TutorProfileResponse>> updateProfile(
                        @Parameter(hidden = true) Principal principal,
                        @Valid @RequestBody TutorProfileRequest request) {

                TutorProfileResponse response = tutorService.updateProfile(
                                principal.getName(),
                                request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutor profile updated successfully.",
                                                response));
        }

        /**
         * Update Teaching Preferences
         */
        @Operation(summary = "Update teaching preferences", description = "Updates the authenticated tutor's teaching preferences, including subject, class level, and board selections.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teaching preferences updated successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or empty teaching preferences.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor or one or more referenced master records were not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @PostMapping("/preferences")
        public ResponseEntity<ApiResponse<Void>> updateTeachingPreferences(
                        @Parameter(hidden = true) Principal principal,
                        @Valid @RequestBody TutorTeachingPreferenceRequest request) {

                tutorService.updateTeachingPreferences(
                                principal.getName(),
                                request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Teaching preferences updated successfully.",
                                                null));
        }

        /**
         * Get Teaching Preferences
         */
        @Operation(summary = "Get teaching preferences", description = "Fetches the teaching preferences configured by the authenticated tutor.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teaching preferences fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/preferences")
        public ResponseEntity<ApiResponse<List<TutorTeachingPreferenceDto>>> getTeachingPreferences(
                        @Parameter(hidden = true) Principal principal) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Teaching preferences fetched successfully.",
                                                tutorService.getTeachingPreferences(
                                                                principal.getName())));
        }

        /**
         * Upload Document
         */
        @Operation(summary = "Upload tutor documents", description = "Submits one or more tutor document details. Each document contains a document type and document URL.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Documents uploaded successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid document details or no documents supplied.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true)))
        })
        @PostMapping("/documents")
        public ResponseEntity<ApiResponse<Void>> uploadDocuments(
                        @Parameter(hidden = true) Principal principal,
                        @Valid @RequestBody TutorDocumentsRequest request) {

                tutorService.uploadDocuments(
                                principal.getName(),
                                request.getDocuments());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                "Documents uploaded successfully.",
                                                null));
        }

        /**
         * Get Documents
         */
        @Operation(summary = "Get tutor documents", description = "Fetches all documents submitted by the authenticated tutor, including their verification status and remarks.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/documents")
        public ResponseEntity<ApiResponse<List<TutorDocumentDto>>> getDocuments(
                        @Parameter(hidden = true) Principal principal) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Documents fetched successfully.",
                                                tutorService.getDocuments(
                                                                principal.getName())));
        }

        /**
         * Delete Document
         */
        @Operation(summary = "Delete tutor document", description = "Deletes a document belonging to the currently authenticated tutor using its document ID.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to delete this document.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @DeleteMapping("/documents/{documentId}")
        public ResponseEntity<ApiResponse<Void>> deleteDocument(
                        @Parameter(hidden = true) Principal principal,
                        @Parameter(description = "ID of the document to delete.", example = "12", required = true) @PathVariable Long documentId) {

                tutorService.deleteDocument(
                                principal.getName(),
                                documentId);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Document deleted successfully.",
                                                null));
        }

        /**
         * Tutor Dashboard
         */
        @Operation(summary = "Get tutor dashboard", description = "Fetches the authenticated tutor's dashboard, including profile, match request counts, "
                        + "subscription details, and verification status.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard fetched successfully."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is missing or invalid.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The authenticated user is not permitted to access this resource.", content = @Content(schema = @Schema(hidden = true))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor profile was not found.", content = @Content(schema = @Schema(hidden = true)))
        })
        @GetMapping("/dashboard")
        public ResponseEntity<ApiResponse<TutorDashboardResponse>> getDashboard(
                        @Parameter(hidden = true) Principal principal) {

                TutorDashboardResponse response = tutorService.getDashboard(
                                principal.getName());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Dashboard fetched successfully.",
                                                response));
        }
}