package com.mediator.matching.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;

@Repository
public interface MatchRequestRepository
        extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByStudent_StudentId(Long studentId);

    List<MatchRequest> findByTutor_TutorId(Long tutorId);

    List<MatchRequest> findByStudent_StudentIdAndStatus(
            Long studentId,
            MatchRequestStatus status);

    List<MatchRequest> findByTutor_TutorIdAndStatus(
            Long tutorId,
            MatchRequestStatus status);

    long countByStudent_StudentIdAndStatus(
            Long studentId,
            MatchRequestStatus status);

    long countByTutor_TutorIdAndStatus(
            Long tutorId,
            MatchRequestStatus status);

    boolean existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
            Long studentId,
            Long tutorId,
            List<MatchRequestStatus> statuses);
}