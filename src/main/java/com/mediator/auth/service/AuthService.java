package com.mediator.auth.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final StudentRepository studentRepository;

    private final TutorRepository tutorRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    /**
     * Student Registration
     */
    @Transactional
    public LoginResponse registerStudent(RegisterStudentRequest request) {

        User savedUser = createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobileNumber(),
                request.getPassword(),
                Role.ROLE_STUDENT);

        Student student = Student.builder()
                .user(savedUser)
                .build();

        studentRepository.save(student);

        return buildLoginResponse(savedUser);
    }

    /**
     * Tutor Registration
     */
    @Transactional
    public LoginResponse registerTutor(RegisterTutorRequest request) {

        User savedUser = createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getMobileNumber(),
                request.getPassword(),
                Role.ROLE_TUTOR);

        Tutor tutor = Tutor.builder()
                .user(savedUser)
                .verificationStatus(VerificationStatus.PENDING)
                .profileStatus(ProfileStatus.DRAFT)
                .build();

        tutorRepository.save(tutor);

        return buildLoginResponse(savedUser);
    }

    /**
     * Login
     */
    public LoginResponse login(LoginRequest request) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

        } catch (org.springframework.security.core.AuthenticationException ex) {

            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found."));

        return buildLoginResponse(user);
    }

    /**
     * Common method for creating user.
     */
    private User createUser(
            String firstName,
            String lastName,
            String email,
            String mobileNumber,
            String password,
            Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered.");
        }

        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new DuplicateResourceException("Mobile number is already registered.");
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .mobileNumber(mobileNumber)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user) {

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
    }
}