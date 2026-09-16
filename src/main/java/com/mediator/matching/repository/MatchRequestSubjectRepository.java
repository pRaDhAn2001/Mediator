package com.mediator.matching.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediator.matching.entity.MatchRequestSubject;

@Repository
public interface MatchRequestSubjectRepository
        extends JpaRepository<MatchRequestSubject, Long> {

    List<MatchRequestSubject> findByMatchRequest_RequestId(
            Long requestId);

    List<MatchRequestSubject> findBySubject_Id(Long subjectId);

    List<MatchRequestSubject> findByMatchRequest_RequestIdOrderByRequestSubjectId(
            Long requestId);

    void deleteByMatchRequest_RequestId(
            Long requestId);
}
