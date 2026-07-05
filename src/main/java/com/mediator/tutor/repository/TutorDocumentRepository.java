package com.mediator.tutor.repository;

import com.mediator.tutor.entity.TutorDocument;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorDocumentRepository extends JpaRepository<TutorDocument, Long> {

    List<TutorDocument> findByTutor_TutorId(Long tutorId);

}
