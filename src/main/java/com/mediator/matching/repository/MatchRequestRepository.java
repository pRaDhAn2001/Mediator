package com.mediator.matching.repository;

import com.mediator.matching.entity.MatchRequest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRequestRepository
        extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByStudent_StudentId(Long studentId);

    List<MatchRequest> findByTutor_TutorId(Long tutorId);
}
