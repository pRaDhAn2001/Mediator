package com.mediator.student.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.student.entity.Student;
import com.mediator.student.entity.StudentPreference;

@Component
public class StudentMapper {

    /**
     * Entity -> Response DTO
     */
    public StudentProfileResponse toResponse(
            Student student,
            List<StudentPreference> preferences,
            Integer completionPercentage) {

        List<SubjectResponse> subjects = preferences.stream()
                .map(StudentPreference::getSubject)
                .map(this::toSubjectResponse)
                .collect(Collectors.toList());

        return StudentProfileResponse.builder()
                .firstName(student.getUser().getFirstName())
                .lastName(student.getUser().getLastName())
                .email(student.getUser().getEmail())
                .mobileNumber(student.getUser().getMobileNumber())

                .gender(student.getGender())
                .address(student.getAddress())
                .preferredMode(student.getPreferredMode())
                .preferredRadius(student.getPreferredRadius())

                .classLevelId(
                        student.getClassLevel() != null
                                ? student.getClassLevel().getId()
                                : null)

                .classStandard(
                        student.getClassLevel() != null
                                ? student.getClassLevel().getStandard()
                                : null)

                .boardId(
                        student.getBoard() != null
                                ? student.getBoard().getId()
                                : null)

                .boardName(
                        student.getBoard() != null
                                ? student.getBoard().getName()
                                : null)

                .schoolName(student.getSchoolName())

                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .parentEmail(student.getParentEmail())

                .subjects(subjects)

                .profileCompletion(completionPercentage)

                .build();
    }

    /**
     * Request DTO -> Entity
     *
     * Copies only profile fields.
     * User details are updated separately.
     */
    public void updateStudentEntity(
            Student student,
            StudentProfileRequest request,
            Board board,
            ClassLevel classLevel) {

        student.setGender(request.getGender());

        student.setAddress(request.getAddress());

        student.setPreferredMode(request.getPreferredMode());

        student.setPreferredRadius(request.getPreferredRadius());

        student.setBoard(board);

        student.setClassLevel(classLevel);

        student.setSchoolName(request.getSchoolName());

        student.setParentName(request.getParentName());
    }

    /**
     * Subject -> DTO
     */
    private SubjectResponse toSubjectResponse(Subject subject) {

        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }
}