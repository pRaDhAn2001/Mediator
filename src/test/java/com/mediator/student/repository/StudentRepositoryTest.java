package com.mediator.student.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.common.base.Gender;
import com.mediator.student.entity.Student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("StudentRepository Data JPA Tests")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find student by associated user")
    void findByUser_success() {

        User user = User.builder()
                .email("student@test.com")
                .password("password")
                .firstName("John")
                .lastName("Student")
                .mobileNumber("9876543210")
                .role(Role.ROLE_STUDENT)
                .build();

        entityManager.persist(user);

        Student student = Student.builder()
                .user(user)
                .gender(Gender.MALE)
                .parentName("Parent")
                .parentPhone("9876543211")
                .build();

        entityManager.persistAndFlush(student);

        Optional<Student> result = studentRepository.findByUser(user);

        assertThat(result).isPresent();
        assertThat(result.get().getStudentId())
                .isEqualTo(student.getStudentId());
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("student@test.com");
    }

    @Test
    @DisplayName("Should return empty when user has no student profile")
    void findByUser_notFound() {

        User user = User.builder()
                .email("tutor@test.com")
                .password("password")
                .firstName("Tutor")
                .lastName("User")
                .mobileNumber("9876543210")
                .role(Role.ROLE_TUTOR)
                .build();

        entityManager.persistAndFlush(user);

        Optional<Student> result = studentRepository.findByUser(user);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find student by student ID")
    void findById_success() {

        User user = User.builder()
                .email("student2@test.com")
                .password("password")
                .firstName("Jane")
                .lastName("Student")
                .mobileNumber("9876543211")
                .role(Role.ROLE_STUDENT)
                .build();

        entityManager.persist(user);

        Student student = Student.builder()
                .user(user)
                .gender(Gender.FEMALE)
                .build();

        Student saved = entityManager.persistAndFlush(student);

        Optional<Student> result = studentRepository.findById(saved.getStudentId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("student2@test.com");
    }
}