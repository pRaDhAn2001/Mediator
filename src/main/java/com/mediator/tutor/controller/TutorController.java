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
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorTeachingPreferenceRequest;
import com.mediator.tutor.service.TutorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Validated
public class TutorController {

        private final TutorService tutorService;

        /**
         * Get Tutor Profile
         */
        @GetMapping("/profile")
        public ResponseEntity<ApiResponse<TutorProfileResponse>> getProfile(
                        Principal principal) {

                TutorProfileResponse response = tutorService.getProfile(principal.getName());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tutor profile fetched successfully.",
                                                response));
        }

        /**
         * Update Tutor Profile
         */
        @PutMapping("/profile")
        public ResponseEntity<ApiResponse<TutorProfileResponse>> updateProfile(
                        Principal principal,
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
        @PostMapping("/preferences")
        public ResponseEntity<ApiResponse<Void>> updateTeachingPreferences(
                        Principal principal,
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
        @GetMapping("/preferences")
        public ResponseEntity<ApiResponse<List<TutorTeachingPreferenceDto>>> getTeachingPreferences(
                        Principal principal) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Teaching preferences fetched successfully.",
                                                tutorService.getTeachingPreferences(
                                                                principal.getName())));
        }

        /**
         * Upload Document
         */
        @PostMapping("/documents")
        public ResponseEntity<ApiResponse<Void>> uploadDocument(
                        Principal principal,
                        @Valid @RequestBody TutorDocumentDto request) {

                tutorService.uploadDocument(
                                principal.getName(),
                                request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                "Document uploaded successfully.",
                                                null));
        }

        /**
         * Get Documents
         */
        @GetMapping("/documents")
        public ResponseEntity<ApiResponse<List<TutorDocumentDto>>> getDocuments(
                        Principal principal) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Documents fetched successfully.",
                                                tutorService.getDocuments(
                                                                principal.getName())));
        }

        /**
         * Delete Document
         */
        @DeleteMapping("/documents/{documentId}")
        public ResponseEntity<ApiResponse<Void>> deleteDocument(
                        Principal principal,
                        @PathVariable Long documentId) {

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
        @GetMapping("/dashboard")
        public ResponseEntity<ApiResponse<TutorDashboardResponse>> getDashboard(
                        Principal principal) {

                TutorDashboardResponse response = tutorService.getDashboard(
                                principal.getName());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Dashboard fetched successfully.",
                                                response));
        }
}
