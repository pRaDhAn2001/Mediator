package com.mediator.master.repository;

import com.mediator.master.entity.Subject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("SubjectRepository Data JPA Tests")
class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and retrieve subject")
    void saveAndFind_success() {

        Subject subject = Subject.builder()
                .name("Mathematics")
                .build();

        Subject saved = subjectRepository.saveAndFlush(subject);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName())
                .isEqualTo("Mathematics");
    }

    @Test
    @DisplayName("Should find subject by ID")
    void findById_success() {

        Subject subject = Subject.builder()
                .name("Physics")
                .build();

        Subject saved = entityManager.persistAndFlush(subject);

        Optional<Subject> result = subjectRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("Physics");
    }

    @Test
    @DisplayName("Should return empty when subject does not exist")
    void findById_notFound() {

        Optional<Subject> result = subjectRepository.findById(999999L);

        assertThat(result).isEmpty();
    }
}