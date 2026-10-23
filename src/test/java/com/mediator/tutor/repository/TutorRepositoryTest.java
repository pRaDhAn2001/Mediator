package com.mediator.tutor.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.common.base.Gender;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.VerificationStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TutorRepository Data JPA Tests")
class TutorRepositoryTest {

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find tutor by associated user")
    void findByUser_success() {

        User user = User.builder()
                .email("tutor@test.com")
                .password("password")
                .firstName("John")
                .lastName("Tutor")
                .mobileNumber("9876543210")
                .role(Role.ROLE_TUTOR)
                .build();

        entityManager.persist(user);

        Tutor tutor = Tutor.builder()
                .user(user)
                .gender(Gender.MALE)
                .verificationStatus(
                        VerificationStatus.APPROVED)
                .profileStatus(ProfileStatus.LIVE)
                .build();

        entityManager.persistAndFlush(tutor);

        Optional<Tutor> result = tutorRepository.findByUser(user);

        assertThat(result).isPresent();
        assertThat(result.get().getTutorId())
                .isEqualTo(tutor.getTutorId());
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("tutor@test.com");
    }

    @Test
    @DisplayName("Should return empty when user has no tutor profile")
    void findByUser_notFound() {

        User user = User.builder()
                .email("student@test.com")
                .password("password")
                .firstName("John")
                .lastName("Student")
                .mobileNumber("9876543210")
                .role(Role.ROLE_STUDENT)
                .build();

        entityManager.persistAndFlush(user);

        Optional<Tutor> result = tutorRepository.findByUser(user);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find tutor by tutor ID")
    void findById_success() {

        User user = User.builder()
                .email("tutor2@test.com")
                .password("password")
                .firstName("Jane")
                .lastName("Tutor")
                .mobileNumber("9876543211")
                .role(Role.ROLE_TUTOR)
                .build();

        entityManager.persist(user);

        Tutor tutor = Tutor.builder()
                .user(user)
                .verificationStatus(
                        VerificationStatus.APPROVED)
                .profileStatus(ProfileStatus.LIVE)
                .build();

        Tutor saved = entityManager.persistAndFlush(tutor);

        Optional<Tutor> result = tutorRepository.findById(saved.getTutorId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("tutor2@test.com");
    }
}