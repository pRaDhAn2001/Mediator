
package com.mediator.matching.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.matching.entity.MatchRequestSubject;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.repository.TutorRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MatchRequestSubjectRepositoryTest {

    @Autowired
    private MatchRequestSubjectRepository matchRequestSubjectRepository;

    @Autowired
    private MatchRequestRepository matchRequestRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --------------------------------------------------
    // Helper methods
    // --------------------------------------------------

    private User createUser(String email, String mobile, Role role) {
        User user = User.builder()
                .email(email)
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .mobileNumber(mobile)
                .role(role)
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Tutor createTutor() {
        User user = createUser(
                "tutor@example.com",
                "9000000001",
                Role.ROLE_TUTOR);

        Tutor tutor = Tutor.builder()
                .user(user)
                .build();

        return tutorRepository.saveAndFlush(tutor);
    }

    private Student createStudent() {
        User user = createUser(
                "student@example.com",
                "8000000001",
                Role.ROLE_STUDENT);

        Student student = Student.builder()
                .user(user)
                .build();

        return studentRepository.saveAndFlush(student);
    }

    private Subject createSubject(String name) {
        Subject subject = Subject.builder()
                .name(name)
                .build();

        return subjectRepository.saveAndFlush(subject);
    }

    private MatchRequest createMatchRequest(
            Student student,
            Tutor tutor) {
        MatchRequest request = MatchRequest.builder()
                .student(student)
                .tutor(tutor)
                .status(MatchRequestStatus.REQUESTED)
                .message("Matching repository test")
                .build();

        return matchRequestRepository.saveAndFlush(request);
    }

    private MatchRequestSubject createRequestSubject(
            MatchRequest request,
            Subject subject) {
        MatchRequestSubject requestSubject = MatchRequestSubject.builder()
                .matchRequest(request)
                .subject(subject)
                .build();

        return matchRequestSubjectRepository
                .saveAndFlush(requestSubject);
    }

    // --------------------------------------------------
    // 1. findByMatchRequest_RequestId
    // --------------------------------------------------

    @Test
    void findByMatchRequestRequestId_shouldReturnSubjectsForRequest() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request = createMatchRequest(student, tutor);

        Subject mathematics = createSubject("Mathematics");
        Subject science = createSubject("Science");

        MatchRequestSubject first = createRequestSubject(
                request, mathematics);

        MatchRequestSubject second = createRequestSubject(
                request, science);

        List<MatchRequestSubject> results = matchRequestSubjectRepository
                .findByMatchRequest_RequestId(
                        request.getRequestId());

        assertThat(results)
                .hasSize(2)
                .extracting(MatchRequestSubject::getRequestSubjectId)
                .containsExactlyInAnyOrder(
                        first.getRequestSubjectId(),
                        second.getRequestSubjectId());
    }

    @Test
    void findByMatchRequestRequestId_shouldReturnEmptyListWhenNoSubjectsExist() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request = createMatchRequest(student, tutor);

        assertThat(
                matchRequestSubjectRepository
                        .findByMatchRequest_RequestId(
                                request.getRequestId()))
                .isEmpty();
    }

    // --------------------------------------------------
    // 2. findBySubject_Id
    // --------------------------------------------------

    @Test
    void findBySubjectId_shouldReturnRequestsContainingSubject() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request1 = createMatchRequest(student, tutor);
        MatchRequest request2 = createMatchRequest(student, tutor);

        Subject mathematics = createSubject("Mathematics");
        Subject science = createSubject("Science");

        MatchRequestSubject first = createRequestSubject(
                request1, mathematics);

        MatchRequestSubject second = createRequestSubject(
                request2, mathematics);

        createRequestSubject(request1, science);

        List<MatchRequestSubject> results = matchRequestSubjectRepository.findBySubject_Id(
                mathematics.getId());

        assertThat(results)
                .hasSize(2)
                .extracting(MatchRequestSubject::getRequestSubjectId)
                .containsExactlyInAnyOrder(
                        first.getRequestSubjectId(),
                        second.getRequestSubjectId());

        assertThat(results)
                .allSatisfy(result -> assertThat(result.getSubject().getId())
                        .isEqualTo(mathematics.getId()));
    }

    @Test
    void findBySubjectId_shouldReturnEmptyListWhenSubjectIsNotUsed() {
        Subject mathematics = createSubject("Mathematics");

        assertThat(
                matchRequestSubjectRepository.findBySubject_Id(
                        mathematics.getId()))
                .isEmpty();
    }

    // --------------------------------------------------
    // 3. findByMatchRequest_RequestIdOrderByRequestSubjectId
    // --------------------------------------------------

    @Test
    void findByMatchRequestRequestIdOrderByRequestSubjectId_shouldReturnAscendingOrder() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request = createMatchRequest(student, tutor);

        Subject mathematics = createSubject("Mathematics");
        Subject science = createSubject("Science");
        Subject english = createSubject("English");

        MatchRequestSubject first = createRequestSubject(
                request, mathematics);

        MatchRequestSubject second = createRequestSubject(
                request, science);

        MatchRequestSubject third = createRequestSubject(
                request, english);

        List<MatchRequestSubject> results = matchRequestSubjectRepository
                .findByMatchRequest_RequestIdOrderByRequestSubjectId(
                        request.getRequestId());

        assertThat(results)
                .extracting(MatchRequestSubject::getRequestSubjectId)
                .containsExactly(
                        first.getRequestSubjectId(),
                        second.getRequestSubjectId(),
                        third.getRequestSubjectId());
    }

    @Test
    void findByMatchRequestRequestIdOrderByRequestSubjectId_shouldReturnEmptyListWhenNoSubjectsExist() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request = createMatchRequest(student, tutor);

        assertThat(
                matchRequestSubjectRepository
                        .findByMatchRequest_RequestIdOrderByRequestSubjectId(
                                request.getRequestId()))
                .isEmpty();
    }

    // --------------------------------------------------
    // 4. deleteByMatchRequest_RequestId
    // --------------------------------------------------

    @Test
    void deleteByMatchRequestRequestId_shouldDeleteOnlyMatchingRequestSubjects() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request1 = createMatchRequest(student, tutor);
        MatchRequest request2 = createMatchRequest(student, tutor);

        Subject mathematics = createSubject("Mathematics");
        Subject science = createSubject("Science");

        createRequestSubject(request1, mathematics);
        createRequestSubject(request1, science);
        createRequestSubject(request2, mathematics);

        matchRequestSubjectRepository
                .deleteByMatchRequest_RequestId(
                        request1.getRequestId());

        entityManager.flush();
        entityManager.clear();

        assertThat(
                matchRequestSubjectRepository
                        .findByMatchRequest_RequestId(
                                request1.getRequestId()))
                .isEmpty();

        assertThat(
                matchRequestSubjectRepository
                        .findByMatchRequest_RequestId(
                                request2.getRequestId()))
                .hasSize(1);
    }

    @Test
    void deleteByMatchRequestRequestId_shouldNotDeleteOtherRequests() {
        Student student = createStudent();
        Tutor tutor = createTutor();

        MatchRequest request1 = createMatchRequest(student, tutor);
        MatchRequest request2 = createMatchRequest(student, tutor);

        Subject mathematics = createSubject("Mathematics");

        createRequestSubject(request1, mathematics);

        MatchRequestSubject remaining = createRequestSubject(
                request2, mathematics);

        matchRequestSubjectRepository
                .deleteByMatchRequest_RequestId(
                        request1.getRequestId());

        entityManager.flush();
        entityManager.clear();

        List<MatchRequestSubject> remainingSubjects = matchRequestSubjectRepository
                .findByMatchRequest_RequestId(
                        request2.getRequestId());

        assertThat(remainingSubjects)
                .hasSize(1)
                .extracting(MatchRequestSubject::getRequestSubjectId)
                .containsExactly(remaining.getRequestSubjectId());
    }
}
