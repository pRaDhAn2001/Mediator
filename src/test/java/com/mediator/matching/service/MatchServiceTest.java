package com.mediator.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mediator.auth.entity.User;
import com.mediator.common.base.Address;
import com.mediator.common.base.PreferredMode;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.matching.dto.request.TutorSearchRequest;
import com.mediator.matching.dto.response.TutorCardResponse;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorSearchResponse;
import com.mediator.matching.mapper.MatchMapper;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.student.entity.Student;
import com.mediator.student.repository.StudentRepository;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

        @Mock
        private TutorRepository tutorRepository;

        @Mock
        private StudentRepository studentRepository;

        @Mock
        private SubscriptionRepository subscriptionRepository;

        @Mock
        private TutorTeachingPreferenceRepository preferenceRepository;

        @Mock
        private com.mediator.auth.repository.UserRepository userRepository;

        @Spy
        private MatchMapper matchMapper = new MatchMapper(
                        new com.mediator.common.mapper.PreferredModeMapper());

        @InjectMocks
        private MatchService matchService;

        private Board cbse;

        private ClassLevel class8;

        private ClassLevel class10;

        private Subject mathematics;

        private Subject physics;

        private Subject chemistry;

        private User tutorUser;

        @BeforeEach
        void setUp() {

                cbse = Board.builder()
                                .id(1L)
                                .name("CBSE")
                                .build();

                class8 = ClassLevel.builder()
                                .id(8L)
                                .standard(8)
                                .build();

                class10 = ClassLevel.builder()
                                .id(10L)
                                .standard(10)
                                .build();

                mathematics = Subject.builder()
                                .id(1L)
                                .name("Mathematics")
                                .build();

                physics = Subject.builder()
                                .id(2L)
                                .name("Physics")
                                .build();

                chemistry = Subject.builder()
                                .id(3L)
                                .name("Chemistry")
                                .build();

                tutorUser = User.builder()
                                .id(1L)
                                .firstName("John")
                                .lastName("Tutor")
                                .email("tutor@test.com")
                                .mobileNumber("9876543210")
                                .build();
        }

        // ---------------------------------------------------------
        // Helper methods
        // ---------------------------------------------------------

        private Tutor createTutor(
                        Long tutorId,
                        VerificationStatus verificationStatus,
                        ProfileStatus profileStatus,
                        boolean online,
                        boolean studentHome,
                        boolean tutorHome,
                        Double latitude,
                        Double longitude) {

                Address address = Address.builder()
                                .city("Kolkata")
                                .latitude(latitude)
                                .longitude(longitude)
                                .build();

                PreferredMode mode = PreferredMode.builder()
                                .online(online)
                                .studentHome(studentHome)
                                .tutorHome(tutorHome)
                                .build();

                return Tutor.builder()
                                .tutorId(tutorId)
                                .user(
                                                User.builder()
                                                                .id(tutorId)
                                                                .firstName("Tutor")
                                                                .lastName(String.valueOf(tutorId))
                                                                .email("tutor" + tutorId + "@test.com")
                                                                .mobileNumber("9876543210")
                                                                .build())
                                .address(address)
                                .preferredMode(mode)
                                .verificationStatus(verificationStatus)
                                .profileStatus(profileStatus)
                                .teachingExperienceYears(5)
                                .build();
        }

        private TutorTeachingPreference createPreference(
                        Long preferenceId,
                        Tutor tutor,
                        Board board,
                        ClassLevel classLevel,
                        Subject subject) {

                TutorTeachingPreference preference = new TutorTeachingPreference();

                preference.setPreferenceId(preferenceId);
                preference.setTutor(tutor);
                preference.setBoard(board);
                preference.setClassLevel(classLevel);
                preference.setSubject(subject);

                return preference;
        }

        private TutorSearchRequest createSearchRequest(
                        List<Long> subjectIds) {

                return TutorSearchRequest.builder()
                                .subjectIds(subjectIds)
                                .boardId(1L)
                                .classLevelId(8L)
                                .preferredMode(
                                                PreferredModeDto.builder()
                                                                .studentHome(true)
                                                                .build())
                                .latitude(22.545)
                                .longitude(88.352)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();
        }

        // ---------------------------------------------------------
        // Search tests
        // ---------------------------------------------------------

        @Test
        void searchTutors_shouldReturnTutorMatchingAllSubjects() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class8,
                                                mathematics),

                                createPreference(
                                                2L,
                                                tutor,
                                                cbse,
                                                class8,
                                                physics),

                                createPreference(
                                                3L,
                                                tutor,
                                                cbse,
                                                class8,
                                                chemistry));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L,
                                                SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L, 2L, 3L)));

                assertThat(response).isNotNull();
                assertThat(response.getTutors()).hasSize(1);

                TutorCardResponse card = response.getTutors().get(0);

                assertThat(card.getTutorId())
                                .isEqualTo(1L);

                assertThat(card.getMatchedSubjects())
                                .isEqualTo(3);

                assertThat(card.getTotalRequestedSubjects())
                                .isEqualTo(3);

                assertThat(card.getMatchPercentage())
                                .isEqualTo(100.0);
        }

        @Test
        void searchTutors_shouldReturnTutorMatchingSomeSubjects() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class8,
                                                mathematics),

                                createPreference(
                                                2L,
                                                tutor,
                                                cbse,
                                                class8,
                                                physics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L,
                                                SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L, 2L, 3L)));

                assertThat(response.getTutors())
                                .hasSize(1);

                TutorCardResponse card = response.getTutors().get(0);

                assertThat(card.getMatchedSubjects())
                                .isEqualTo(2);

                assertThat(card.getTotalRequestedSubjects())
                                .isEqualTo(3);

                assertThat(card.getMatchPercentage())
                                .isEqualTo(66.67);
        }

        @Test
        void searchTutors_shouldExcludeTutorWithZeroSubjectMatches() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class8,
                                                chemistry));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L, 2L)));

                assertThat(response.getTutors())
                                .isEmpty();
        }

        @Test
        void searchTutors_shouldFilterByClassLevel() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class10,
                                                mathematics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L)));

                assertThat(response.getTutors())
                                .isEmpty();
        }

        @Test
        void searchTutors_shouldFilterByBoard() {

                Board icse = Board.builder()
                                .id(2L)
                                .name("ICSE")
                                .build();

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                icse,
                                                class8,
                                                mathematics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L)));

                assertThat(response.getTutors())
                                .isEmpty();
        }

        @Test
        void searchTutors_shouldFilterByPreferredMode() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true,
                                false,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class8,
                                                mathematics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .boardId(1L)
                                .classLevelId(8L)
                                .preferredMode(
                                                PreferredModeDto.builder()
                                                                .online(true)
                                                                .build())
                                .page(0)
                                .size(10)
                                .build();

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors())
                                .hasSize(1);
        }

        @Test
        void searchTutors_shouldExcludeTutorOutsideRadius() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.725,
                                88.352);

                List<TutorTeachingPreference> preferences = List.of(
                                createPreference(
                                                1L,
                                                tutor,
                                                cbse,
                                                class8,
                                                mathematics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferences);

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L)));

                assertThat(response.getTutors())
                                .isEmpty();
        }

        @Test
        void searchTutors_shouldPrioritizeMoreMatchedSubjects() {

                Tutor tutorA = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                Tutor tutorB = createTutor(
                                2L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                List<TutorTeachingPreference> preferencesA = List.of(
                                createPreference(
                                                1L,
                                                tutorA,
                                                cbse,
                                                class8,
                                                mathematics),

                                createPreference(
                                                2L,
                                                tutorA,
                                                cbse,
                                                class8,
                                                physics),

                                createPreference(
                                                3L,
                                                tutorA,
                                                cbse,
                                                class8,
                                                chemistry));

                List<TutorTeachingPreference> preferencesB = List.of(
                                createPreference(
                                                4L,
                                                tutorB,
                                                cbse,
                                                class8,
                                                mathematics));

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutorA, tutorB));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(preferencesA);

                when(preferenceRepository.findByTutor_TutorId(2L))
                                .thenReturn(preferencesB);

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(
                                                List.of(1L, 2L, 3L)));

                assertThat(response.getTutors())
                                .hasSize(2);

                assertThat(response.getTutors()
                                .get(0)
                                .getTutorId())
                                .isEqualTo(1L);

                assertThat(response.getTutors()
                                .get(1)
                                .getTutorId())
                                .isEqualTo(2L);
        }

        @Test
        void searchTutors_shouldSupportPagination() {

                Tutor tutor1 = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                Tutor tutor2 = createTutor(
                                2L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false,
                                true,
                                false,
                                22.545,
                                88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor1, tutor2));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(
                                                List.of(
                                                                createPreference(
                                                                                1L,
                                                                                tutor1,
                                                                                cbse,
                                                                                class8,
                                                                                mathematics)));

                when(preferenceRepository.findByTutor_TutorId(2L))
                                .thenReturn(
                                                List.of(
                                                                createPreference(
                                                                                2L,
                                                                                tutor2,
                                                                                cbse,
                                                                                class8,
                                                                                mathematics)));

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .boardId(1L)
                                .classLevelId(8L)
                                .page(0)
                                .size(1)
                                .build();

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors())
                                .hasSize(1);

                assertThat(response.getTotalElements())
                                .isEqualTo(2L);

                assertThat(response.getTotalPages())
                                .isEqualTo(2);

                assertThat(response.getHasNext())
                                .isTrue();
        }

        @Test
        void searchTutors_shouldThrowExceptionWhenSubjectsEmpty() {

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of())
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(
                                () -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "At least one subject is required");
        }

        // ---------------------------------------------------------
        // Tutor details
        // ---------------------------------------------------------

        @Test
        void getTutorDetails_shouldReturnApprovedLiveTutor() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true,
                                true,
                                false,
                                22.545,
                                88.352);

                Student student = Student.builder()
                                .studentId(10L)
                                .user(
                                                User.builder()
                                                                .id(2L)
                                                                .email("student@test.com")
                                                                .build())
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(
                                                Optional.of(student.getUser()));

                when(studentRepository.findByUser(student.getUser()))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(1L))
                                .thenReturn(Optional.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(
                                                List.of(
                                                                createPreference(
                                                                                1L,
                                                                                tutor,
                                                                                cbse,
                                                                                class8,
                                                                                mathematics)));

                TutorDetailsResponse response = matchService.getTutorDetails(
                                "student@test.com",
                                1L);

                assertThat(response)
                                .isNotNull();

                assertThat(response.getTutorId())
                                .isEqualTo(1L);
        }

        @Test
        void getTutorDetails_shouldThrowWhenTutorNotApproved() {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.PENDING,
                                ProfileStatus.LIVE,
                                true,
                                true,
                                false,
                                22.545,
                                88.352);

                Student student = Student.builder()
                                .studentId(10L)
                                .user(
                                                User.builder()
                                                                .id(2L)
                                                                .email("student@test.com")
                                                                .build())
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(
                                                Optional.of(student.getUser()));

                when(studentRepository.findByUser(student.getUser()))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(1L))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(
                                () -> matchService.getTutorDetails(
                                                "student@test.com",
                                                1L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Tutor not found");
        }
}
