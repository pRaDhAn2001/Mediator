package com.mediator.matching.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mediator.common.dto.ApiResponse;
import com.mediator.matching.dto.request.CreateMatchRequest;
import com.mediator.matching.dto.request.TutorActionRequest;
import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.service.MatchRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Validated
@Tag(name = "Match Requests", description = "APIs for creating and managing student-tutor match requests")
@SecurityRequirement(name = "Bearer Authentication")
public class MatchRequestController {

        private final MatchRequestService matchRequestService;

        @PostMapping
        @Operation(summary = "Create a match request", description = "Allows a student to send a match request to a tutor for selected subjects.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Match request created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not allowed to create this request")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> createRequest(
                        @Parameter(hidden = true) Authentication authentication,
                        @Valid @RequestBody CreateMatchRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                "Request sent successfully.",
                                                matchRequestService.createRequest(authentication.getName(), request)));
        }

        @GetMapping("/student")
        @Operation(summary = "Get student match requests", description = "Retrieves match requests associated with the authenticated student.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student requests fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
        })
        public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> studentRequests(
                        @Parameter(hidden = true) Authentication authentication) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Student requests fetched successfully.",
                                matchRequestService.getStudentRequests(authentication.getName())));
        }

        @GetMapping("/tutor")
        @Operation(summary = "Get tutor match requests", description = "Retrieves match requests associated with the authenticated tutor.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutor requests fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
        })
        public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> tutorRequests(
                        @Parameter(hidden = true) Authentication authentication) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Tutor requests fetched successfully.",
                                matchRequestService.getTutorRequests(authentication.getName())));
        }

        @GetMapping("/{requestId}")
        @Operation(summary = "Get match request details", description = "Retrieves the details of a specific match request.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request details fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request ID"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match request not found")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> details(
                        @Parameter(hidden = true) Authentication authentication,
                        @Parameter(description = "ID of the match request", example = "1") @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Request details fetched successfully.",
                                matchRequestService.getRequestDetails(authentication.getName(), requestId)));
        }

        @PutMapping("/{requestId}/cancel")
        @Operation(summary = "Cancel a match request", description = "Cancels a match request using its request ID.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request cancelled successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request ID or cancellation not permitted"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match request not found")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> cancel(
                        @Parameter(hidden = true) Authentication authentication,
                        @Parameter(description = "ID of the match request to cancel", example = "1") @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Request cancelled successfully.",
                                matchRequestService.cancelRequest(authentication.getName(), requestId)));
        }

        @PutMapping("/{requestId}/connect")
        @Operation(summary = "Connect to a student request", description = "Allows the tutor to connect to a student's match request.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student request connected successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request cannot be connected in its current state"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match request not found")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> connect(
                        @Parameter(hidden = true) Authentication authentication,
                        @Parameter(description = "ID of the match request", example = "1") @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Student request connected successfully.",
                                matchRequestService.connectStudent(authentication.getName(), requestId)));
        }

        @PutMapping("/{requestId}/partial-finalize")
        @Operation(summary = "Partially finalize a match request", description = "Allows the tutor to accept selected subjects from a match request.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request partially finalized successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid subject selection or request state"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match request not found")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> partialFinalize(
                        @Parameter(hidden = true) Authentication authentication,
                        @Parameter(description = "ID of the match request", example = "1") @PathVariable @Positive Long requestId,
                        @Valid @RequestBody TutorActionRequest action) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Request partially finalized successfully.",
                                matchRequestService.partiallyFinalize(authentication.getName(), requestId, action)));
        }

        @PutMapping("/{requestId}/finalize")
        @Operation(summary = "Finalize a match request", description = "Finalizes the match request.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request finalized successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request cannot be finalized in its current state"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match request not found")
        })
        public ResponseEntity<ApiResponse<MatchRequestResponse>> finalizeRequest(
                        @Parameter(hidden = true) Authentication authentication,
                        @Parameter(description = "ID of the match request", example = "1") @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Request finalized successfully.",
                                matchRequestService.finalizeRequest(authentication.getName(), requestId)));
        }
}