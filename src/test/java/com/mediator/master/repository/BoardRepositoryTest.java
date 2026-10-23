package com.mediator.master.repository;

import com.mediator.master.entity.Board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BoardRepository Data JPA Tests")
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and retrieve board")
    void saveAndFind_success() {

        Board board = Board.builder()
                .name("CBSE")
                .build();

        Board saved = boardRepository.saveAndFlush(board);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("CBSE");
    }

    @Test
    @DisplayName("Should find board by ID")
    void findById_success() {

        Board board = Board.builder()
                .name("ICSE")
                .build();

        Board saved = entityManager.persistAndFlush(board);

        Optional<Board> result = boardRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("ICSE");
    }

    @Test
    @DisplayName("Should return empty when board does not exist")
    void findById_notFound() {

        Optional<Board> result = boardRepository.findById(999999L);

        assertThat(result).isEmpty();
    }
}