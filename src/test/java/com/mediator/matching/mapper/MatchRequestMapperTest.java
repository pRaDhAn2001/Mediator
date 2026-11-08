package com.mediator.matching.mapper;

import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.matching.entity.MatchRequestSubject;
import com.mediator.matching.entity.SubjectRequestStatus;
import com.mediator.student.entity.Student;
import com.mediator.tutor.entity.Tutor;
import com.mediator.auth.entity.User;
import com.mediator.master.entity.Subject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchRequestMapperTest {

    private final MatchRequestMapper mapper = new MatchRequestMapper();

    @Test
    void toStudentResponse_shouldHideStudentContactInformation() {
        MatchRequest request = createRequest();

        MatchRequestResponse response = mapper.toStudentResponse(request);

        assertEquals(100L, response.getRequestId());
        assertEquals(10L, response.getStudentId());
        assertEquals("Asha Sharma", response.getStudentName());
        assertEquals(20L, response.getTutorId());
        assertEquals("Rahul Das", response.getTutorName());
        assertNull(response.getStudentPhone());
        assertNull(response.getStudentEmail());
        assertEquals("Please contact me", response.getMessage());
        assertEquals(MatchRequestStatus.REQUESTED, response.getStatus());
        assertEquals(1, response.getSubjects().size());
        assertEquals(1L, response.getSubjects().get(0).getSubjectId());
        assertEquals("Mathematics", response.getSubjects().get(0).getSubjectName());
        assertEquals(
                SubjectRequestStatus.REQUESTED,
                response.getSubjects().get(0).getStatus());
    }

    @Test
    void toTutorResponse_shouldIncludeStudentContactInformation() {
        MatchRequest request = createRequest();

        MatchRequestResponse response = mapper.toTutorResponse(request);

        assertEquals("9876543210", response.getStudentPhone());
        assertEquals("student@example.com", response.getStudentEmail());
        assertEquals("Asha Sharma", response.getStudentName());
        assertEquals("Rahul Das", response.getTutorName());
    }

    @Test
    void toStudentResponse_shouldTrimNamesAndHandleNullNameParts() {
        MatchRequest request = createRequest();

        when(request.getStudent().getUser().getFirstName()).thenReturn(" Asha ");
        when(request.getStudent().getUser().getLastName()).thenReturn(null);
        when(request.getTutor().getUser().getFirstName()).thenReturn(null);
        when(request.getTutor().getUser().getLastName()).thenReturn(" Das ");

        MatchRequestResponse response = mapper.toStudentResponse(request);

        assertEquals("Asha", response.getStudentName());
        assertEquals("Das", response.getTutorName());
    }

    @Test
    void toStudentResponse_shouldMapRequestTimestamps() {
        MatchRequest request = createRequest();

        LocalDateTime requestedAt = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime connectedAt = LocalDateTime.of(2026, 10, 1, 11, 0);
        LocalDateTime finalizedAt = LocalDateTime.of(2026, 10, 2, 12, 0);

        when(request.getCreatedAt()).thenReturn(requestedAt);
        when(request.getConnectedAt()).thenReturn(connectedAt);
        when(request.getFinalizedAt()).thenReturn(finalizedAt);

        MatchRequestResponse response = mapper.toStudentResponse(request);

        assertEquals(requestedAt, response.getRequestedAt());
        assertEquals(connectedAt, response.getConnectedAt());
        assertEquals(finalizedAt, response.getFinalizedAt());
    }

    private MatchRequest createRequest() {
        User studentUser = mock(User.class);
        when(studentUser.getFirstName()).thenReturn("Asha");
        when(studentUser.getLastName()).thenReturn("Sharma");
        when(studentUser.getMobileNumber()).thenReturn("9876543210");
        when(studentUser.getEmail()).thenReturn("student@example.com");

        Student student = mock(Student.class);
        when(student.getStudentId()).thenReturn(10L);
        when(student.getUser()).thenReturn(studentUser);

        User tutorUser = mock(User.class);
        when(tutorUser.getFirstName()).thenReturn("Rahul");
        when(tutorUser.getLastName()).thenReturn("Das");

        Tutor tutor = mock(Tutor.class);
        when(tutor.getTutorId()).thenReturn(20L);
        when(tutor.getUser()).thenReturn(tutorUser);

        Subject subject = mock(Subject.class);
        when(subject.getId()).thenReturn(1L);
        when(subject.getName()).thenReturn("Mathematics");

        MatchRequestSubject requestSubject = mock(MatchRequestSubject.class);
        when(requestSubject.getSubject()).thenReturn(subject);
        when(requestSubject.getStatus())
                .thenReturn(SubjectRequestStatus.REQUESTED);

        MatchRequest request = mock(MatchRequest.class);
        when(request.getRequestId()).thenReturn(100L);
        when(request.getStudent()).thenReturn(student);
        when(request.getTutor()).thenReturn(tutor);
        when(request.getStatus()).thenReturn(MatchRequestStatus.REQUESTED);
        when(request.getMessage()).thenReturn("Please contact me");
        when(request.getRequestedSubjects()).thenReturn(List.of(requestSubject));

        return request;
    }
}
