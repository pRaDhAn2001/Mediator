package com.mediator.master.repository;

import com.mediator.master.entity.Board;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    boolean existsByName(String name);

    Optional<Board> findByName(String name);
}
