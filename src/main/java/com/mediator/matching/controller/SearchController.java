package com.mediator.matching.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.mediator.common.dto.ApiResponse;
import com.mediator.matching.dto.request.TutorSearchRequest;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorSearchResponse;
import com.mediator.matching.service.MatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tutor Search", description = "APIs for searching tutors and viewing tutor details")
public class SearchController {

        private final MatchService matchService;

        @PostMapping("/tutors")
        @Operation(summary = "Search tutors", description = "Searches for tutors based on selected subjects, board, class level, teaching mode and location preferences.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutors fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search criteria")
        })
        public ResponseEntity<ApiResponse<TutorSearchResponse>> searchTutors(
                        @Valid @RequestBody TutorSearchRequest request) {

                return ResponseEntity.ok(ApiResponse.success(
                                "Tutors fetched successfully.",
                                matchService.searchTutors(request)));
        }

        @GetMapping("/tutors/{tutorId}")
        @Operation(summary = "Get tutor details", description = "Retrieves detailed information about a tutor for the authenticated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tutor details fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tutor not found")
        })
        public ResponseEntity<ApiResponse<TutorDetailsResponse>> getTutorDetails(
                        @Parameter(description = "ID of the tutor", example = "1") @PathVariable Long tutorId) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                return ResponseEntity.ok(ApiResponse.success(
                                "Tutor details fetched successfully.",
                                matchService.getTutorDetails(email, tutorId)));
        }
}