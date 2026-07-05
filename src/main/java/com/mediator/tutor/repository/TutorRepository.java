package com.mediator.tutor.repository;

import com.mediator.tutor.entity.Tutor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUser_Email(String email);

    Optional<Tutor> findByUser_Id(Long userId);
}
