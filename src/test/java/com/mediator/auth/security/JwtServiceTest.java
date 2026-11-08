package com.mediator.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY = "4f6e7954686973497341566572794c6f6e675365637265744b6579466f724a5754";
    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    private User sampleUser;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);

        signingKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(SECRET_KEY));

        sampleUser = User.builder()
                .id(101L)
                .email("student@test.com")
                .password("encoded-password")
                .role(Role.ROLE_STUDENT)
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    private String createToken(Map<String, Object> claims) {
        Date now = new Date();

        return Jwts.builder()
                .claims(claims)
                .subject("student@test.com")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void extractUserId_integerClaim_returnsLongValue() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123);

        String token = createToken(claims);

        assertThat(jwtService.extractUserId(token)).isEqualTo(123L);
    }

    @Test
    void extractUserId_largeNumericClaim_returnsLongValue() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 3_000_000_000L);

        String token = createToken(claims);

        assertThat(jwtService.extractUserId(token)).isEqualTo(3_000_000_000L);
    }

    @Test
    void extractUserId_stringClaim_usesFallbackConversion() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "456");

        String token = createToken(claims);

        assertThat(jwtService.extractUserId(token)).isEqualTo(456L);
    }

    @Test
    void extractUsername_returnsSubject() {
        String token = createToken(new HashMap<>());

        assertThat(jwtService.extractUsername(token))
                .isEqualTo("student@test.com");
    }

    @Nested
    @DisplayName("Token Generation & Claim Extraction")
    class TokenClaimsTests {

        @Test
        @DisplayName("Should generate token and extract all claims accurately")
        void generateToken_andExtractClaims_success() {
            String token = jwtService.generateToken(sampleUser);

            assertThat(token).isNotBlank();
            assertThat(jwtService.extractUsername(token)).isEqualTo("student@test.com");
            assertThat(jwtService.extractUserId(token)).isEqualTo(101L);
            assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_STUDENT");
            assertThat(jwtService.extractFirstName(token)).isEqualTo("John");
            assertThat(jwtService.extractLastName(token)).isEqualTo("Doe");
            assertThat(jwtService.extractFullName(token)).isEqualTo("John Doe");

            Date expiration = jwtService.extractExpiration(token);
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("Should return expiration in seconds correctly")
        void getExpirationInSeconds_returnsCorrectSeconds() {
            long seconds = jwtService.getExpirationInSeconds();
            assertThat(seconds).isEqualTo(86400L);
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return true for a valid generated token")
        void isTokenValid_validToken_returnsTrue() {
            String token = jwtService.generateToken(sampleUser);
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Should return false for a malformed token string")
        void isTokenValid_malformedToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();
        }

        @Test
        @DisplayName("Should return false for null or empty token")
        void isTokenValid_emptyToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("Should return false when token signature is tampered with")
        void isTokenValid_tamperedToken_returnsFalse() {
            String token = jwtService.generateToken(sampleUser);
            String tampered = token.substring(0, token.length() - 5) + "abcde";
            assertThat(jwtService.isTokenValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("Should return false for an expired token")
        void isTokenValid_expiredToken_returnsFalse() {
            JwtService expiredJwtService = new JwtService();

            ReflectionTestUtils.setField(
                    expiredJwtService, "secretKey", SECRET_KEY);
            ReflectionTestUtils.setField(
                    expiredJwtService, "jwtExpiration", -1000L);

            String expiredToken = expiredJwtService.generateToken(sampleUser);

            assertThat(expiredJwtService.isTokenValid(expiredToken)).isFalse();
        }

    }

    @Test
    void extractUserId_missingClaim_throwsException() {
        String token = createToken(new HashMap<>());

        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void extractUserId_nonNumericString_throwsNumberFormatException() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "not-a-number");

        String token = createToken(claims);

        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(NumberFormatException.class);
    }

}
