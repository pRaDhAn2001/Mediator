package com.mediator.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private static final AtomicInteger COUNTER = new AtomicInteger(0);

        private String email;
        private String mobileNumber;
        private String password;

        @BeforeEach
        void setUp() {
                int suffix = COUNTER.incrementAndGet();

                email = "integration" + suffix + "@example.com";
                mobileNumber = "9" + String.format("%09d", suffix);
                password = "Password123";
        }

        private String studentRegistrationJson(
                        String email,
                        String mobileNumber,
                        String password) throws Exception {
                return objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "email", email,
                                                "password", password,
                                                "firstName", "Test",
                                                "lastName", "Student",
                                                "mobileNumber", mobileNumber));
        }

        private String tutorRegistrationJson(
                        String email,
                        String mobileNumber,
                        String password) throws Exception {
                return objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "email", email,
                                                "password", password,
                                                "firstName", "Test",
                                                "lastName", "Tutor",
                                                "mobileNumber", mobileNumber));
        }

        private String loginJson(String email, String password)
                        throws Exception {
                return objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "email", email,
                                                "password", password));
        }

        // --------------------------------------------------
        // 1. Student registration
        // --------------------------------------------------

        @Test
        void registerStudent_withValidData_shouldSucceed()
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode body = objectMapper.readTree(
                                result.getResponse().getContentAsString());

                assertThat(body.path("message").asText())
                                .isEqualTo("Student registered successfully.");

                assertThat(body.path("data").path("token").asText())
                                .isNotBlank();

                assertThat(body.path("data").path("email").asText())
                                .isEqualTo(email);
        }

        // --------------------------------------------------
        // 2. Tutor registration
        // --------------------------------------------------

        @Test
        void registerTutor_withValidData_shouldSucceed()
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(tutorRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode body = objectMapper.readTree(
                                result.getResponse().getContentAsString());

                assertThat(body.path("message").asText())
                                .isEqualTo("Tutor registered successfully.");

                assertThat(body.path("data").path("token").asText())
                                .isNotBlank();

                assertThat(body.path("data").path("email").asText())
                                .isEqualTo(email);
        }

        // --------------------------------------------------
        // 3. Duplicate email
        // --------------------------------------------------

        @Test
        void registerStudent_withDuplicateEmail_shouldFail()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk());

                String secondMobile = "8" + String.format(
                                "%09d",
                                COUNTER.incrementAndGet());

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                secondMobile,
                                                                password)))
                                .andReturn();

                assertThat(result.getResponse().getStatus())
                                .isBetween(400, 499);
        }

        // --------------------------------------------------
        // 4. Invalid email
        // --------------------------------------------------

        @Test
        void registerStudent_withInvalidEmail_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                "invalid-email",
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isBadRequest());
        }

        // --------------------------------------------------
        // 5. Invalid mobile number
        // --------------------------------------------------

        @Test
        void registerStudent_withInvalidMobileNumber_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                "1234567890",
                                                                password)))
                                .andExpect(status().isBadRequest());
        }

        // --------------------------------------------------
        // 6. Password too short
        // --------------------------------------------------

        @Test
        void registerStudent_withShortPassword_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                "short")))
                                .andExpect(status().isBadRequest());
        }

        // --------------------------------------------------
        // 7. Missing required fields
        // --------------------------------------------------

        @Test
        void registerStudent_withMissingFields_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        // --------------------------------------------------
        // 8. Login with valid credentials
        // --------------------------------------------------

        @Test
        void login_withValidCredentials_shouldReturnJwt()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk());

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(loginJson(email, password)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode body = objectMapper.readTree(
                                result.getResponse().getContentAsString());

                assertThat(body.path("message").asText())
                                .isEqualTo("Login successful.");

                assertThat(body.path("data").path("token").asText())
                                .isNotBlank();

                assertThat(body.path("data").path("email").asText())
                                .isEqualTo(email);
        }

        // --------------------------------------------------
        // 9. Login with invalid password
        // --------------------------------------------------

        @Test
        void login_withInvalidPassword_shouldFail()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk());

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(loginJson(
                                                                email,
                                                                "WrongPassword123")))
                                .andReturn();

                assertThat(result.getResponse().getStatus())
                                .isBetween(400, 499);
        }

        // --------------------------------------------------
        // 10. Login with unknown email
        // --------------------------------------------------

        @Test
        void login_withUnknownEmail_shouldFail()
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(loginJson(
                                                                "unknown@example.com",
                                                                "Password123")))
                                .andReturn();

                assertThat(result.getResponse().getStatus())
                                .isBetween(400, 499);
        }

        // --------------------------------------------------
        // 11. Login with invalid request body
        // --------------------------------------------------

        @Test
        void login_withMissingFields_shouldReturnBadRequest()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void registerStudent_withDuplicateMobileNumber_shouldFail()
                        throws Exception {

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk());

                String secondEmail = "duplicate-mobile-" + COUNTER.incrementAndGet()
                                + "@example.com";

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                secondEmail,
                                                                mobileNumber,
                                                                password)))
                                .andReturn();

                assertThat(result.getResponse().getStatus())
                                .isBetween(400, 499);
        }

        @Test
        void registerTutor_withInvalidData_shouldReturnBadRequest()
                        throws Exception {

                String requestBody = objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "email", "invalid-email",
                                                "password", "",
                                                "firstName", "",
                                                "lastName", "",
                                                "mobileNumber", "123"));

                mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void logout_withoutToken_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(post("/api/v1/auth/logout"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void logout_withValidToken_shouldSucceed()
                        throws Exception {

                MvcResult registrationResult = mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(studentRegistrationJson(
                                                                email,
                                                                mobileNumber,
                                                                password)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode registrationBody = objectMapper.readTree(
                                registrationResult.getResponse().getContentAsString());

                String token = registrationBody.path("data")
                                .path("token")
                                .asText();

                assertThat(token).isNotBlank();

                MvcResult logoutResult = mockMvc.perform(
                                post("/api/v1/auth/logout")
                                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode logoutBody = objectMapper.readTree(
                                logoutResult.getResponse().getContentAsString());

                assertThat(logoutBody.path("message").asText())
                                .isEqualTo("Logged out successfully.");
        }

}
