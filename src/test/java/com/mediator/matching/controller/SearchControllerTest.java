package com.mediator.matching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.auth.security.JwtAuthenticationFilter;
import com.mediator.auth.config.SecurityConfig;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.matching.dto.request.TutorSearchRequest;
import com.mediator.matching.dto.response.TutorCardResponse;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorSearchResponse;
import com.mediator.matching.service.MatchService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.mediator.common.exception.GlobalExceptionHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class,
                JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
@DisplayName("SearchController WebMvc Tests")
class SearchControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private MatchService matchService;

        private static final String STUDENT_EMAIL = "student@test.com";

        // ============================================================
        // SETUP / TEARDOWN
        // ============================================================

        @BeforeEach
        void setUp() {

                SecurityContext context = SecurityContextHolder.createEmptyContext();

                TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                                STUDENT_EMAIL,
                                null);

                authentication.setAuthenticated(true);

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        // ============================================================
        // SEARCH TUTORS
        // ============================================================

        @Nested
        @DisplayName("POST /api/v1/search/tutors")
        class SearchTutorsTests {

                @Test
                @DisplayName("Should return tutors for valid search request")
                void searchTutors_withValidRequest_shouldReturnTutors() throws Exception {

                        TutorSearchRequest request = TutorSearchRequest.builder()
                                        .subjectIds(List.of(1L, 2L))
                                        .boardId(1L)
                                        .classLevelId(8L)
                                        .preferredMode(
                                                        PreferredModeDto.builder()
                                                                        .studentHome(true)
                                                                        .build())
                                        .latitude(22.545)
                                        .longitude(88.352)
                                        .radius(5.0)
                                        .page(0)
                                        .size(10)
                                        .build();

                        TutorCardResponse tutorCard = TutorCardResponse.builder()
                                        .tutorId(10L)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .matchedSubjects(2)
                                        .totalRequestedSubjects(2)
                                        .matchPercentage(100.0)
                                        .build();

                        TutorSearchResponse response = TutorSearchResponse.builder()
                                        .tutors(List.of(tutorCard))
                                        .currentPage(0)
                                        .totalPages(1)
                                        .totalElements(1L)
                                        .hasNext(false)
                                        .build();

                        when(matchService.searchTutors(any(TutorSearchRequest.class)))
                                        .thenReturn(response);

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Tutors fetched successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.tutors").isArray())
                                        .andExpect(jsonPath("$.data.tutors.length()").value(1))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].tutorId")
                                                                        .value(10))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].firstName")
                                                                        .value("John"))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].lastName")
                                                                        .value("Doe"))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].matchedSubjects")
                                                                        .value(2))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].totalRequestedSubjects")
                                                                        .value(2))
                                        .andExpect(
                                                        jsonPath("$.data.tutors[0].matchPercentage")
                                                                        .value(100.0))
                                        .andExpect(
                                                        jsonPath("$.data.currentPage")
                                                                        .value(0))
                                        .andExpect(
                                                        jsonPath("$.data.totalPages")
                                                                        .value(1))
                                        .andExpect(
                                                        jsonPath("$.data.totalElements")
                                                                        .value(1))
                                        .andExpect(
                                                        jsonPath("$.data.hasNext")
                                                                        .value(false));

                        verify(matchService)
                                        .searchTutors(any(TutorSearchRequest.class));
                }

                @Test
                @DisplayName("Should return empty result when no tutors match")
                void searchTutors_whenNoTutorsFound_shouldReturnEmptyResult()
                                throws Exception {

                        TutorSearchRequest request = TutorSearchRequest.builder()
                                        .subjectIds(List.of(1L))
                                        .boardId(1L)
                                        .classLevelId(8L)
                                        .page(0)
                                        .size(10)
                                        .build();

                        TutorSearchResponse response = TutorSearchResponse.builder()
                                        .tutors(List.of())
                                        .currentPage(0)
                                        .totalPages(0)
                                        .totalElements(0L)
                                        .hasNext(false)
                                        .build();

                        when(matchService.searchTutors(any(TutorSearchRequest.class)))
                                        .thenReturn(response);

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Tutors fetched successfully."))
                                        .andExpect(jsonPath("$.data.tutors").isArray())
                                        .andExpect(
                                                        jsonPath("$.data.tutors.length()")
                                                                        .value(0))
                                        .andExpect(
                                                        jsonPath("$.data.totalElements")
                                                                        .value(0));

                        verify(matchService)
                                        .searchTutors(any(TutorSearchRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when subject list is empty")
                void searchTutors_withEmptySubjects_shouldReturnBadRequest()
                                throws Exception {

                        TutorSearchRequest request = TutorSearchRequest.builder()
                                        .subjectIds(List.of())
                                        .boardId(1L)
                                        .classLevelId(8L)
                                        .page(0)
                                        .size(10)
                                        .build();

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Validation failed."));

                        verify(matchService, never())
                                        .searchTutors(any(TutorSearchRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when subject list is missing")
                void searchTutors_withMissingSubjects_shouldReturnBadRequest()
                                throws Exception {

                        TutorSearchRequest request = TutorSearchRequest.builder()
                                        .boardId(1L)
                                        .classLevelId(8L)
                                        .page(0)
                                        .size(10)
                                        .build();

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Validation failed."));

                        verify(matchService, never())
                                        .searchTutors(any(TutorSearchRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when request body is missing")
                void searchTutors_withMissingRequestBody_shouldReturnBadRequest()
                                throws Exception {

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isBadRequest());

                        verify(matchService, never())
                                        .searchTutors(any(TutorSearchRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when request JSON is malformed")
                void searchTutors_withMalformedJson_shouldReturnBadRequest()
                                throws Exception {

                        mockMvc.perform(
                                        post("/api/v1/search/tutors")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content("{invalid-json"))
                                        .andExpect(status().isBadRequest());

                        verify(matchService, never())
                                        .searchTutors(any(TutorSearchRequest.class));
                }
        }

        // ============================================================
        // GET TUTOR DETAILS
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/search/tutors/{tutorId}")
        class GetTutorDetailsTests {

                @Test
                @DisplayName("Should return tutor details for valid tutor ID")
                void getTutorDetails_withValidId_shouldReturnTutorDetails()
                                throws Exception {

                        TutorDetailsResponse response = TutorDetailsResponse.builder()
                                        .tutorId(10L)
                                        .firstName("John")
                                        .lastName("Doe")
                                        .description("Expert Mathematics tutor")
                                        .build();

                        when(matchService.getTutorDetails(
                                        STUDENT_EMAIL,
                                        10L)).thenReturn(response);

                        mockMvc.perform(
                                        get("/api/v1/search/tutors/10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Tutor details fetched successfully."))
                                        .andExpect(
                                                        jsonPath("$.data.tutorId")
                                                                        .value(10))
                                        .andExpect(
                                                        jsonPath("$.data.firstName")
                                                                        .value("John"))
                                        .andExpect(
                                                        jsonPath("$.data.lastName")
                                                                        .value("Doe"))
                                        .andExpect(
                                                        jsonPath("$.data.description")
                                                                        .value("Expert Mathematics tutor"));

                        verify(matchService)
                                        .getTutorDetails(STUDENT_EMAIL, 10L);
                }

                @Test
                @DisplayName("Should return 404 when tutor does not exist")
                void getTutorDetails_whenTutorNotFound_shouldReturn404()
                                throws Exception {

                        when(matchService.getTutorDetails(
                                        STUDENT_EMAIL,
                                        999L)).thenThrow(
                                                        new ResourceNotFoundException("Tutor not found."));

                        mockMvc.perform(
                                        get("/api/v1/search/tutors/999"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value("Tutor not found."));

                        verify(matchService)
                                        .getTutorDetails(STUDENT_EMAIL, 999L);
                }

                @Test
                @DisplayName("Should return 400 when tutor details request violates business rules")
                void getTutorDetails_whenBusinessRuleFails_shouldReturn400()
                                throws Exception {

                        when(matchService.getTutorDetails(
                                        STUDENT_EMAIL,
                                        10L)).thenThrow(
                                                        new BadRequestException(
                                                                        "Only students can view tutor details."));

                        mockMvc.perform(
                                        get("/api/v1/search/tutors/10"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(
                                                        jsonPath("$.message")
                                                                        .value(
                                                                                        "Only students can view tutor details."));

                        verify(matchService)
                                        .getTutorDetails(STUDENT_EMAIL, 10L);
                }

                @Test
                @DisplayName("Should use authenticated user's email when fetching tutor details")
                void getTutorDetails_shouldUseAuthenticatedUserEmail()
                                throws Exception {

                        String anotherStudentEmail = "another.student@test.com";

                        SecurityContext context = SecurityContextHolder.createEmptyContext();

                        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                                        anotherStudentEmail,
                                        null);

                        authentication.setAuthenticated(true);

                        context.setAuthentication(authentication);

                        SecurityContextHolder.setContext(context);

                        TutorDetailsResponse response = TutorDetailsResponse.builder()
                                        .tutorId(20L)
                                        .firstName("Jane")
                                        .lastName("Smith")
                                        .description("Physics tutor")
                                        .build();

                        when(matchService.getTutorDetails(
                                        anotherStudentEmail,
                                        20L)).thenReturn(response);

                        mockMvc.perform(
                                        get("/api/v1/search/tutors/20"))
                                        .andExpect(status().isOk())
                                        .andExpect(
                                                        jsonPath("$.data.tutorId")
                                                                        .value(20));

                        verify(matchService)
                                        .getTutorDetails(
                                                        anotherStudentEmail,
                                                        20L);
                }
        }
}