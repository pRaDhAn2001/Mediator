
package com.mediator.matching.mapper;

import org.springframework.stereotype.Component;

import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.dto.response.MatchRequestSubjectResponse;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestSubject;

import java.util.List;

@Component
public class MatchRequestMapper {

        public MatchRequestResponse toStudentResponse(MatchRequest request) {

                return MatchRequestResponse.builder()
                                .requestId(request.getRequestId())

                                .studentId(request.getStudent().getStudentId())
                                .studentName(getFullName(
                                                request.getStudent().getUser().getFirstName(),
                                                request.getStudent().getUser().getLastName()))

                                .tutorId(request.getTutor().getTutorId())
                                .tutorName(getFullName(
                                                request.getTutor().getUser().getFirstName(),
                                                request.getTutor().getUser().getLastName()))

                                .status(request.getStatus())
                                .message(request.getMessage())

                                .requestedAt(request.getCreatedAt())
                                .connectedAt(request.getConnectedAt())
                                .finalizedAt(request.getFinalizedAt())

                                .subjects(toSubjectResponses(request))

                                // Student must NOT receive tutor contact information
                                .studentPhone(null)
                                .studentEmail(null)

                                .build();
        }

        public MatchRequestResponse toTutorResponse(MatchRequest request) {

                return MatchRequestResponse.builder()
                                .requestId(request.getRequestId())

                                .studentId(request.getStudent().getStudentId())
                                .studentName(getFullName(
                                                request.getStudent().getUser().getFirstName(),
                                                request.getStudent().getUser().getLastName()))

                                .tutorId(request.getTutor().getTutorId())
                                .tutorName(getFullName(
                                                request.getTutor().getUser().getFirstName(),
                                                request.getTutor().getUser().getLastName()))

                                .status(request.getStatus())
                                .message(request.getMessage())

                                .requestedAt(request.getCreatedAt())
                                .connectedAt(request.getConnectedAt())
                                .finalizedAt(request.getFinalizedAt())

                                /*
                                 * Tutor is allowed to contact the student
                                 * after receiving the request.
                                 */
                                .studentPhone(request.getStudent().getUser().getMobileNumber())
                                .studentEmail(request.getStudent().getUser().getEmail())

                                .subjects(toSubjectResponses(request))

                                .build();
        }

        private List<MatchRequestSubjectResponse> toSubjectResponses(
                        MatchRequest request) {

                return request.getRequestedSubjects()
                                .stream()
                                .map(this::toSubjectResponse)
                                .toList();
        }

        private MatchRequestSubjectResponse toSubjectResponse(
                        MatchRequestSubject subject) {

                return MatchRequestSubjectResponse.builder()
                                .subjectId(subject.getSubject().getId())
                                .subjectName(subject.getSubject().getName())
                                .status(subject.getStatus())
                                .build();
        }

        private String getFullName(String firstName, String lastName) {

                String first = firstName == null ? "" : firstName.trim();
                String last = lastName == null ? "" : lastName.trim();

                return (first + " " + last).trim();
        }
}