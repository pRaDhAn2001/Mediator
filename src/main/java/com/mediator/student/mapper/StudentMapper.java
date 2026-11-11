package com.mediator.student.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mediator.common.mapper.AddressMapper;
import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.entity.Student;
import com.mediator.student.entity.StudentPreference;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StudentMapper {

        private final AddressMapper addressMapper;

        private final PreferredModeMapper preferredModeMapper;

        /**
         * Converts Student Entity -> StudentProfileResponse
         */
        public StudentProfileResponse toResponse(
                        Student student,
                        List<StudentPreference> preferences,
                        Integer profileCompletion) {

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

                                .address(
                                                addressMapper.toDto(
                                                                student.getAddress()))

                                .preferredMode(
                                                preferredModeMapper.toDto(
                                                                student.getPreferredMode()))

                                .preferredRadius(
                                                student.getPreferredRadius())

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

                                .profileCompletion(profileCompletion)

                                .build();
        }

        /**
         * Updates existing Student Entity from Request DTO.
         */
        public void updateStudentEntity(
                        Student student,
                        StudentProfileRequest request,
                        Board board,
                        ClassLevel classLevel) {

                student.setGender(request.getGender());

                student.setAddress(
                                addressMapper.toEntity(
                                                request.getAddress()));

                student.setPreferredMode(
                                preferredModeMapper.toEntity(
                                                request.getPreferredMode()));

                student.setPreferredRadius(
                                request.getPreferredRadius());

                student.setBoard(board);

                student.setClassLevel(classLevel);

                student.setSchoolName(
                                request.getSchoolName());

                student.setParentName(
                                request.getParentName());

                student.setParentPhone(
                                request.getParentPhone());

                student.setParentEmail(
                                request.getParentEmail());
        }

        /**
         * Subject -> DTO
         */
        public SubjectResponse toSubjectResponse(Subject subject) {

                return SubjectResponse.builder()
                                .id(subject.getId())
                                .name(subject.getName())
                                .build();
        }
}