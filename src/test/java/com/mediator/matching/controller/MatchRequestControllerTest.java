package com.mediator.matching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.GlobalExceptionHandler;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.matching.dto.request.CreateMatchRequest;
import com.mediator.matching.dto.request.TutorActionRequest;
import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.matching.service.MatchRequestService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchRequestController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.mediator.auth.config.SecurityConfig.class,
                com.mediator.auth.security.JwtAuthenticationFilter.class
}))
@Import(GlobalExceptionHandler.class)
@DisplayName("MatchRequestController WebMvc Tests")
class MatchRequestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private MatchRequestService matchRequestService;

        private final Principal principal = new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null);

        // =========================================================
        // CREATE REQUEST
        // =========================================================

        @Test
        @DisplayName("POST /api/v1/matches - valid request should return 201")
        void createRequest_withValidData_shouldReturn201Created()
                        throws Exception {

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(10L)
                                .subjectIds(List.of(1L, 2L))
                                .message("Need tuition for Math and Physics")
                                .build();

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .tutorId(10L)
                                .studentId(5L)
                                .status(MatchRequestStatus.REQUESTED)
                                .message(request.getMessage())
                                .build();

                when(matchRequestService.createRequest(
                                eq("user@test.com"),
                                any(CreateMatchRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(
                                post("/api/v1/matches")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Request sent successfully."))
                                .andExpect(jsonPath("$.data.requestId")
                                                .value(100L))
                                .andExpect(jsonPath("$.data.status")
                                                .value("REQUESTED"));

                verify(matchRequestService)
                                .createRequest(
                                                eq("user@test.com"),
                                                any(CreateMatchRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/matches - invalid request should return 400")
        void createRequest_withInvalidData_shouldReturn400()
                        throws Exception {

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(null)
                                .subjectIds(List.of())
                                .message("Invalid request")
                                .build();

                mockMvc.perform(
                                post("/api/v1/matches")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))
                                .andExpect(jsonPath("$.errors.tutorId")
                                                .exists())
                                .andExpect(jsonPath("$.errors.subjectIds")
                                                .exists());
        }

        // =========================================================
        // STUDENT REQUESTS
        // =========================================================

        @Test
        @DisplayName("GET /api/v1/matches/student - should return requests")
        void studentRequests_shouldReturnRequestsList()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .studentId(5L)
                                .tutorId(10L)
                                .status(MatchRequestStatus.REQUESTED)
                                .build();

                when(matchRequestService.getStudentRequests(
                                "user@test.com"))
                                .thenReturn(List.of(response));

                mockMvc.perform(
                                get("/api/v1/matches/student")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Student requests fetched successfully."))
                                .andExpect(jsonPath("$.data")
                                                .isArray())
                                .andExpect(jsonPath("$.data.length()")
                                                .value(1))
                                .andExpect(jsonPath("$.data[0].requestId")
                                                .value(100L));

                verify(matchRequestService)
                                .getStudentRequests("user@test.com");
        }

        @Test
        @DisplayName("GET /api/v1/matches/student - empty list should return 200")
        void studentRequests_whenEmpty_shouldReturnEmptyList()
                        throws Exception {

                when(matchRequestService.getStudentRequests(
                                "user@test.com"))
                                .thenReturn(List.of());

                mockMvc.perform(
                                get("/api/v1/matches/student")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.data")
                                                .isArray())
                                .andExpect(jsonPath("$.data.length()")
                                                .value(0));

                verify(matchRequestService)
                                .getStudentRequests("user@test.com");
        }

        // =========================================================
        // TUTOR REQUESTS / PENDING REQUESTS
        // =========================================================

        @Test
        @DisplayName("GET /api/v1/matches/tutor - should return pending requests")
        void tutorRequests_shouldReturnRequestsList()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .studentId(5L)
                                .tutorId(10L)
                                .status(MatchRequestStatus.REQUESTED)
                                .build();

                when(matchRequestService.getTutorRequests(
                                "user@test.com"))
                                .thenReturn(List.of(response));

                mockMvc.perform(
                                get("/api/v1/matches/tutor")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Tutor requests fetched successfully."))
                                .andExpect(jsonPath("$.data")
                                                .isArray())
                                .andExpect(jsonPath("$.data.length()")
                                                .value(1))
                                .andExpect(jsonPath("$.data[0].requestId")
                                                .value(100L))
                                .andExpect(jsonPath("$.data[0].status")
                                                .value("REQUESTED"));

                verify(matchRequestService)
                                .getTutorRequests("user@test.com");
        }

        @Test
        @DisplayName("GET /api/v1/matches/tutor - empty list should return 200")
        void tutorRequests_whenEmpty_shouldReturnEmptyList()
                        throws Exception {

                when(matchRequestService.getTutorRequests(
                                "user@test.com"))
                                .thenReturn(List.of());

                mockMvc.perform(
                                get("/api/v1/matches/tutor")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.data")
                                                .isArray())
                                .andExpect(jsonPath("$.data.length()")
                                                .value(0));

                verify(matchRequestService)
                                .getTutorRequests("user@test.com");
        }

        // =========================================================
        // REQUEST DETAILS
        // =========================================================

        @Test
        @DisplayName("GET /api/v1/matches/{id} - valid ID should return details")
        void details_withValidId_shouldReturnRequestDetails()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .studentId(5L)
                                .tutorId(10L)
                                .status(MatchRequestStatus.REQUESTED)
                                .build();

                when(matchRequestService.getRequestDetails(
                                "user@test.com",
                                100L))
                                .thenReturn(response);

                mockMvc.perform(
                                get("/api/v1/matches/100")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Request details fetched successfully."))
                                .andExpect(jsonPath("$.data.requestId")
                                                .value(100L))
                                .andExpect(jsonPath("$.data.status")
                                                .value("REQUESTED"));

                verify(matchRequestService)
                                .getRequestDetails("user@test.com", 100L);
        }

        @Test
        @DisplayName("GET /api/v1/matches/{id} - not found should return 404")
        void details_whenNotFound_shouldReturn404()
                        throws Exception {

                when(matchRequestService.getRequestDetails(
                                "user@test.com",
                                999L))
                                .thenThrow(
                                                new ResourceNotFoundException(
                                                                "Match request not found with ID: 999"));

                mockMvc.perform(
                                get("/api/v1/matches/999")
                                                .principal(principal))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Match request not found with ID: 999"));
        }

        @Test
        @DisplayName("GET /api/v1/matches/{id} - zero ID should return 400")
        void details_withZeroId_shouldReturn400()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/matches/0")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false));
        }

        @Test
        @DisplayName("GET /api/v1/matches/{id} - negative ID should return 400")
        void details_withNegativeId_shouldReturn400()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/matches/-1")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false));
        }

        // =========================================================
        // CANCEL REQUEST
        // =========================================================

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/cancel - should cancel request")
        void cancel_shouldReturnSuccess()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .status(MatchRequestStatus.CANCELLED)
                                .build();

                when(matchRequestService.cancelRequest(
                                "user@test.com",
                                100L))
                                .thenReturn(response);

                mockMvc.perform(
                                put("/api/v1/matches/100/cancel")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Request cancelled successfully."))
                                .andExpect(jsonPath("$.data.status")
                                                .value("CANCELLED"));

                verify(matchRequestService)
                                .cancelRequest("user@test.com", 100L);
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/cancel - invalid state should return 400")
        void cancel_whenInvalidState_shouldReturn400()
                        throws Exception {

                when(matchRequestService.cancelRequest(
                                "user@test.com",
                                100L))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Only requested matches can be cancelled."));

                mockMvc.perform(
                                put("/api/v1/matches/100/cancel")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Only requested matches can be cancelled."));
        }

        @Test
        @DisplayName("PUT /api/v1/matches/0/cancel - invalid ID should return 400")
        void cancel_withInvalidId_shouldReturn400()
                        throws Exception {

                mockMvc.perform(
                                put("/api/v1/matches/0/cancel")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false));
        }

        // =========================================================
        // CONNECT STUDENT
        // =========================================================

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/connect - should connect student")
        void connect_shouldReturnSuccess()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .status(MatchRequestStatus.CONNECTED)
                                .build();

                when(matchRequestService.connectStudent(
                                "user@test.com",
                                100L))
                                .thenReturn(response);

                mockMvc.perform(
                                put("/api/v1/matches/100/connect")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Student request connected successfully."))
                                .andExpect(jsonPath("$.data.status")
                                                .value("CONNECTED"));

                verify(matchRequestService)
                                .connectStudent("user@test.com", 100L);
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/connect - invalid state should return 400")
        void connect_whenInvalidState_shouldReturn400()
                        throws Exception {

                when(matchRequestService.connectStudent(
                                "user@test.com",
                                100L))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Only requested matches can be connected."));

                mockMvc.perform(
                                put("/api/v1/matches/100/connect")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Only requested matches can be connected."));
        }

        // =========================================================
        // PARTIAL FINALIZE
        // =========================================================

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/partial-finalize - valid request should succeed")
        void partialFinalize_withValidData_shouldReturnSuccess()
                        throws Exception {

                TutorActionRequest request = TutorActionRequest.builder()
                                .acceptedSubjectIds(List.of(1L))
                                .build();

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .status(MatchRequestStatus.PARTIALLY_FINALIZED)
                                .build();

                when(matchRequestService.partiallyFinalize(
                                eq("user@test.com"),
                                eq(100L),
                                any(TutorActionRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(
                                put("/api/v1/matches/100/partial-finalize")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Request partially finalized successfully."))
                                .andExpect(jsonPath("$.data.status")
                                                .value("PARTIALLY_FINALIZED"));

                verify(matchRequestService)
                                .partiallyFinalize(
                                                eq("user@test.com"),
                                                eq(100L),
                                                any(TutorActionRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/partial-finalize - missing subjects should return 400")
        void partialFinalize_whenSubjectsMissing_shouldReturn400()
                        throws Exception {

                TutorActionRequest request = TutorActionRequest.builder()
                                .acceptedSubjectIds(null)
                                .build();

                mockMvc.perform(
                                put("/api/v1/matches/100/partial-finalize")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))
                                .andExpect(jsonPath("$.errors.acceptedSubjectIds")
                                                .exists());
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/partial-finalize - empty subjects should return 400")
        void partialFinalize_withEmptySubjects_shouldReturn400()
                        throws Exception {

                TutorActionRequest request = TutorActionRequest.builder()
                                .acceptedSubjectIds(List.of())
                                .build();

                mockMvc.perform(
                                put("/api/v1/matches/100/partial-finalize")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."));
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/partial-finalize - service rejection should return 400")
        void partialFinalize_whenInvalidState_shouldReturn400()
                        throws Exception {

                TutorActionRequest request = TutorActionRequest.builder()
                                .acceptedSubjectIds(List.of(1L))
                                .build();

                when(matchRequestService.partiallyFinalize(
                                eq("user@test.com"),
                                eq(100L),
                                any(TutorActionRequest.class)))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Request must be connected first."));

                mockMvc.perform(
                                put("/api/v1/matches/100/partial-finalize")
                                                .principal(principal)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Request must be connected first."));
        }

        // =========================================================
        // FINALIZE REQUEST
        // =========================================================

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/finalize - should finalize request")
        void finalizeRequest_shouldReturnSuccess()
                        throws Exception {

                MatchRequestResponse response = MatchRequestResponse.builder()
                                .requestId(100L)
                                .status(MatchRequestStatus.FINALIZED)
                                .build();

                when(matchRequestService.finalizeRequest(
                                "user@test.com",
                                100L))
                                .thenReturn(response);

                mockMvc.perform(
                                put("/api/v1/matches/100/finalize")
                                                .principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Request finalized successfully."))
                                .andExpect(jsonPath("$.data.status")
                                                .value("FINALIZED"));

                verify(matchRequestService)
                                .finalizeRequest("user@test.com", 100L);
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/finalize - invalid state should return 400")
        void finalizeRequest_whenInvalidState_shouldReturn400()
                        throws Exception {

                when(matchRequestService.finalizeRequest(
                                "user@test.com",
                                100L))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Request cannot be finalized."));

                mockMvc.perform(
                                put("/api/v1/matches/100/finalize")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Request cannot be finalized."));
        }

        @Test
        @DisplayName("PUT /api/v1/matches/{id}/finalize - invalid ID should return 400")
        void finalizeRequest_withInvalidId_shouldReturn400()
                        throws Exception {

                mockMvc.perform(
                                put("/api/v1/matches/0/finalize")
                                                .principal(principal))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success")
                                                .value(false));
        }
}