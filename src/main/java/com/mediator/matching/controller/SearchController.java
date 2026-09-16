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

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

        private final MatchService matchService;

        // ============================================================
        // SEARCH TUTORS
        // ============================================================

        @PostMapping("/tutors")
        public ResponseEntity<ApiResponse<TutorSearchResponse>> searchTutors(
                        @Valid @RequestBody TutorSearchRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutors fetched successfully.",
                                                matchService.searchTutors(
                                                                request)));
        }

        // ============================================================
        // GET TUTOR DETAILS
        // ============================================================

        @GetMapping("/tutors/{tutorId}")
        public ResponseEntity<ApiResponse<TutorDetailsResponse>> getTutorDetails(
                        @PathVariable Long tutorId) {

                /*
                 * Replace this with however your application currently
                 * obtains the logged-in user's email.
                 *
                 * If your project already has SecurityContext-based
                 * authentication, use that implementation here.
                 */
                String email = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutor details fetched successfully.",
                                                matchService.getTutorDetails(
                                                                email,
                                                                tutorId)));
        }
}