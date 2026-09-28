package com.mediator.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.base.Gender;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.common.exception.GlobalExceptionHandler;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.student.dto.dashboard.StudentDashboardResponse;
import com.mediator.student.dto.preference.StudentPreferenceDto;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.service.StudentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.mediator.auth.config.SecurityConfig.class,
                com.mediator.auth.security.JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("StudentController WebMvc Tests")
class StudentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private StudentService studentService;

        // ============================================================
        // COMMON TEST DATA
        // ============================================================

        private StudentProfileRequest createValidProfileRequest() {

                return StudentProfileRequest.builder()
                                .gender(Gender.MALE)
                                .boardId(1L)
                                .classLevelId(8L)
                                .schoolName("St. Xavier's")
                                .parentName("Jane Doe")
                                .parentPhone("9876543210")
                                .parentEmail("parent@test.com")
                                .sameAsPrimaryPhone(false)
                                .sameAsPrimaryEmail(false)
                                .address(
                                                AddressDto.builder()
                                                                .street("Park Street")
                                                                .city("Kolkata")
                                                                .district("Kolkata")
                                                                .state("West Bengal")
                                                                .zipCode("700016")
                                                                .latitude(22.545)
                                                                .longitude(88.352)
                                                                .build())
                                .preferredMode(
                                                PreferredModeDto.builder()
                                                                .studentHome(true)
                                                                .online(false)
                                                                .tutorHome(false)
                                                                .build())
                                .preferredRadius(5.0)
                                .build();
        }

        private StudentProfileResponse createProfileResponse() {

                return StudentProfileResponse.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("student@test.com")
                                .boardName("CBSE")
                                .profileCompletion(80)
                                .build();
        }

        // ============================================================
        // GET PROFILE
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/students/profile")
        class GetProfileTests {

                @Test
                @DisplayName("Should return student profile successfully")
                void getProfile_shouldReturnProfile() throws Exception {

                        StudentProfileResponse response = createProfileResponse();

                        when(studentService.getProfile())
                                        .thenReturn(response);

                        mockMvc.perform(
                                        get("/api/v1/students/profile")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile fetched successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.firstName").value("John"))
                                        .andExpect(jsonPath("$.data.lastName").value("Doe"))
                                        .andExpect(jsonPath("$.data.email").value("student@test.com"))
                                        .andExpect(jsonPath("$.data.boardName").value("CBSE"))
                                        .andExpect(jsonPath("$.data.profileCompletion").value(80))
                                        .andExpect(jsonPath("$.timestamp").exists());

                        verify(studentService).getProfile();
                }

                @Test
                @DisplayName("Should return 404 when student profile is not found")
                void getProfile_whenNotFound_shouldReturn404() throws Exception {

                        when(studentService.getProfile())
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "Student profile not found."));

                        mockMvc.perform(
                                        get("/api/v1/students/profile")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile not found."));

                        verify(studentService).getProfile();
                }
        }

        // ============================================================
        // UPDATE PROFILE
        // ============================================================

        @Nested
        @DisplayName("PUT /api/v1/students/profile")
        class UpdateProfileTests {

                @Test
                @DisplayName("Should update student profile successfully")
                void updateProfile_withValidData_shouldReturnUpdatedProfile()
                                throws Exception {

                        StudentProfileRequest request = createValidProfileRequest();

                        StudentProfileResponse response = StudentProfileResponse.builder()
                                        .firstName("John")
                                        .lastName("Doe")
                                        .email("student@test.com")
                                        .parentName(request.getParentName())
                                        .parentPhone(request.getParentPhone())
                                        .parentEmail(request.getParentEmail())
                                        .boardName("CBSE")
                                        .profileCompletion(95)
                                        .build();

                        when(studentService.updateProfile(
                                        any(StudentProfileRequest.class)))
                                        .thenReturn(response);

                        mockMvc.perform(
                                        put("/api/v1/students/profile")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile updated successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.firstName").value("John"))
                                        .andExpect(jsonPath("$.data.parentName")
                                                        .value("Jane Doe"))
                                        .andExpect(jsonPath("$.data.parentPhone")
                                                        .value("9876543210"))
                                        .andExpect(jsonPath("$.data.parentEmail")
                                                        .value("parent@test.com"))
                                        .andExpect(jsonPath("$.data.profileCompletion")
                                                        .value(95));

                        verify(studentService)
                                        .updateProfile(any(StudentProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when profile request validation fails")
                void updateProfile_withInvalidData_shouldReturn400()
                                throws Exception {

                        StudentProfileRequest invalidRequest = StudentProfileRequest.builder()
                                        .gender(null)
                                        .parentName("")
                                        .build();

                        mockMvc.perform(
                                        put("/api/v1/students/profile")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        invalidRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Validation failed."))
                                        .andExpect(jsonPath("$.errors").exists())
                                        .andExpect(jsonPath("$.errors.gender").exists())
                                        .andExpect(jsonPath("$.errors.parentName").exists());

                        /*
                         * Because validation fails before the controller invokes
                         * StudentService, service interaction must not occur.
                         */
                        verifyNoInteractions(studentService);
                }

                @Test
                @DisplayName("Should return 400 when service rejects profile update")
                void updateProfile_whenServiceRejects_shouldReturn400()
                                throws Exception {

                        StudentProfileRequest request = createValidProfileRequest();

                        when(studentService.updateProfile(
                                        any(StudentProfileRequest.class)))
                                        .thenThrow(
                                                        new BadRequestException(
                                                                        "Invalid student profile data."));

                        mockMvc.perform(
                                        put("/api/v1/students/profile")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Invalid student profile data."));

                        verify(studentService)
                                        .updateProfile(any(StudentProfileRequest.class));
                }

                @Test
                @DisplayName("Should return 404 when student profile does not exist")
                void updateProfile_whenStudentNotFound_shouldReturn404()
                                throws Exception {

                        StudentProfileRequest request = createValidProfileRequest();

                        when(studentService.updateProfile(
                                        any(StudentProfileRequest.class)))
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "Student profile not found."));

                        mockMvc.perform(
                                        put("/api/v1/students/profile")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile not found."));

                        verify(studentService)
                                        .updateProfile(any(StudentProfileRequest.class));
                }
        }

        // ============================================================
        // GET DASHBOARD
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/students/dashboard")
        class GetDashboardTests {

                @Test
                @DisplayName("Should return student dashboard successfully")
                void getDashboard_shouldReturnDashboard() throws Exception {

                        StudentDashboardResponse response = StudentDashboardResponse.builder()
                                        .profileCompletion(85)
                                        .activeMatchesCount(2L)
                                        .pendingMatchesCount(1L)
                                        .build();

                        when(studentService.getDashboard())
                                        .thenReturn(response);

                        mockMvc.perform(
                                        get("/api/v1/students/dashboard")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Dashboard fetched successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.profileCompletion")
                                                        .value(85))
                                        .andExpect(jsonPath("$.data.activeMatchesCount")
                                                        .value(2))
                                        .andExpect(jsonPath("$.data.pendingMatchesCount")
                                                        .value(1))
                                        .andExpect(jsonPath("$.timestamp").exists());

                        verify(studentService).getDashboard();
                }

                @Test
                @DisplayName("Should return 404 when dashboard student is not found")
                void getDashboard_whenStudentNotFound_shouldReturn404()
                                throws Exception {

                        when(studentService.getDashboard())
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "Student profile not found."));

                        mockMvc.perform(
                                        get("/api/v1/students/dashboard")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile not found."));

                        verify(studentService).getDashboard();
                }
        }

        // ============================================================
        // UPDATE PREFERENCES
        // ============================================================

        @Nested
        @DisplayName("PUT /api/v1/students/preferences")
        class UpdatePreferencesTests {

                @Test
                @DisplayName("Should update preferred subjects successfully")
                void updatePreferences_withValidData_shouldReturnSuccess()
                                throws Exception {

                        StudentPreferenceDto request = StudentPreferenceDto.builder()
                                        .subjectIds(List.of(1L, 2L))
                                        .build();

                        List<SubjectResponse> responses = List.of(
                                        SubjectResponse.builder()
                                                        .id(1L)
                                                        .name("Mathematics")
                                                        .build(),
                                        SubjectResponse.builder()
                                                        .id(2L)
                                                        .name("Physics")
                                                        .build());

                        when(studentService.updatePreferences(
                                        any(StudentPreferenceDto.class)))
                                        .thenReturn(responses);

                        mockMvc.perform(
                                        put("/api/v1/students/preferences")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student preferences updated successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(2))
                                        .andExpect(jsonPath("$.data[0].id").value(1))
                                        .andExpect(jsonPath("$.data[0].name")
                                                        .value("Mathematics"))
                                        .andExpect(jsonPath("$.data[1].id").value(2))
                                        .andExpect(jsonPath("$.data[1].name")
                                                        .value("Physics"));

                        verify(studentService)
                                        .updatePreferences(any(StudentPreferenceDto.class));
                }

                @Test
                @DisplayName("Should return 400 when subject list is empty")
                void updatePreferences_withEmptySubjects_shouldReturn400()
                                throws Exception {

                        StudentPreferenceDto request = StudentPreferenceDto.builder()
                                        .subjectIds(List.of())
                                        .build();

                        mockMvc.perform(
                                        put("/api/v1/students/preferences")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Validation failed."));

                        verifyNoInteractions(studentService);
                }

                @Test
                @DisplayName("Should return 400 when subject list is null")
                void updatePreferences_withNullSubjects_shouldReturn400()
                                throws Exception {

                        StudentPreferenceDto request = StudentPreferenceDto.builder()
                                        .subjectIds(null)
                                        .build();

                        mockMvc.perform(
                                        put("/api/v1/students/preferences")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Validation failed."));

                        verifyNoInteractions(studentService);
                }

                @Test
                @DisplayName("Should return 400 when service rejects preferences")
                void updatePreferences_whenServiceRejects_shouldReturn400()
                                throws Exception {

                        StudentPreferenceDto request = StudentPreferenceDto.builder()
                                        .subjectIds(List.of(999L))
                                        .build();

                        when(studentService.updatePreferences(
                                        any(StudentPreferenceDto.class)))
                                        .thenThrow(
                                                        new BadRequestException(
                                                                        "Invalid subject selection."));

                        mockMvc.perform(
                                        put("/api/v1/students/preferences")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .content(
                                                                        objectMapper.writeValueAsString(
                                                                                        request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Invalid subject selection."));

                        verify(studentService)
                                        .updatePreferences(any(StudentPreferenceDto.class));
                }
        }

        // ============================================================
        // GET PREFERENCES
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/students/preferences")
        class GetPreferencesTests {

                @Test
                @DisplayName("Should return preferred subjects successfully")
                void getPreferences_shouldReturnPreferencesList()
                                throws Exception {

                        List<SubjectResponse> responses = List.of(
                                        SubjectResponse.builder()
                                                        .id(1L)
                                                        .name("Mathematics")
                                                        .build(),
                                        SubjectResponse.builder()
                                                        .id(2L)
                                                        .name("Physics")
                                                        .build());

                        when(studentService.getPreferences())
                                        .thenReturn(responses);

                        mockMvc.perform(
                                        get("/api/v1/students/preferences")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student preferences fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(2))
                                        .andExpect(jsonPath("$.data[0].id").value(1))
                                        .andExpect(jsonPath("$.data[0].name")
                                                        .value("Mathematics"))
                                        .andExpect(jsonPath("$.data[1].id").value(2))
                                        .andExpect(jsonPath("$.data[1].name")
                                                        .value("Physics"))
                                        .andExpect(jsonPath("$.timestamp").exists());

                        verify(studentService).getPreferences();
                }

                @Test
                @DisplayName("Should return empty list when student has no preferences")
                void getPreferences_whenEmpty_shouldReturnEmptyList()
                                throws Exception {

                        when(studentService.getPreferences())
                                        .thenReturn(List.of());

                        mockMvc.perform(
                                        get("/api/v1/students/preferences")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student preferences fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(0));

                        verify(studentService).getPreferences();
                }

                @Test
                @DisplayName("Should return 404 when student is not found")
                void getPreferences_whenStudentNotFound_shouldReturn404()
                                throws Exception {

                        when(studentService.getPreferences())
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "Student profile not found."));

                        mockMvc.perform(
                                        get("/api/v1/students/preferences")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Student profile not found."));

                        verify(studentService).getPreferences();
                }
        }
}