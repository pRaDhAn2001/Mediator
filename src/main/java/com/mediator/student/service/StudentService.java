package com.mediator.student.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.dashboard.StudentDashboardResponse;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.entity.ContactOwner;
import com.mediator.student.entity.Student;
import com.mediator.student.entity.StudentPreference;
import com.mediator.student.mapper.StudentMapper;
import com.mediator.student.repository.StudentPreferenceRepository;
import com.mediator.student.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    private final StudentPreferenceRepository studentPreferenceRepository;

    private final SubjectRepository subjectRepository;

    private final BoardRepository boardRepository;

    private final ClassLevelRepository classLevelRepository;

    private final StudentMapper studentMapper;

    /**
     * Returns logged-in student's profile.
     */
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile() {

        Student student = getLoggedInStudent();

        List<StudentPreference> preferences = studentPreferenceRepository.findByStudent_StudentId(
                student.getStudentId());

        Integer completion = calculateProfileCompletion(student, preferences);

        return studentMapper.toResponse(
                student,
                preferences,
                completion);
    }

    /**
     * Dashboard API.
     */
    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard() {

        StudentProfileResponse profile = getProfile();

        /*
         * Match module isn't implemented yet.
         * Keeping these values as zero.
         */

        return StudentDashboardResponse.builder()
                .profile(profile)
                .profileCompletion(profile.getProfileCompletion())
                .activeMatchesCount(0L)
                .pendingMatchesCount(0L)
                .cancelledMatchesCount(0L)
                .build();
    }

    /**
     * Returns currently authenticated student.
     */
    private Student getLoggedInStudent() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return studentRepository
                .findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found."));
    }

    /**
     * Updates student's profile.
     */
    public StudentProfileResponse updateProfile(StudentProfileRequest request) {

        Student student = getLoggedInStudent();

        /*
         * Validate Board
         */
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Board not found with id : "
                                + request.getBoardId()));

        /*
         * Validate Class
         */
        ClassLevel classLevel = classLevelRepository.findById(request.getClassLevelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Class Level not found with id : "
                                + request.getClassLevelId()));

        /*
         * Update Student Entity
         */
        studentMapper.updateStudentEntity(
                student,
                request,
                board,
                classLevel);

        /*
         * Handle Parent Phone
         */
        if (Boolean.TRUE.equals(request.getSameAsPrimaryPhone())) {

            student.setParentPhone(
                    student.getUser().getMobileNumber());

            student.setPrimaryMobileOwner(ContactOwner.SELF);

        } else {

            student.setParentPhone(request.getParentPhone());

            student.setPrimaryMobileOwner(ContactOwner.PARENT);
        }

        /*
         * Handle Parent Email
         */
        if (Boolean.TRUE.equals(request.getSameAsPrimaryEmail())) {

            student.setParentEmail(
                    student.getUser().getEmail());

            student.setPrimaryEmailOwner(ContactOwner.SELF);

        } else {

            student.setParentEmail(request.getParentEmail());

            student.setPrimaryEmailOwner(ContactOwner.PARENT);
        }

        /*
         * Save Student
         */
        studentRepository.save(student);

        /*
         * Replace Subject Preferences
         */
        replaceStudentSubjects(
                student,
                request.getSubjectIds());

        /*
         * Return Updated Profile
         */
        List<StudentPreference> preferences = studentPreferenceRepository.findByStudent_StudentId(
                student.getStudentId());

        Integer completion = calculateProfileCompletion(
                student,
                preferences);

        return studentMapper.toResponse(
                student,
                preferences,
                completion);
    }

    /**
     * Replaces student's preferred subjects.
     */
    private void replaceStudentSubjects(
            Student student,
            List<Long> subjectIds) {

        studentPreferenceRepository.deleteByStudent(student);

        if (subjectIds == null || subjectIds.isEmpty()) {
            return;
        }

        List<Subject> subjects = loadSubjects(subjectIds);

        List<StudentPreference> preferences = subjects.stream()
                .map(subject -> {

                    StudentPreference preference = new StudentPreference();

                    preference.setStudent(student);

                    preference.setSubject(subject);

                    return preference;

                })
                .toList();

        studentPreferenceRepository.saveAll(preferences);
    }

    /**
     * Calculates profile completion percentage.
     */
    private Integer calculateProfileCompletion(
            Student student,
            List<StudentPreference> preferences) {

        int completed = 0;
        final int totalFields = 11;

        if (student.getGender() != null)
            completed++;

        if (student.getAddress() != null)
            completed++;

        if (student.getPreferredMode() != null)
            completed++;

        if (student.getPreferredRadius() != null)
            completed++;

        if (student.getClassLevel() != null)
            completed++;

        if (student.getBoard() != null)
            completed++;

        if (student.getSchoolName() != null &&
                !student.getSchoolName().isBlank())
            completed++;

        if (student.getParentName() != null &&
                !student.getParentName().isBlank())
            completed++;

        if (student.getParentPhone() != null &&
                !student.getParentPhone().isBlank())
            completed++;

        if (student.getParentEmail() != null &&
                !student.getParentEmail().isBlank())
            completed++;

        if (preferences != null && !preferences.isEmpty())
            completed++;

        return (completed * 100) / totalFields;
    }

    /**
     * Loads all subjects from database.
     */
    private List<Subject> loadSubjects(List<Long> subjectIds) {

        List<Subject> subjects = subjectRepository.findAllById(subjectIds);

        validateSubjects(subjectIds, subjects);

        return subjects;
    }

    /**
     * Validates requested subjects.
     */
    private void validateSubjects(
            List<Long> requestedIds,
            List<Subject> subjects) {

        if (requestedIds.size() != subjects.size()) {

            throw new ResourceNotFoundException(
                    "One or more selected subjects do not exist.");
        }
    }
}
