package com.mediator.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.auth.dto.request.LoginRequest;
import com.mediator.auth.dto.request.RegisterStudentRequest;
import com.mediator.auth.dto.request.RegisterTutorRequest;
import com.mediator.auth.dto.response.LoginResponse;
import com.mediator.auth.entity.Role;
import com.mediator.auth.service.AuthService;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mediator.auth.config.SecurityConfig;
import com.mediator.auth.security.JwtAuthenticationFilter;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class,
                JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        // =========================================================
        // STUDENT REGISTRATION
        // =========================================================

        @Test
        void registerStudent_withValidData_shouldReturnSuccess()
                        throws Exception {

                RegisterStudentRequest request = RegisterStudentRequest.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("student@test.com")
                                .password("Password123")
                                .mobileNumber("9876543210")
                                .build();

                LoginResponse loginResponse = LoginResponse.builder()
                                .token("jwt-token-student")
                                .tokenType("Bearer")
                                .userId(1L)
                                .email("student@test.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(Role.ROLE_STUDENT)
                                .build();

                when(authService.registerStudent(any(RegisterStudentRequest.class)))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())

                                .andExpect(jsonPath("$.success").value(true))

                                .andExpect(jsonPath("$.message")
                                                .value("Student registered successfully."))

                                .andExpect(jsonPath("$.data.token")
                                                .value("jwt-token-student"))

                                .andExpect(jsonPath("$.data.tokenType")
                                                .value("Bearer"))

                                .andExpect(jsonPath("$.data.userId")
                                                .value(1))

                                .andExpect(jsonPath("$.data.email")
                                                .value("student@test.com"))

                                .andExpect(jsonPath("$.data.firstName")
                                                .value("John"))

                                .andExpect(jsonPath("$.data.lastName")
                                                .value("Doe"))

                                .andExpect(jsonPath("$.data.role")
                                                .value("ROLE_STUDENT"));

                verify(authService).registerStudent(
                                argThat(actual -> actual.getFirstName().equals("John")
                                                && actual.getLastName().equals("Doe")
                                                && actual.getEmail().equals("student@test.com")
                                                && actual.getPassword().equals("Password123")
                                                && actual.getMobileNumber().equals("9876543210")));
        }

        @Test
        void registerStudent_withInvalidData_shouldReturnBadRequest()
                        throws Exception {

                RegisterStudentRequest request = RegisterStudentRequest.builder()
                                .firstName("")
                                .lastName("")
                                .email("invalid-email")
                                .password("")
                                .mobileNumber("123")
                                .build();

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))

                                .andExpect(jsonPath("$.errors.email")
                                                .exists())

                                .andExpect(jsonPath("$.errors.firstName")
                                                .exists())

                                .andExpect(jsonPath("$.errors.lastName")
                                                .exists())

                                .andExpect(jsonPath("$.errors.password")
                                                .exists())

                                .andExpect(jsonPath("$.errors.mobileNumber")
                                                .exists());

                verifyNoInteractions(authService);
        }

        @Test
        void registerStudent_whenServiceThrowsBadRequest_shouldReturnBadRequest()
                        throws Exception {

                RegisterStudentRequest request = RegisterStudentRequest.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("existing@test.com")
                                .password("Password123")
                                .mobileNumber("9876543210")
                                .build();

                when(authService.registerStudent(any(RegisterStudentRequest.class)))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Email already registered."));

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Email already registered."));

                verify(authService).registerStudent(
                                any(RegisterStudentRequest.class));
        }

        // =========================================================
        // TUTOR REGISTRATION
        // =========================================================

        @Test
        void registerTutor_withValidData_shouldReturnSuccess()
                        throws Exception {

                RegisterTutorRequest request = RegisterTutorRequest.builder()
                                .firstName("Jane")
                                .lastName("Smith")
                                .email("tutor@test.com")
                                .password("Password123")
                                .mobileNumber("9876543211")
                                .build();

                LoginResponse loginResponse = LoginResponse.builder()
                                .token("jwt-token-tutor")
                                .tokenType("Bearer")
                                .userId(2L)
                                .email("tutor@test.com")
                                .firstName("Jane")
                                .lastName("Smith")
                                .role(Role.ROLE_TUTOR)
                                .build();

                when(authService.registerTutor(any(RegisterTutorRequest.class)))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())

                                .andExpect(jsonPath("$.success")
                                                .value(true))

                                .andExpect(jsonPath("$.message")
                                                .value("Tutor registered successfully."))

                                .andExpect(jsonPath("$.data.token")
                                                .value("jwt-token-tutor"))

                                .andExpect(jsonPath("$.data.tokenType")
                                                .value("Bearer"))

                                .andExpect(jsonPath("$.data.userId")
                                                .value(2))

                                .andExpect(jsonPath("$.data.email")
                                                .value("tutor@test.com"))

                                .andExpect(jsonPath("$.data.firstName")
                                                .value("Jane"))

                                .andExpect(jsonPath("$.data.lastName")
                                                .value("Smith"))

                                .andExpect(jsonPath("$.data.role")
                                                .value("ROLE_TUTOR"));

                verify(authService).registerTutor(
                                argThat(actual -> actual.getFirstName().equals("Jane")
                                                && actual.getLastName().equals("Smith")
                                                && actual.getEmail().equals("tutor@test.com")
                                                && actual.getPassword().equals("Password123")
                                                && actual.getMobileNumber().equals("9876543211")));
        }

        @Test
        void registerTutor_withInvalidData_shouldReturnBadRequest()
                        throws Exception {

                RegisterTutorRequest request = RegisterTutorRequest.builder()
                                .firstName("")
                                .lastName("")
                                .email("invalid-email")
                                .password("")
                                .mobileNumber("123")
                                .build();

                mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))

                                .andExpect(jsonPath("$.errors.email")
                                                .exists())

                                .andExpect(jsonPath("$.errors.firstName")
                                                .exists())

                                .andExpect(jsonPath("$.errors.lastName")
                                                .exists())

                                .andExpect(jsonPath("$.errors.password")
                                                .exists())

                                .andExpect(jsonPath("$.errors.mobileNumber")
                                                .exists());

                verifyNoInteractions(authService);
        }

        @Test
        void registerTutor_whenServiceThrowsBadRequest_shouldReturnBadRequest()
                        throws Exception {

                RegisterTutorRequest request = RegisterTutorRequest.builder()
                                .firstName("Jane")
                                .lastName("Smith")
                                .email("existing@test.com")
                                .password("Password123")
                                .mobileNumber("9876543211")
                                .build();

                when(authService.registerTutor(any(RegisterTutorRequest.class)))
                                .thenThrow(
                                                new BadRequestException(
                                                                "Email already registered."));

                mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Email already registered."));

                verify(authService).registerTutor(
                                any(RegisterTutorRequest.class));
        }

        // =========================================================
        // LOGIN
        // =========================================================

        @Test
        void login_withValidCredentials_shouldReturnSuccess()
                        throws Exception {

                LoginRequest request = LoginRequest.builder()
                                .email("student@test.com")
                                .password("Password123")
                                .build();

                LoginResponse loginResponse = LoginResponse.builder()
                                .token("jwt-token-login")
                                .tokenType("Bearer")
                                .userId(1L)
                                .email("student@test.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(Role.ROLE_STUDENT)
                                .build();

                when(authService.login(any(LoginRequest.class)))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())

                                .andExpect(jsonPath("$.success")
                                                .value(true))

                                .andExpect(jsonPath("$.message")
                                                .value("Login successful."))

                                .andExpect(jsonPath("$.data.token")
                                                .value("jwt-token-login"))

                                .andExpect(jsonPath("$.data.tokenType")
                                                .value("Bearer"))

                                .andExpect(jsonPath("$.data.userId")
                                                .value(1))

                                .andExpect(jsonPath("$.data.email")
                                                .value("student@test.com"))

                                .andExpect(jsonPath("$.data.role")
                                                .value("ROLE_STUDENT"));

                verify(authService).login(
                                argThat(actual -> actual.getEmail().equals("student@test.com")
                                                && actual.getPassword().equals("Password123")));
        }

        @Test
        void login_withInvalidRequestData_shouldReturnBadRequest()
                        throws Exception {

                LoginRequest request = LoginRequest.builder()
                                .email("invalid-email")
                                .password("")
                                .build();

                mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Validation failed."))

                                .andExpect(jsonPath("$.errors.email")
                                                .exists())

                                .andExpect(jsonPath("$.errors.password")
                                                .exists());

                verifyNoInteractions(authService);
        }

        @Test
        void login_withBadCredentials_shouldReturnUnauthorized()
                        throws Exception {

                LoginRequest request = LoginRequest.builder()
                                .email("student@test.com")
                                .password("WrongPassword")
                                .build();

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(
                                                new BadCredentialsException(
                                                                "Invalid email or password."));

                mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())

                                .andExpect(jsonPath("$.success")
                                                .value(false))

                                .andExpect(jsonPath("$.message")
                                                .value("Invalid email or password."));

                verify(authService).login(
                                any(LoginRequest.class));
        }

        // =========================================================
        // LOGOUT
        // =========================================================

        @Test
        void logout_shouldReturnSuccess()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/logout")
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())

                                .andExpect(jsonPath("$.success")
                                                .value(true))

                                .andExpect(jsonPath("$.message")
                                                .value("Logged out successfully."))

                                .andExpect(jsonPath("$.data")
                                                .doesNotExist());

                verifyNoInteractions(authService);
        }

        // =========================================================
        // HTTP METHOD / CONTENT VALIDATION
        // =========================================================

        @Test
        void registerStudent_withWrongHttpMethod_shouldReturnMethodNotAllowed()
                        throws Exception {

                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                                .get("/api/v1/auth/register/student"))
                                .andExpect(status().isMethodNotAllowed());

                verifyNoInteractions(authService);
        }

        @Test
        void login_withMalformedJson_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{invalid-json}"))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(authService);
        }
}