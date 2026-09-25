package com.mediator.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.matching.dto.request.CreateMatchRequest;
import com.mediator.matching.dto.request.TutorActionRequest;
import com.mediator.matching.dto.response.MatchRequestResponse;
import com.mediator.matching.entity.MatchRequest;
import com.mediator.matching.entity.MatchRequestStatus;
import com.mediator.matching.entity.MatchRequestSubject;
import com.mediator.matching.entity.SubjectRequestStatus;
import com.mediator.matching.mapper.MatchRequestMapper;
import com.mediator.matching.repository.MatchRequestRepository;
import com.mediator.matching.repository.MatchRequestSubjectRepository;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MatchRequestServiceTest {

        @Mock
        private MatchRequestRepository matchRequestRepository;

        @Mock
        private MatchRequestSubjectRepository matchRequestSubjectRepository;

        @Mock
        private StudentRepository studentRepository;

        @Mock
        private TutorRepository tutorRepository;

        @Mock
        private SubjectRepository subjectRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private MatchRequestMapper matchRequestMapper;

        @Mock
        private TutorTeachingPreferenceRepository tutorTeachingPreferenceRepository;

        @InjectMocks
        private MatchRequestService matchRequestService;

        private User studentUser;
        private User tutorUser;

        private Student student;
        private Tutor tutor;

        private Board board;
        private ClassLevel classLevel;

        private Subject math;
        private Subject physics;

        @BeforeEach
        void setUp() {

                studentUser = User.builder()
                                .id(1L)
                                .firstName("Student")
                                .lastName("User")
                                .email("student@test.com")
                                .mobileNumber("9999999999")
                                .build();

                tutorUser = User.builder()
                                .id(2L)
                                .firstName("Tutor")
                                .lastName("User")
                                .email("tutor@test.com")
                                .mobileNumber("8888888888")
                                .build();

                board = Board.builder()
                                .id(1L)
                                .name("CBSE")
                                .build();

                classLevel = ClassLevel.builder()
                                .id(8L)
                                .standard(8)
                                .build();

                student = Student.builder()
                                .studentId(10L)
                                .user(studentUser)
                                .board(board)
                                .classLevel(classLevel)
                                .build();

                tutor = Tutor.builder()
                                .tutorId(20L)
                                .user(tutorUser)
                                .verificationStatus(VerificationStatus.APPROVED)
                                .build();

                math = Subject.builder()
                                .id(1L)
                                .name("Mathematics")
                                .build();

                physics = Subject.builder()
                                .id(2L)
                                .name("Physics")
                                .build();
        }

        // ============================================================
        // Helper methods
        // ============================================================

        private TutorTeachingPreference createPreference(
                        Long id,
                        Tutor tutor,
                        Board board,
                        ClassLevel classLevel,
                        Subject subject) {

                TutorTeachingPreference preference = new TutorTeachingPreference();

                preference.setPreferenceId(id);
                preference.setTutor(tutor);
                preference.setBoard(board);
                preference.setClassLevel(classLevel);
                preference.setSubject(subject);

                return preference;
        }

        private void mockStudent(String email) {

                when(userRepository.findByEmail(email))
                                .thenReturn(Optional.of(studentUser));

                when(studentRepository.findByUser(studentUser))
                                .thenReturn(Optional.of(student));
        }

        private void mockTutor(String email) {

                when(userRepository.findByEmail(email))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));
        }

        private void mockStudentResponse() {

                when(matchRequestMapper.toStudentResponse(
                                any(MatchRequest.class)))
                                .thenAnswer(invocation -> {

                                        MatchRequest request = invocation.getArgument(0);

                                        return MatchRequestResponse.builder()
                                                        .requestId(request.getRequestId())
                                                        .studentId(
                                                                        request.getStudent()
                                                                                        .getStudentId())
                                                        .tutorId(
                                                                        request.getTutor()
                                                                                        .getTutorId())
                                                        .status(request.getStatus())
                                                        .build();
                                });
        }

        private void mockTutorResponse() {

                when(matchRequestMapper.toTutorResponse(
                                any(MatchRequest.class)))
                                .thenAnswer(invocation -> {

                                        MatchRequest request = invocation.getArgument(0);

                                        return MatchRequestResponse.builder()
                                                        .requestId(request.getRequestId())
                                                        .studentId(
                                                                        request.getStudent()
                                                                                        .getStudentId())
                                                        .tutorId(
                                                                        request.getTutor()
                                                                                        .getTutorId())
                                                        .status(request.getStatus())
                                                        .build();
                                });
        }

        private MatchRequest createRequestEntity(
                        MatchRequestStatus status) {

                return MatchRequest.builder()
                                .requestId(100L)
                                .student(student)
                                .tutor(tutor)
                                .status(status)
                                .message("Need tuition")
                                .requestedSubjects(
                                                new ArrayList<>())
                                .build();
        }

        private MatchRequestSubject createRequestSubject(
                        MatchRequest request,
                        Subject subject,
                        SubjectRequestStatus status) {

                return MatchRequestSubject.builder()
                                .matchRequest(request)
                                .subject(subject)
                                .status(status)
                                .build();
        }

        private void mockActiveRequestCheck(boolean exists) {

                when(matchRequestRepository
                                .existsByStudent_StudentIdAndTutor_TutorIdAndStatusIn(
                                                any(),
                                                any(),
                                                anyList()))
                                .thenReturn(exists);
        }

        // ============================================================
        // CREATE REQUEST
        // ============================================================

        @Test
        void createRequest_success() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .message("Need mathematics tutor")
                                .build();

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                board,
                                                classLevel,
                                                math));

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                when(tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(preferences);

                when(subjectRepository.findById(1L))
                                .thenReturn(Optional.of(math));

                mockStudentResponse();

                MatchRequestResponse response = matchRequestService.createRequest(
                                "student@test.com",
                                request);

                assertThat(response).isNotNull();

                assertThat(response.getStudentId())
                                .isEqualTo(10L);

                assertThat(response.getTutorId())
                                .isEqualTo(20L);

                assertThat(response.getStatus())
                                .isEqualTo(MatchRequestStatus.REQUESTED);

                verify(matchRequestRepository)
                                .save(any(MatchRequest.class));

                verify(matchRequestSubjectRepository)
                                .saveAll(anyList());

                verify(subjectRepository)
                                .findById(1L);
        }

        @Test
        void createRequest_tutorNotFound() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(999L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Tutor not found");
        }

        @Test
        void createRequest_unapprovedTutor() {

                mockStudent("student@test.com");

                Tutor unapprovedTutor = Tutor.builder()
                                .tutorId(20L)
                                .user(tutorUser)
                                .verificationStatus(
                                                VerificationStatus.PENDING)
                                .build();

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(unapprovedTutor));

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "unverified tutor");
        }

        @Test
        void createRequest_subjectListEmpty() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of())
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "At least one subject is required");
        }

        @Test
        void createRequest_duplicateSubjectIds() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L, 1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Duplicate subject IDs");
        }

        @Test
        void createRequest_activeRequestAlreadyExists() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(true);

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "already have an active request");
        }

        @Test
        void createRequest_studentClassLevelMissing() {

                mockStudent("student@test.com");

                student.setClassLevel(null);

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "class level is not configured");
        }

        @Test
        void createRequest_studentBoardMissing() {

                mockStudent("student@test.com");

                student.setBoard(null);

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "board is not configured");
        }

        @Test
        void createRequest_tutorHasNoTeachingPreferences() {

                mockStudent("student@test.com");

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                when(tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(List.of());

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "no teaching preferences configured");
        }

        @Test
        void createRequest_tutorDoesNotTeachRequestedSubject() {

                mockStudent("student@test.com");

                ClassLevel class10 = ClassLevel.builder()
                                .id(10L)
                                .standard(10)
                                .build();

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                board,
                                                class10,
                                                math));

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                when(tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(preferences);

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Tutor does not teach the following requested subject IDs");
        }

        @Test
        void createRequest_tutorDoesNotTeachRequestedSubjectForBoard() {

                mockStudent("student@test.com");

                Board icse = Board.builder()
                                .id(2L)
                                .name("ICSE")
                                .build();

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                icse,
                                                classLevel,
                                                math));

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                when(tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(preferences);

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Tutor does not teach");
        }

        @Test
        void createRequest_subjectNotFound() {

                mockStudent("student@test.com");

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                board,
                                                classLevel,
                                                math));

                CreateMatchRequest request = CreateMatchRequest.builder()
                                .tutorId(20L)
                                .subjectIds(List.of(1L))
                                .build();

                when(tutorRepository.findById(20L))
                                .thenReturn(Optional.of(tutor));

                mockActiveRequestCheck(false);

                when(tutorTeachingPreferenceRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(preferences);

                when(subjectRepository.findById(1L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchRequestService.createRequest(
                                "student@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining(
                                                "Subject not found");
        }

        // ============================================================
        // STUDENT REQUESTS
        // ============================================================

        @Test
        void getStudentRequests_success() {

                mockStudent("student@test.com");

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository
                                .findByStudent_StudentId(10L))
                                .thenReturn(List.of(request));

                mockStudentResponse();

                List<MatchRequestResponse> response = matchRequestService.getStudentRequests(
                                "student@test.com");

                assertThat(response)
                                .hasSize(1);

                assertThat(response.get(0).getStatus())
                                .isEqualTo(MatchRequestStatus.REQUESTED);
        }

        @Test
        void getStudentRequests_studentNotFound() {

                when(userRepository.findByEmail(
                                "student@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchRequestService.getStudentRequests(
                                "student@test.com"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining(
                                                "User not found");
        }

        // ============================================================
        // TUTOR REQUESTS
        // ============================================================

        @Test
        void getTutorRequests_success() {

                mockTutor("tutor@test.com");

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository
                                .findByTutor_TutorId(20L))
                                .thenReturn(List.of(request));

                mockTutorResponse();

                List<MatchRequestResponse> response = matchRequestService.getTutorRequests(
                                "tutor@test.com");

                assertThat(response)
                                .hasSize(1);

                assertThat(response.get(0).getStatus())
                                .isEqualTo(MatchRequestStatus.REQUESTED);
        }

        // ============================================================
        // REQUEST DETAILS
        // ============================================================

        @Test
        void getRequestDetails_asStudent_success() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockStudentResponse();

                MatchRequestResponse response = matchRequestService.getRequestDetails(
                                "student@test.com",
                                100L);

                assertThat(response)
                                .isNotNull();

                assertThat(response.getRequestId())
                                .isEqualTo(100L);

                assertThat(response.getStatus())
                                .isEqualTo(MatchRequestStatus.REQUESTED);
        }

        @Test
        void getRequestDetails_asTutor_success() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.getRequestDetails(
                                "tutor@test.com",
                                100L);

                assertThat(response)
                                .isNotNull();

                assertThat(response.getTutorId())
                                .isEqualTo(20L);

                assertThat(response.getStatus())
                                .isEqualTo(MatchRequestStatus.REQUESTED);
        }

        @Test
        void getRequestDetails_unauthorizedUser() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.getRequestDetails(
                                "other@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "not allowed to view");
        }

        @Test
        void getRequestDetails_requestNotFound() {

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchRequestService.getRequestDetails(
                                "student@test.com",
                                100L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining(
                                                "Match request not found");
        }

        // ============================================================
        // CANCEL REQUEST
        // ============================================================

        @Test
        void cancelRequest_success() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                MatchRequestSubject subject = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(subject)));

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockStudentResponse();

                MatchRequestResponse response = matchRequestService.cancelRequest(
                                "student@test.com",
                                100L);

                assertThat(response.getStatus())
                                .isEqualTo(MatchRequestStatus.CANCELLED);

                assertThat(request.getStatus())
                                .isEqualTo(MatchRequestStatus.CANCELLED);

                assertThat(subject.getStatus())
                                .isEqualTo(SubjectRequestStatus.REJECTED);

                verify(matchRequestRepository)
                                .save(request);
        }

        @Test
        void cancelRequest_wrongStudent() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.cancelRequest(
                                "other@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "not allowed to modify");
        }

        @Test
        void cancelRequest_statusNotRequested() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.cancelRequest(
                                "student@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Only a requested match can be cancelled");
        }

        // ============================================================
        // CONNECT STUDENT
        // ============================================================

        @Test
        void connectStudent_success() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                MatchRequestSubject subject = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(subject)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.connectStudent(
                                "tutor@test.com",
                                100L);

                assertThat(response.getStatus())
                                .isEqualTo(MatchRequestStatus.CONNECTED);

                assertThat(request.getStatus())
                                .isEqualTo(MatchRequestStatus.CONNECTED);

                assertThat(request.getConnectedAt())
                                .isNotNull();

                // Subjects remain REQUESTED after connection.
                assertThat(subject.getStatus())
                                .isEqualTo(SubjectRequestStatus.REQUESTED);

                verify(matchRequestRepository)
                                .save(request);
        }

        @Test
        void connectStudent_wrongTutor() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.connectStudent(
                                "other@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "not allowed to modify");
        }

        @Test
        void connectStudent_statusNotRequested() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.connectStudent(
                                "tutor@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Only a REQUESTED match can be connected");
        }

        // ============================================================
        // PARTIAL FINALIZATION
        // ============================================================

        @Test
        void partiallyFinalize_someAccepted_movesToPartiallyFinalized() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                MatchRequestSubject physicsRequest = createRequestSubject(
                                request,
                                physics,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(
                                                                mathRequest,
                                                                physicsRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of(1L))
                                .build();

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action);

                assertThat(response.getStatus())
                                .isEqualTo(
                                                MatchRequestStatus.PARTIALLY_FINALIZED);

                assertThat(mathRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.ACCEPTED);

                assertThat(physicsRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.REJECTED);

                verify(matchRequestRepository)
                                .save(request);
        }

        @Test
        void partiallyFinalize_allAccepted_movesDirectlyToFinalized() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                MatchRequestSubject physicsRequest = createRequestSubject(
                                request,
                                physics,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(
                                                                mathRequest,
                                                                physicsRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of(1L, 2L))
                                .build();

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action);

                assertThat(response.getStatus())
                                .isEqualTo(
                                                MatchRequestStatus.FINALIZED);

                assertThat(request.getFinalizedAt())
                                .isNotNull();

                assertThat(mathRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.FINALIZED);

                assertThat(physicsRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.FINALIZED);
        }

        @Test
        void partiallyFinalize_noSubjectsAccepted() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(mathRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of())
                                .build();

                assertThatThrownBy(() -> matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "At least one subject must be accepted");
        }

        @Test
        void partiallyFinalize_subjectNotInOriginalRequest() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(mathRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of(2L))
                                .build();

                assertThatThrownBy(() -> matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "only accept subjects included");
        }

        @Test
        void partiallyFinalize_duplicateAcceptedSubjectIds() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(mathRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of(1L, 1L))
                                .build();

                assertThatThrownBy(() -> matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Duplicate accepted subject IDs");
        }

        @Test
        void partiallyFinalize_wrongStatus() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                TutorActionRequest action = TutorActionRequest.builder()
                                .acceptedSubjectIds(
                                                List.of(1L))
                                .build();

                assertThatThrownBy(() -> matchRequestService.partiallyFinalize(
                                "tutor@test.com",
                                100L,
                                action))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "only for CONNECTED requests");
        }

        // ============================================================
        // FINALIZE
        // ============================================================

        @Test
        void finalizeRequest_connected_finalizesAllSubjects() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REQUESTED);

                MatchRequestSubject physicsRequest = createRequestSubject(
                                request,
                                physics,
                                SubjectRequestStatus.REQUESTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(
                                                                mathRequest,
                                                                physicsRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.finalizeRequest(
                                "tutor@test.com",
                                100L);

                assertThat(response.getStatus())
                                .isEqualTo(
                                                MatchRequestStatus.FINALIZED);

                assertThat(request.getStatus())
                                .isEqualTo(
                                                MatchRequestStatus.FINALIZED);

                assertThat(request.getFinalizedAt())
                                .isNotNull();

                assertThat(mathRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.FINALIZED);

                assertThat(physicsRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.FINALIZED);

                verify(matchRequestRepository)
                                .save(request);
        }

        @Test
        void finalizeRequest_partiallyFinalized_finalizesAcceptedSubjectsOnly() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.PARTIALLY_FINALIZED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.ACCEPTED);

                MatchRequestSubject physicsRequest = createRequestSubject(
                                request,
                                physics,
                                SubjectRequestStatus.REJECTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(
                                                                mathRequest,
                                                                physicsRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                mockTutorResponse();

                MatchRequestResponse response = matchRequestService.finalizeRequest(
                                "tutor@test.com",
                                100L);

                assertThat(response.getStatus())
                                .isEqualTo(
                                                MatchRequestStatus.FINALIZED);

                assertThat(mathRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.FINALIZED);

                assertThat(physicsRequest.getStatus())
                                .isEqualTo(
                                                SubjectRequestStatus.REJECTED);

                assertThat(request.getFinalizedAt())
                                .isNotNull();
        }

        @Test
        void finalizeRequest_partiallyFinalized_withoutAcceptedSubjects() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.PARTIALLY_FINALIZED);

                MatchRequestSubject mathRequest = createRequestSubject(
                                request,
                                math,
                                SubjectRequestStatus.REJECTED);

                request.setRequestedSubjects(
                                new ArrayList<>(
                                                List.of(mathRequest)));

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.finalizeRequest(
                                "tutor@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "No accepted subjects");
        }

        @Test
        void finalizeRequest_wrongStatus() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.REQUESTED);

                mockTutor("tutor@test.com");

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.finalizeRequest(
                                "tutor@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Only CONNECTED or PARTIALLY_FINALIZED");
        }

        @Test
        void finalizeRequest_wrongTutor() {

                MatchRequest request = createRequestEntity(
                                MatchRequestStatus.CONNECTED);

                when(matchRequestRepository.findById(100L))
                                .thenReturn(Optional.of(request));

                assertThatThrownBy(() -> matchRequestService.finalizeRequest(
                                "other@test.com",
                                100L))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "not allowed to modify");
        }
}