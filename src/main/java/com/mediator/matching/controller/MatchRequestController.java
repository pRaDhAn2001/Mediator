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

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Validated
public class MatchRequestController {

        private final MatchRequestService matchRequestService;

        @PostMapping
        public ResponseEntity<ApiResponse<MatchRequestResponse>> createRequest(
                        Authentication authentication,
                        @Valid @RequestBody CreateMatchRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Request sent successfully.",
                                                                matchRequestService.createRequest(
                                                                                authentication.getName(),
                                                                                request)));
        }

        @GetMapping("/student")
        public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> studentRequests(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student requests fetched successfully.",
                                                matchRequestService.getStudentRequests(
                                                                authentication.getName())));
        }

        @GetMapping("/tutor")
        public ResponseEntity<ApiResponse<List<MatchRequestResponse>>> tutorRequests(
                        Authentication authentication) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutor requests fetched successfully.",
                                                matchRequestService.getTutorRequests(
                                                                authentication.getName())));
        }

        @GetMapping("/{requestId}")
        public ResponseEntity<ApiResponse<MatchRequestResponse>> details(
                        Authentication authentication,
                        @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Request details fetched successfully.",
                                                matchRequestService.getRequestDetails(
                                                                authentication.getName(),
                                                                requestId)));
        }

        @PutMapping("/{requestId}/cancel")
        public ResponseEntity<ApiResponse<MatchRequestResponse>> cancel(
                        Authentication authentication,
                        @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Request cancelled successfully.",
                                                matchRequestService.cancelRequest(
                                                                authentication.getName(),
                                                                requestId)));
        }

        @PutMapping("/{requestId}/connect")
        public ResponseEntity<ApiResponse<MatchRequestResponse>> connect(
                        Authentication authentication,
                        @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Student request connected successfully.",
                                                matchRequestService.connectStudent(
                                                                authentication.getName(),
                                                                requestId)));
        }

        @PutMapping("/{requestId}/partial-finalize")
        public ResponseEntity<ApiResponse<MatchRequestResponse>> partialFinalize(
                        Authentication authentication,
                        @PathVariable @Positive Long requestId,
                        @Valid @RequestBody TutorActionRequest action) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Request partially finalized successfully.",
                                                matchRequestService.partiallyFinalize(
                                                                authentication.getName(),
                                                                requestId,
                                                                action)));
        }

        @PutMapping("/{requestId}/finalize")
        public ResponseEntity<ApiResponse<MatchRequestResponse>> finalizeRequest(
                        Authentication authentication,
                        @PathVariable @Positive Long requestId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Request finalized successfully.",
                                                matchRequestService.finalizeRequest(
                                                                authentication.getName(),
                                                                requestId)));
        }
}