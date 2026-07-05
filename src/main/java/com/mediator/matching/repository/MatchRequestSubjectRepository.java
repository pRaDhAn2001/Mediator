package com.mediator.matching.repository;

import com.mediator.matching.entity.MatchRequestSubject;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRequestSubjectRepository
        extends JpaRepository<MatchRequestSubject, Long> {

    List<MatchRequestSubject> findByMatchRequest_RequestId(Long requestId);

    List<MatchRequestSubject> findBySubject_Id(Long subjectId);
}
