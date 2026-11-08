package com.mediator.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mediator.auth.dto.request.LoginRequest;
import com.mediator.auth.dto.request.RegisterStudentRequest;
import com.mediator.auth.dto.request.RegisterTutorRequest;
import com.mediator.auth.dto.response.LoginResponse;
import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.auth.security.JwtService;
import com.mediator.common.exception.AuthenticationException;
import com.mediator.common.exception.DuplicateResourceException;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .mobileNumber("9876543210")
                .role(Role.ROLE_STUDENT)
                .build();
    }

    @Nested
    @DisplayName("registerStudent()")
    class RegisterStudentTests {

        private RegisterStudentRequest request;

        @BeforeEach
        void setUp() {
            request = RegisterStudentRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("student@example.com")
                    .mobileNumber("9876543210")
                    .password("rawPassword123")
                    .build();
        }

        @Test
        @DisplayName("Should register student successfully and return LoginResponse")
        void registerStudent_success() {
            when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNumber("9876543210")).thenReturn(false);
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtService.generateToken(sampleUser)).thenReturn("sample.jwt.token");
            when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

            LoginResponse response = authService.registerStudent(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("sample.jwt.token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_STUDENT);
            assertThat(response.getExpiresIn()).isEqualTo(86400L);

            verify(studentRepository).save(any(Student.class));
            verify(userRepository).save(argThat(u -> u.getRole() == Role.ROLE_STUDENT));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void registerStudent_duplicateEmail_throwsException() {
            when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerStudent(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email is already registered.");

            verify(userRepository, never()).save(any(User.class));
            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when mobile number already exists")
        void registerStudent_duplicateMobile_throwsException() {
            when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNumber("9876543210")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerStudent(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Mobile number is already registered.");

            verify(userRepository, never()).save(any(User.class));
            verify(studentRepository, never()).save(any(Student.class));
        }
    }

    @Nested
    @DisplayName("registerTutor()")
    class RegisterTutorTests {

        private RegisterTutorRequest request;

        @BeforeEach
        void setUp() {
            request = RegisterTutorRequest.builder()
                    .firstName("Jane")
                    .lastName("Tutor")
                    .email("tutor@example.com")
                    .mobileNumber("9876543211")
                    .password("rawPassword123")
                    .build();
        }

        @Test
        @DisplayName("Should register tutor with PENDING verification & DRAFT status")
        void registerTutor_success() {
            User tutorUser = User.builder()
                    .id(2L)
                    .email("tutor@example.com")
                    .password("encodedPassword")
                    .firstName("Jane")
                    .lastName("Tutor")
                    .mobileNumber("9876543211")
                    .role(Role.ROLE_TUTOR)
                    .build();

            when(userRepository.existsByEmail("tutor@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNumber("9876543211")).thenReturn(false);
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(tutorUser);
            when(jwtService.generateToken(tutorUser)).thenReturn("sample.tutor.jwt.token");
            when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

            LoginResponse response = authService.registerTutor(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("sample.tutor.jwt.token");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_TUTOR);

            verify(tutorRepository).save(argThat(t -> 
                    t.getVerificationStatus() == VerificationStatus.PENDING &&
                    t.getProfileStatus() == ProfileStatus.DRAFT &&
                    t.getUser().equals(tutorUser)
            ));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when tutor email already exists")
        void registerTutor_duplicateEmail_throwsException() {
            when(userRepository.existsByEmail("tutor@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerTutor(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email is already registered.");

            verify(tutorRepository, never()).save(any(Tutor.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when tutor mobile already exists")
        void registerTutor_duplicateMobile_throwsException() {
            when(userRepository.existsByEmail("tutor@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNumber("9876543211")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerTutor(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Mobile number is already registered.");

            verify(tutorRepository, never()).save(any(Tutor.class));
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        private LoginRequest request;

        @BeforeEach
        void setUp() {
            request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("Password123")
                    .build();
        }

        @Test
        @DisplayName("Should authenticate and return LoginResponse with valid credentials")
        void login_success() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser)).thenReturn("login.jwt.token");
            when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

            LoginResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("login.jwt.token");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_STUDENT);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should throw AuthenticationException on bad credentials")
        void login_badCredentials_throwsAuthenticationException() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid email or password.");

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should throw AuthenticationException when authenticated user not found in repository")
        void login_userNotFound_throwsAuthenticationException() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("User not found.");
        }
    }
}
