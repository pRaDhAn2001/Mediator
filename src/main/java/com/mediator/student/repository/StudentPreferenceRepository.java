package com.mediator.student.repository;

import com.mediator.student.entity.StudentPreference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Long> {

    List<StudentPreference> findByStudent_StudentId(Long studentId);

    List<StudentPreference> findBySubject_Id(Long subjectId);
}