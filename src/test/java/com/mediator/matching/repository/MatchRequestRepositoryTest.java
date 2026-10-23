
package com.mediator.matching.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.repository.TutorRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MatchRequestRepositoryTest {

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private StudentRepository studentRepository;

    private int userCounter = 0;

    private User createUser(Role role) {
        userCounter++;
        String suffix = String.valueOf(userCounter);

        User user = User.builder()
                .email("user" + suffix + "@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .mobileNumber(
                        (role == Role.ROLE_TUTOR ? "9" : "8")
                                + String.format(
                                        "%09d",
                                        userCounter))
                .role(role)
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Tutor createTutor() {
        User user = createUser(Role.ROLE_TUTOR);

        return tutorRepository.saveAndFlush(
                Tutor.builder()
                        .user(user)
                        .build());
    }

    private Student createStudent() {
        User user = createUser(Role.ROLE_STUDENT);

        return studentRepository.saveAndFlush(
                Student.builder()
                        .user(user)
                        .build());
    }

    private MatchRequest createRequest(
            Student student,
            Tutor tutor,
            MatchRequestStatus status) {
        MatchRequest request = MatchRequest.builder()
                .student(student)
                .tutor(tutor)
                .status(status)
                .message("Repository test request")
                .build();

        return matchRequestRepository.saveAndFlush(request);
    }

    @Test
    void findByStudentStudentId_shouldReturnRequestsForStudent() {
        Student student = createStudent();
        Tutor tutor1 = createTutor();
        Tutor tutor2 = createTutor();
        Student anotherStudent = createStudent();

        MatchRequest first = createRequest(
                student, tutor1, MatchRequestStatus.REQUESTED);

        MatchRequest second = createRequest(
                student, tutor2, MatchRequestStatus.CONNECTED);

        createRequest(
                anotherStudent, tutor1, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository.findByStudent_StudentId(
                        student.getStudentId()))
                .extracting(MatchRequest::getRequestId)
                .containsExactlyInAnyOrder(
                        first.getRequestId(),
                        second.getRequestId());
    }

    @Test
    void findByStudentStudentId_shouldReturnEmptyWhenNoRequestsExist() {
        Student student = createStudent();

        assertThat(
                matchRequestRepository.findByStudent_StudentId(
                        student.getStudentId()))
                .isEmpty();
    }

    @Test
    void findByTutorTutorId_shouldReturnRequestsForTutor() {
        Student student1 = createStudent();
        Student student2 = createStudent();
        Tutor tutor = createTutor();
        Tutor anotherTutor = createTutor();

        MatchRequest first = createRequest(
                student1, tutor, MatchRequestStatus.REQUESTED);

        MatchRequest second = createRequest(
                student2, tutor, MatchRequestStatus.CONNECTED);

        createRequest(
                student1, anotherTutor, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository.findByTutor_TutorId(
                        tutor.getTutorId()))
                .extracting(MatchRequest::getRequestId)
                .containsExactlyInAnyOrder(
                        first.getRequestId(),
                        second.getRequestId());
    }

    @Test
    void findByTutorTutorId_shouldReturnEmptyWhenNoRequestsExist() {
        Tutor tutor = createTutor();

        assertThat(
                matchRequestRepository.findByTutor_TutorId(
                        tutor.getTutorId()))
                .isEmpty();
    }

    @Test
    void findByStudentAndStatus_shouldReturnOnlyMatchingStatus() {
        Student student = createStudent();
        Tutor tutor1 = createTutor();
        Tutor tutor2 = createTutor();

        createRequest(
                student, tutor1, MatchRequestStatus.REQUESTED);

        MatchRequest connected = createRequest(
                student, tutor2, MatchRequestStatus.CONNECTED);

        assertThat(
                matchRequestRepository.findByStudent_StudentIdAndStatus(
                        student.getStudentId(),
                        MatchRequestStatus.CONNECTED))
                .extracting(MatchRequest::getRequestId)
                .containsExactly(connected.getRequestId());
    }

    @Test
    void findByStudentAndStatus_shouldReturnEmptyWhenStatusDoesNotMatch() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        createRequest(
                student, tutor, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository.findByStudent_StudentIdAndStatus(
                        student.getStudentId(),
                        MatchRequestStatus.CANCELLED))
                .isEmpty();
    }

    @Test
    void findByTutorAndStatus_shouldReturnOnlyMatchingStatus() {
        Student student1 = createStudent();
        Student student2 = createStudent();
        Tutor tutor = createTutor();

        createRequest(
                student1, tutor, MatchRequestStatus.REQUESTED);

        MatchRequest connected = createRequest(
                student2, tutor, MatchRequestStatus.CONNECTED);

        assertThat(
                matchRequestRepository.findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        MatchRequestStatus.CONNECTED))
                .extracting(MatchRequest::getRequestId)
                .containsExactly(connected.getRequestId());
    }

    @Test
    void findByTutorAndStatus_shouldReturnEmptyWhenStatusDoesNotMatch() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        createRequest(
                student, tutor, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository.findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        MatchRequestStatus.CANCELLED))
                .isEmpty();
    }

    @Test
    void countByStudentAndStatus_shouldReturnCorrectCount() {
        Student student = createStudent();

        createRequest(
                student, createTutor(), MatchRequestStatus.REQUESTED);

        createRequest(
                student, createTutor(), MatchRequestStatus.REQUESTED);

        createRequest(
                student, createTutor(), MatchRequestStatus.CONNECTED);

        assertThat(
                matchRequestRepository.countByStudent_StudentIdAndStatus(
                        student.getStudentId(),
                        MatchRequestStatus.REQUESTED))
                .isEqualTo(2);
    }

    @Test
    void countByStudentAndStatus_shouldReturnZeroWhenNoMatchesExist() {
        Student student = createStudent();

        assertThat(
                matchRequestRepository.countByStudent_StudentIdAndStatus(
                        student.getStudentId(),
                        MatchRequestStatus.REQUESTED))
                .isZero();
    }

    @Test
    void countByTutorAndStatus_shouldReturnCorrectCount() {
        Tutor tutor = createTutor();

        createRequest(
                createStudent(), tutor, MatchRequestStatus.REQUESTED);

        createRequest(
                createStudent(), tutor, MatchRequestStatus.REQUESTED);

        createRequest(
                createStudent(), tutor, MatchRequestStatus.CONNECTED);

        assertThat(
                matchRequestRepository.countByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        MatchRequestStatus.REQUESTED))
                .isEqualTo(2);
    }

    @Test
    void countByTutorAndStatus_shouldReturnZeroWhenNoMatchesExist() {
        Tutor tutor = createTutor();

        assertThat(
                matchRequestRepository.countByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        MatchRequestStatus.REQUESTED))
                .isZero();
    }

    @Test
    void existsByStudentTutorAndStatusIn_shouldReturnTrueForMatchingStatus() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        createRequest(
                student, tutor, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository
                        .existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
                                student.getStudentId(),
                                tutor.getTutorId(),
                                List.of(
                                        MatchRequestStatus.REQUESTED,
                                        MatchRequestStatus.CONNECTED)))
                .isTrue();
    }

    @Test
    void existsByStudentTutorAndStatusIn_shouldReturnFalseForNonMatchingStatus() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        createRequest(
                student, tutor, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository
                        .existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
                                student.getStudentId(),
                                tutor.getTutorId(),
                                List.of(MatchRequestStatus.CANCELLED)))
                .isFalse();
    }

    @Test
    void existsByStudentTutorAndStatusIn_shouldReturnFalseForDifferentTutor() {
        Student student = createStudent();
        Tutor tutor1 = createTutor();
        Tutor tutor2 = createTutor();

        createRequest(
                student, tutor1, MatchRequestStatus.REQUESTED);

        assertThat(
                matchRequestRepository
                        .existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
                                student.getStudentId(),
                                tutor2.getTutorId(),
                                List.of(MatchRequestStatus.REQUESTED)))
                .isFalse();
    }
}
