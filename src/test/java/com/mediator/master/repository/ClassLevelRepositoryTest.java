package com.mediator.master.repository;

import com.mediator.master.entity.ClassLevel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ClassLevelRepository Data JPA Tests")
class ClassLevelRepositoryTest {

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and retrieve class level")
    void saveAndFind_success() {

        ClassLevel classLevel = ClassLevel.builder()
                .standard(8)
                .build();

        ClassLevel saved = classLevelRepository.saveAndFlush(classLevel);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStandard()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should find class level by ID")
    void findById_success() {

        ClassLevel classLevel = ClassLevel.builder()
                .standard(10)
                .build();

        ClassLevel saved = entityManager.persistAndFlush(classLevel);

        Optional<ClassLevel> result = classLevelRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStandard())
                .isEqualTo(10);
    }

    @Test
    @DisplayName("Should return empty when class level does not exist")
    void findById_notFound() {

        Optional<ClassLevel> result = classLevelRepository.findById(999999L);

        assertThat(result).isEmpty();
    }
}