package com.mediator.tutor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.common.base.Gender;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.GlobalExceptionHandler;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.tutor.dto.academics.AcademicProfileDto;
import com.mediator.tutor.dto.dashboard.TutorDashboardResponse;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorDocumentsRequest;
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.request.TutorTeachingPreferenceRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.DocumentType;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.service.TutorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvc tests for TutorController.
 *
 * Covered endpoints:
 *
 * 1. GET /api/v1/tutors/profile
 * 2. PUT /api/v1/tutors/profile
 * 3. POST /api/v1/tutors/preferences
 * 4. GET /api/v1/tutors/preferences
 * 5. POST /api/v1/tutors/documents
 * 6. GET /api/v1/tutors/documents
 * 7. DELETE /api/v1/tutors/documents/{documentId}
 * 8. GET /api/v1/tutors/dashboard
 */
@WebMvcTest(controllers = TutorController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.mediator.auth.config.SecurityConfig.class,
                com.mediator.auth.security.JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TutorControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TutorService tutorService;

        private Principal principal;

        // ============================================================
        // SETUP
        // ============================================================

        @BeforeEach
        void setUp() {

                principal = new UsernamePasswordAuthenticationToken(
                                "tutor@test.com",
                                null);

                SecurityContextHolder.clearContext();
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        // ============================================================
        // HELPER METHODS
        // ============================================================

        private TutorProfileRequest createValidProfileRequest() {

                return TutorProfileRequest.builder()
                                .gender(Gender.MALE)
                                .description("Experienced Mathematics and Physics tutor")
                                .teachingExperienceYears(5)
                                .industryExperienceYears(2)
                                .currentOccupation("Full-time Tutor")
                                .preferredRadius(10.0)
                                .salaryMin(BigDecimal.valueOf(500))
                                .salaryMax(BigDecimal.valueOf(1000))

                                .address(
                                                AddressDto.builder()
                                                                .street("Park Street")
                                                                .city("Kolkata")
                                                                .district("Kolkata")
                                                                .state("West Bengal")
                                                                .zipCode("700016")
                                                                .latitude(22.572)
                                                                .longitude(88.352)
                                                                .build())

                                .preferredMode(
                                                PreferredModeDto.builder()
                                                                .online(true)
                                                                .studentHome(true)
                                                                .tutorHome(false)
                                                                .build())

                                .academicProfile(
                                                AcademicProfileDto.builder()
                                                                .class10Percentage(85.0)
                                                                .class12Percentage(80.0)
                                                                .degreeName("B.Sc")
                                                                .instituteName("University of Calcutta")
                                                                .passingYear(2020)
                                                                .build())

                                .build();
        }

        private TutorTeachingPreferenceRequest createValidPreferenceRequest() {

                return TutorTeachingPreferenceRequest.builder()
                                .preferences(
                                                List.of(
                                                                TutorTeachingPreferenceDto.builder()
                                                                                .boardId(1L)
                                                                                .classLevelId(8L)
                                                                                .subjectId(1L)
                                                                                .build()))
                                .build();
        }

        private TutorDocumentsRequest createValidDocumentsRequest() {

                return TutorDocumentsRequest.builder()
                                .documents(
                                                List.of(
                                                                TutorDocumentDto.builder()
                                                                                .documentType(DocumentType.AADHAAR_CARD)
                                                                                .documentUrl("https://example.com/aadhaar.pdf")
                                                                                .build()))
                                .build();
        }

        // ============================================================
        // GET PROFILE
        // ============================================================

        @Test
        void getProfile_shouldReturnTutorProfile() throws Exception {

                TutorProfileResponse response = TutorProfileResponse.builder()
                                .tutorId(1L)
                                .firstName("John")
                                .lastName("Doe")
                                .email("tutor@test.com")
                                .verificationStatus(VerificationStatus.APPROVED)
                                .profileStatus(ProfileStatus.LIVE)
                                .profileCompletion(90)
                                .build();

                when(tutorService.getProfile("tutor@test.com"))
                                .thenReturn(response);

                mockMvc.perform(
                                get("/api/v1/tutors/profile")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile fetched successfully."))
                                .andExpect(jsonPath("$.data.tutorId").value(1L))
                                .andExpect(jsonPath("$.data.firstName").value("John"))
                                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                                .andExpect(jsonPath("$.data.email")
                                                .value("tutor@test.com"))
                                .andExpect(jsonPath("$.data.verificationStatus")
                                                .value("APPROVED"))
                                .andExpect(jsonPath("$.data.profileStatus")
                                                .value("LIVE"))
                                .andExpect(jsonPath("$.data.profileCompletion")
                                                .value(90));

                verify(tutorService)
                                .getProfile("tutor@test.com");
        }

        @Test
        void getProfile_whenTutorNotFound_shouldReturn404() throws Exception {

                when(tutorService.getProfile("tutor@test.com"))
                                .thenThrow(
                                                new ResourceNotFoundException(
                                                                "Tutor profile not found."));

                mockMvc.perform(
                                get("/api/v1/tutors/profile")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile not found."));

                verify(tutorService)
                                .getProfile("tutor@test.com");
        }

        // ============================================================
        // UPDATE PROFILE
        // ============================================================

        @Test
        void updateProfile_withValidData_shouldReturnUpdatedProfile()
                        throws Exception {

                TutorProfileRequest request = createValidProfileRequest();

                TutorProfileResponse response = TutorProfileResponse.builder()
                                .tutorId(1L)
                                .firstName("John")
                                .lastName("Doe")
                                .email("tutor@test.com")
                                .description(request.getDescription())
                                .verificationStatus(
                                                VerificationStatus.APPROVED)
                                .profileStatus(ProfileStatus.LIVE)
                                .build();

                when(
                                tutorService.updateProfile(
                                                eq("tutor@test.com"),
                                                any(TutorProfileRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(
                                put("/api/v1/tutors/profile")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile updated successfully."))
                                .andExpect(jsonPath("$.data.tutorId").value(1L))
                                .andExpect(jsonPath("$.data.email")
                                                .value("tutor@test.com"))
                                .andExpect(jsonPath("$.data.description")
                                                .value("Experienced Mathematics and Physics tutor"));

                verify(tutorService)
                                .updateProfile(
                                                eq("tutor@test.com"),
                                                any(TutorProfileRequest.class));
        }

        @Test
        void updateProfile_withInvalidData_shouldReturnBadRequest()
                        throws Exception {

                TutorProfileRequest invalidRequest = TutorProfileRequest.builder()
                                .description("")
                                .gender(null)
                                .build();

                mockMvc.perform(
                                put("/api/v1/tutors/profile")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))
                                .andExpect(jsonPath("$.errors.gender").exists())
                                .andExpect(jsonPath("$.errors.description").exists());
        }

        @Test
        void updateProfile_whenServiceThrowsBadRequest_shouldReturn400()
                        throws Exception {

                TutorProfileRequest request = createValidProfileRequest();

                when(
                                tutorService.updateProfile(
                                                eq("tutor@test.com"),
                                                any(TutorProfileRequest.class)))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Minimum salary cannot be greater than maximum salary"));

                mockMvc.perform(
                                put("/api/v1/tutors/profile")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Minimum salary cannot be greater than maximum salary"));

                verify(tutorService)
                                .updateProfile(
                                                eq("tutor@test.com"),
                                                any(TutorProfileRequest.class));
        }

        // ============================================================
        // UPDATE TEACHING PREFERENCES
        // ============================================================

        @Test
        void updateTeachingPreferences_withValidData_shouldReturnSuccess()
                        throws Exception {

                TutorTeachingPreferenceRequest request = createValidPreferenceRequest();

                mockMvc.perform(
                                post("/api/v1/tutors/preferences")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Teaching preferences updated successfully."))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(tutorService)
                                .updateTeachingPreferences(
                                                eq("tutor@test.com"),
                                                any(TutorTeachingPreferenceRequest.class));
        }

        @Test
        void updateTeachingPreferences_withEmptyPreferences_shouldReturnBadRequest()
                        throws Exception {

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of())
                                .build();

                mockMvc.perform(
                                post("/api/v1/tutors/preferences")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."));
        }

        @Test
        void updateTeachingPreferences_whenServiceThrowsBadRequest_shouldReturn400()
                        throws Exception {

                TutorTeachingPreferenceRequest request = createValidPreferenceRequest();

                doThrow(
                                new BadRequestException(
                                                "Invalid teaching preference."))
                                .when(tutorService)
                                .updateTeachingPreferences(
                                                eq("tutor@test.com"),
                                                any(TutorTeachingPreferenceRequest.class));

                mockMvc.perform(
                                post("/api/v1/tutors/preferences")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Invalid teaching preference."));

                verify(tutorService)
                                .updateTeachingPreferences(
                                                eq("tutor@test.com"),
                                                any(TutorTeachingPreferenceRequest.class));
        }

        // ============================================================
        // GET TEACHING PREFERENCES
        // ============================================================

        @Test
        void getTeachingPreferences_shouldReturnPreferencesList()
                        throws Exception {

                List<TutorTeachingPreferenceDto> dtoList = List.of(
                                TutorTeachingPreferenceDto.builder()
                                                .boardId(1L)
                                                .classLevelId(8L)
                                                .subjectId(1L)
                                                .build());

                when(
                                tutorService.getTeachingPreferences(
                                                "tutor@test.com"))
                                .thenReturn(dtoList);

                mockMvc.perform(
                                get("/api/v1/tutors/preferences")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Teaching preferences fetched successfully."))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].boardId").value(1L))
                                .andExpect(jsonPath("$.data[0].classLevelId").value(8L))
                                .andExpect(jsonPath("$.data[0].subjectId").value(1L));

                verify(tutorService)
                                .getTeachingPreferences("tutor@test.com");
        }

        @Test
        void getTeachingPreferences_whenNoPreferences_shouldReturnEmptyList()
                        throws Exception {

                when(
                                tutorService.getTeachingPreferences(
                                                "tutor@test.com"))
                                .thenReturn(List.of());

                mockMvc.perform(
                                get("/api/v1/tutors/preferences")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(tutorService)
                                .getTeachingPreferences("tutor@test.com");
        }

        @Test
        void getTeachingPreferences_whenTutorNotFound_shouldReturn404()
                        throws Exception {

                when(
                                tutorService.getTeachingPreferences(
                                                "tutor@test.com"))
                                .thenThrow(
                                                new ResourceNotFoundException(
                                                                "Tutor profile not found."));

                mockMvc.perform(
                                get("/api/v1/tutors/preferences")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile not found."));

                verify(tutorService)
                                .getTeachingPreferences("tutor@test.com");
        }

        // ============================================================
        // UPLOAD DOCUMENTS
        // ============================================================

        @Test
        void uploadDocuments_withValidData_shouldReturnCreated()
                        throws Exception {

                TutorDocumentsRequest request = createValidDocumentsRequest();

                mockMvc.perform(
                                post("/api/v1/tutors/documents")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Documents uploaded successfully."))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(tutorService)
                                .uploadDocuments(
                                                eq("tutor@test.com"),
                                                anyList());
        }

        @Test
        void uploadDocuments_withEmptyList_shouldReturnBadRequest()
                        throws Exception {

                TutorDocumentsRequest request = TutorDocumentsRequest.builder()
                                .documents(List.of())
                                .build();

                mockMvc.perform(
                                post("/api/v1/tutors/documents")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."));
        }

        @Test
        void uploadDocuments_withInvalidDocument_shouldReturnBadRequest()
                        throws Exception {

                TutorDocumentsRequest request = TutorDocumentsRequest.builder()
                                .documents(
                                                List.of(
                                                                TutorDocumentDto.builder()
                                                                                .documentType(null)
                                                                                .documentUrl("")
                                                                                .build()))
                                .build();

                mockMvc.perform(
                                post("/api/v1/tutors/documents")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."));
        }

        @Test
        void uploadDocuments_whenDuplicateDocumentType_shouldReturn400()
                        throws Exception {

                TutorDocumentsRequest request = TutorDocumentsRequest.builder()
                                .documents(
                                                List.of(
                                                                TutorDocumentDto.builder()
                                                                                .documentType(
                                                                                                DocumentType.AADHAAR_CARD)
                                                                                .documentUrl(
                                                                                                "https://example.com/doc1.pdf")
                                                                                .build(),

                                                                TutorDocumentDto.builder()
                                                                                .documentType(
                                                                                                DocumentType.AADHAAR_CARD)
                                                                                .documentUrl(
                                                                                                "https://example.com/doc2.pdf")
                                                                                .build()))
                                .build();

                doThrow(
                                new BadRequestException(
                                                "Duplicate document type."))
                                .when(tutorService)
                                .uploadDocuments(
                                                eq("tutor@test.com"),
                                                anyList());

                mockMvc.perform(
                                post("/api/v1/tutors/documents")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Duplicate document type."));

                verify(tutorService)
                                .uploadDocuments(
                                                eq("tutor@test.com"),
                                                anyList());
        }

        // ============================================================
        // GET DOCUMENTS
        // ============================================================

        @Test
        void getDocuments_shouldReturnDocumentsList()
                        throws Exception {

                List<TutorDocumentDto> documents = List.of(
                                TutorDocumentDto.builder()
                                                .documentId(10L)
                                                .documentType(DocumentType.AADHAAR_CARD)
                                                .documentUrl(
                                                                "https://example.com/doc.pdf")
                                                .verificationStatus(
                                                                VerificationStatus.PENDING)
                                                .build());

                when(
                                tutorService.getDocuments("tutor@test.com")).thenReturn(documents);

                mockMvc.perform(
                                get("/api/v1/tutors/documents")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Documents fetched successfully."))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].documentId")
                                                .value(10L))
                                .andExpect(jsonPath("$.data[0].documentType")
                                                .value("AADHAAR_CARD"))
                                .andExpect(jsonPath("$.data[0].verificationStatus")
                                                .value("PENDING"));

                verify(tutorService)
                                .getDocuments("tutor@test.com");
        }

        @Test
        void getDocuments_whenNoDocuments_shouldReturnEmptyList()
                        throws Exception {

                when(
                                tutorService.getDocuments("tutor@test.com")).thenReturn(List.of());

                mockMvc.perform(
                                get("/api/v1/tutors/documents")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(tutorService)
                                .getDocuments("tutor@test.com");
        }

        @Test
        void getDocuments_whenTutorNotFound_shouldReturn404()
                        throws Exception {

                when(
                                tutorService.getDocuments("tutor@test.com")).thenThrow(
                                                new ResourceNotFoundException(
                                                                "Tutor profile not found."));

                mockMvc.perform(
                                get("/api/v1/tutors/documents")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile not found."));

                verify(tutorService)
                                .getDocuments("tutor@test.com");
        }

        // ============================================================
        // DELETE DOCUMENT
        // ============================================================

        @Test
        void deleteDocument_shouldReturnSuccess()
                        throws Exception {

                mockMvc.perform(
                                delete("/api/v1/tutors/documents/10")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Document deleted successfully."))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(tutorService)
                                .deleteDocument("tutor@test.com", 10L);
        }

        @Test
        void deleteDocument_whenNotFound_shouldReturn404()
                        throws Exception {

                doThrow(
                                new ResourceNotFoundException(
                                                "Document not found."))
                                .when(tutorService)
                                .deleteDocument("tutor@test.com", 999L);

                mockMvc.perform(
                                delete("/api/v1/tutors/documents/999")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Document not found."));

                verify(tutorService)
                                .deleteDocument("tutor@test.com", 999L);
        }

        @Test
        void deleteDocument_whenBadRequest_shouldReturn400()
                        throws Exception {

                doThrow(
                                new BadRequestException(
                                                "Document cannot be deleted."))
                                .when(tutorService)
                                .deleteDocument("tutor@test.com", 10L);

                mockMvc.perform(
                                delete("/api/v1/tutors/documents/10")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Document cannot be deleted."));

                verify(tutorService)
                                .deleteDocument("tutor@test.com", 10L);
        }

        // ============================================================
        // DASHBOARD
        // ============================================================

        @Test
        void getDashboard_shouldReturnDashboard()
                        throws Exception {

                TutorDashboardResponse dashboard = TutorDashboardResponse.builder()
                                .verificationStatus(
                                                VerificationStatus.APPROVED)
                                .totalMatchRequests(5L)
                                .activeStudents(2L)
                                .build();

                when(
                                tutorService.getDashboard("tutor@test.com")).thenReturn(dashboard);

                mockMvc.perform(
                                get("/api/v1/tutors/dashboard")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Dashboard fetched successfully."))
                                .andExpect(jsonPath("$.data.verificationStatus")
                                                .value("APPROVED"))
                                .andExpect(jsonPath("$.data.totalMatchRequests")
                                                .value(5))
                                .andExpect(jsonPath("$.data.activeStudents")
                                                .value(2));

                verify(tutorService)
                                .getDashboard("tutor@test.com");
        }

        @Test
        void getDashboard_whenTutorNotFound_shouldReturn404()
                        throws Exception {

                when(
                                tutorService.getDashboard("tutor@test.com")).thenThrow(
                                                new ResourceNotFoundException(
                                                                "Tutor profile not found."));

                mockMvc.perform(
                                get("/api/v1/tutors/dashboard")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor profile not found."));

                verify(tutorService)
                                .getDashboard("tutor@test.com");
        }
}