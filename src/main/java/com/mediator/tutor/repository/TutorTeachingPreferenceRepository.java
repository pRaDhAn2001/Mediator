package com.mediator.tutor.repository;

import java.util.List;

import com.mediator.tutor.entity.TutorTeachingPreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorTeachingPreferenceRepository
        extends JpaRepository<TutorTeachingPreference, Long> {

    List<TutorTeachingPreference> findByTutor_TutorId(Long tutorId);

    List<TutorTeachingPreference> findBySubject_Id(Long subjectId);

    List<TutorTeachingPreference> findByClassLevel_Id(Long classLevelId);

    List<TutorTeachingPreference> findByBoard_Id(Long boardId);

    void deleteByTutor_TutorId(Long tutorId);
}
