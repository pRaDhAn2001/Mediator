package com.mediator.auth.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Data JPA Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_success() {

        User user = User.builder()
                .email("student@test.com")
                .password("encoded-password")
                .firstName("John")
                .lastName("Student")
                .mobileNumber("9876543210")
                .role(Role.ROLE_STUDENT)
                .build();

        entityManager.persistAndFlush(user);

        Optional<User> result = userRepository.findByEmail("student@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("student@test.com");
        assertThat(result.get().getFirstName())
                .isEqualTo("John");
        assertThat(result.get().getRole())
                .isEqualTo(Role.ROLE_STUDENT);
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_notFound() {

        Optional<User> result = userRepository.findByEmail("unknown@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should distinguish users by email")
    void findByEmail_shouldReturnCorrectUser() {

        User student = User.builder()
                .email("student@test.com")
                .password("password")
                .firstName("Student")
                .lastName("User")
                .mobileNumber("9876543210")
                .role(Role.ROLE_STUDENT)
                .build();

        User tutor = User.builder()
                .email("tutor@test.com")
                .password("password")
                .firstName("Tutor")
                .lastName("User")
                .mobileNumber("9876543211")
                .role(Role.ROLE_TUTOR)
                .build();

        entityManager.persist(student);
        entityManager.persistAndFlush(tutor);

        Optional<User> result = userRepository.findByEmail("tutor@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRole())
                .isEqualTo(Role.ROLE_TUTOR);
        assertThat(result.get().getFirstName())
                .isEqualTo("Tutor");
    }
}