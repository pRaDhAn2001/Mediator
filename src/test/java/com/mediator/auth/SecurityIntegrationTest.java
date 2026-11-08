package com.mediator.auth;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.security.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private ObjectMapper objectMapper;

        private static final AtomicLong MOBILE_SEQUENCE = new AtomicLong(100_000_000L);

        // =========================================================
        // UNAUTHENTICATED ACCESS
        // =========================================================

        @Test
        void protectedStudentEndpoint_withoutToken_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(get("/api/v1/students/profile"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void protectedTutorEndpoint_withoutToken_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(get("/api/v1/tutors/profile"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void protectedEndpoint_withMalformedBearerToken_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void protectedEndpoint_withInvalidJwtStructure_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", "Bearer not.a.valid.jwt"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void protectedEndpoint_withBasicAuthorization_shouldReturnUnauthorized()
                        throws Exception {

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", "Basic dGVzdDp0ZXN0"))
                                .andExpect(status().isUnauthorized());
        }

        // =========================================================
        // VALID AUTHENTICATION
        // =========================================================

        @Test
        void student_withValidToken_shouldAccessOwnProfile()
                        throws Exception {

                String token = registerStudentAndGetToken();

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", bearer(token)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.email").exists());
        }

        @Test
        void tutor_withValidToken_shouldAccessOwnProfile()
                        throws Exception {

                String token = registerTutorAndGetToken();

                mockMvc.perform(
                                get("/api/v1/tutors/profile")
                                                .header("Authorization", bearer(token)))
                                .andExpect(status().isOk());
        }

        // =========================================================
        // ROLE-BASED AUTHORIZATION
        // =========================================================

        @Test
        void student_withValidToken_shouldNotAccessTutorProfile()
                        throws Exception {

                String studentToken = registerStudentAndGetToken();

                mockMvc.perform(
                                get("/api/v1/tutors/profile")
                                                .header("Authorization", bearer(studentToken)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void tutor_withValidToken_shouldNotAccessStudentProfile()
                        throws Exception {

                String tutorToken = registerTutorAndGetToken();

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", bearer(tutorToken)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void student_withValidToken_shouldNotAccessTutorDocuments()
                        throws Exception {

                String studentToken = registerStudentAndGetToken();

                mockMvc.perform(
                                get("/api/v1/tutors/documents")
                                                .header("Authorization", bearer(studentToken)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void tutor_withValidToken_shouldNotAccessStudentPreferences()
                        throws Exception {

                String tutorToken = registerTutorAndGetToken();

                mockMvc.perform(
                                get("/api/v1/students/preferences")
                                                .header("Authorization", bearer(tutorToken)))
                                .andExpect(status().isForbidden());
        }

        // =========================================================
        // PUBLIC ENDPOINTS
        // =========================================================

        @Test
        void subscriptionPlans_withoutToken_shouldBeAccessible()
                        throws Exception {

                mockMvc.perform(get("/api/v1/subscriptions/plans"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void studentRegistration_withoutToken_shouldSucceed()
                        throws Exception {

                String email = uniqueEmail("public-student");

                String requestBody = objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "firstName", "Public",
                                                "lastName", "Student",
                                                "email", email,
                                                "password", "Password123",
                                                "mobileNumber", nextMobileNumber()));

                mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").exists())
                                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"));
        }

        // =========================================================
        // TEST HELPERS
        // =========================================================

        private String registerStudentAndGetToken() throws Exception {

                String email = uniqueEmail("student");

                String requestBody = objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "firstName", "Test",
                                                "lastName", "Student",
                                                "email", email,
                                                "password", "Password123",
                                                "mobileNumber", nextMobileNumber()));

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/student")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").exists())
                                .andReturn();

                return extractToken(result);
        }

        private String registerTutorAndGetToken() throws Exception {

                String email = uniqueEmail("tutor");

                String requestBody = objectMapper.writeValueAsString(
                                java.util.Map.of(
                                                "firstName", "Test",
                                                "lastName", "Tutor",
                                                "email", email,
                                                "password", "Password123",
                                                "mobileNumber", nextMobileNumber()));

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/register/tutor")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").exists())
                                .andReturn();

                return extractToken(result);
        }

        private String extractToken(MvcResult result) throws Exception {

                JsonNode root = objectMapper.readTree(
                                result.getResponse().getContentAsString());

                JsonNode tokenNode = root.path("data").path("token");

                if (tokenNode.isMissingNode()
                                || tokenNode.isNull()
                                || tokenNode.asText().isBlank()) {
                        throw new AssertionError(
                                        "Registration response did not contain data.token");
                }

                return tokenNode.asText();
        }

        private String bearer(String token) {
                return "Bearer " + token;
        }

        private String uniqueEmail(String prefix) {
                return prefix + "-" + UUID.randomUUID() + "@test.com";
        }

        private String nextMobileNumber() {
                return "9" + MOBILE_SEQUENCE.getAndIncrement();
        }

        @Test
        void protectedEndpoint_withTamperedJwt_shouldReturnUnauthorized()
                        throws Exception {

                String validToken = registerStudentAndGetToken();

                String[] parts = validToken.split("\\.");
                assertThat(parts).hasSize(3);

                String modifiedSignature = (parts[2].startsWith("A") ? "B" : "A")
                                + parts[2].substring(1);

                String tamperedToken = parts[0] + "." + parts[1] + "." + modifiedSignature;

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", bearer(tamperedToken)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void protectedEndpoint_withExpiredJwt_shouldReturnUnauthorized()
                        throws Exception {

                User testUser = User.builder()
                                .email("expired-token@test.com")
                                .firstName("Expired")
                                .lastName("User")
                                .role(Role.ROLE_STUDENT)
                                .build();

                JwtService expiredJwtService = new JwtService();

                // Use the same signing key as the application's JwtService.
                Object secretKey = ReflectionTestUtils.getField(
                                jwtService, "secretKey");

                ReflectionTestUtils.setField(
                                expiredJwtService, "secretKey", secretKey);

                // Generate a token whose expiration is in the past.
                ReflectionTestUtils.setField(
                                expiredJwtService, "jwtExpiration", -1000L);

                String expiredToken = expiredJwtService.generateToken(testUser);

                mockMvc.perform(
                                get("/api/v1/students/profile")
                                                .header("Authorization", bearer(expiredToken)))
                                .andExpect(status().isUnauthorized());
        }

}
