package com.mediator.tutor.repository;

import com.mediator.tutor.entity.TutorDocument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorDocumentRepository extends JpaRepository<TutorDocument, Long> {

    List<TutorDocument> findByTutor_TutorId(Long tutorId);

    Optional<TutorDocument> findByDocumentIdAndTutor_TutorId(
            Long documentId,
            Long tutorId);

    void deleteByDocumentIdAndTutor_TutorId(
            Long documentId,
            Long tutorId);

}
