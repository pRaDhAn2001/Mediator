package com.mediator.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.mediator.matching.entity.MatchScore;
import com.mediator.matching.mapper.MatchMapper;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
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

        private TutorSearchRequest createOnlineSearchRequest() {
                return TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .boardId(1L)
                                .classLevelId(8L)
                                .preferredMode(PreferredModeDto.builder()
                                                .online(true)
                                                .build())
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

        // =========================================================
        // Additional search validation tests
        // =========================================================

        @Test
        void searchTutors_shouldRejectNullSubjectIds() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(null)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("At least one subject is required");
        }

        @Test
        void searchTutors_shouldRejectNullPage() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .page(null)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Page must be zero or greater");
        }

        @Test
        void searchTutors_shouldRejectNegativePage() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .page(-1)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Page must be zero or greater");
        }

        @Test
        void searchTutors_shouldRejectNullPageSize() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .page(0)
                                .size(null)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Size must be greater than zero");
        }

        @Test
        void searchTutors_shouldRejectZeroPageSize() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .page(0)
                                .size(0)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Size must be greater than zero");
        }

        @Test
        void searchTutors_shouldRejectPageSizeAboveMaximum() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .page(0)
                                .size(51)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Maximum page size is 50");
        }

        @Test
        void searchTutors_shouldRequireCoordinatesForStudentHomeSearch() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(null)
                                .longitude(88.352)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Latitude and longitude are required");
        }

        @Test
        void searchTutors_shouldRejectInvalidLatitude() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(91.0)
                                .longitude(88.352)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Invalid latitude");
        }

        @Test
        void searchTutors_shouldRejectInvalidLongitude() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(22.545)
                                .longitude(181.0)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Invalid longitude");
        }

        @Test
        void searchTutors_shouldRejectZeroRadiusForStudentHomeSearch() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(22.545)
                                .longitude(88.352)
                                .radius(0.0)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Radius must be greater than zero");
        }

        @Test
        void searchTutors_shouldRejectNullRadiusForStudentHomeSearch() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(22.545)
                                .longitude(88.352)
                                .radius(null)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Radius must be greater than zero");
        }

        // =========================================================
        // Additional tutor search filtering tests
        // =========================================================

        @Test
        void searchTutors_shouldExcludeUnapprovedTutor() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.PENDING,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
                org.mockito.Mockito.verifyNoInteractions(preferenceRepository);
        }

        @Test
        void searchTutors_shouldExcludeTutorWhoseProfileIsNotLive() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.DRAFT,
                                false, true, false,
                                22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
                org.mockito.Mockito.verifyNoInteractions(preferenceRepository);
        }

        @Test
        void searchTutors_shouldReturnEmptyWhenRepositoryHasNoTutors() {
                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of());

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
                assertThat(response.getTotalElements()).isZero();
        }

        @Test
        void searchTutors_shouldExcludeTutorWithNoTeachingPreferences() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of());

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldExcludeTutorWithoutAddressForStudentHomeSearch() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                tutor.setAddress(null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldAllowSearchWithoutPreferredMode() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .boardId(1L)
                                .classLevelId(8L)
                                .preferredMode(null)
                                .latitude(null)
                                .longitude(null)
                                .radius(null)
                                .page(0)
                                .size(10)
                                .build();

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors()).hasSize(1);
        }

        @Test
        void searchTutors_shouldExcludeTutorWithNullPreferredModeWhenModeIsRequested() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                tutor.setPreferredMode(null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
                org.mockito.Mockito.verifyNoInteractions(preferenceRepository);
        }

        @Test
        void getTutorDetails_shouldThrowWhenStudentUserDoesNotExist() {
                when(userRepository.findByEmail("missing@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchService.getTutorDetails("missing@test.com", 1L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("User not found");
        }

        @Test
        void getTutorDetails_shouldThrowWhenStudentProfileDoesNotExist() {
                User user = User.builder()
                                .id(10L)
                                .email("student@test.com")
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(Optional.of(user));

                when(studentRepository.findByUser(user))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchService.getTutorDetails("student@test.com", 1L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Student profile not found");
        }

        @Test
        void getTutorDetails_shouldThrowWhenTutorDoesNotExist() {
                User user = User.builder()
                                .id(10L)
                                .email("student@test.com")
                                .build();

                Student student = Student.builder()
                                .studentId(10L)
                                .user(user)
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(Optional.of(user));

                when(studentRepository.findByUser(user))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> matchService.getTutorDetails("student@test.com", 999L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Tutor not found");
        }

        @Test
        void searchTutors_shouldRequireLongitudeForStudentHomeSearch() {
                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(22.545)
                                .longitude(null)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();

                assertThatThrownBy(() -> matchService.searchTutors(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Latitude and longitude are required");
        }

        @Test
        void searchTutors_shouldSkipPreferenceWithNullSubject() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                TutorTeachingPreference preference = createPreference(1L, tutor, cbse, class8, null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(preference));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldSkipPreferenceWithNullBoard() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                TutorTeachingPreference preference = createPreference(1L, tutor, null, class8, mathematics);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(preference));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldSkipPreferenceWithNullClassLevel() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                TutorTeachingPreference preference = createPreference(1L, tutor, cbse, null, mathematics);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(preference));

                TutorSearchResponse response = matchService.searchTutors(
                                createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void getTutorDetails_shouldThrowWhenTutorProfileIsNotLive() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.DRAFT,
                                true, true, false,
                                22.545, 88.352);

                Student student = Student.builder()
                                .studentId(10L)
                                .user(User.builder()
                                                .id(2L)
                                                .email("student@test.com")
                                                .build())
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(Optional.of(student.getUser()));

                when(studentRepository.findByUser(student.getUser()))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(1L))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> matchService.getTutorDetails("student@test.com", 1L))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Tutor not found");
        }

        @Test
        void searchTutors_shouldSkipPreferenceWithNullBoardWhenBoardFilterRequested() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, 22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, null, class8, mathematics)));

                TutorSearchResponse response = matchService.searchTutors(createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldSkipPreferenceWithNullClassLevelWhenClassFilterRequested() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, 22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, null, mathematics)));

                TutorSearchResponse response = matchService.searchTutors(createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldExcludeTutorWithNullPreferredMode() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, 22.545, 88.352);

                tutor.setPreferredMode(null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .online(true)
                                                .build())
                                .page(0)
                                .size(10)
                                .build();

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldExcludeTutorWhenRequestedModeDoesNotMatch() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .studentHome(true)
                                                .build())
                                .latitude(22.545)
                                .longitude(88.352)
                                .radius(10.0)
                                .page(0)
                                .size(10)
                                .build();

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors()).isEmpty();

                verify(preferenceRepository, never())
                                .findByTutor_TutorId(1L);
        }

        @Test
        void searchTutors_shouldExcludeTutorWithNullLatitudeForStudentHomeSearch() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, null, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                TutorSearchResponse response = matchService.searchTutors(createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void searchTutors_shouldExcludeTutorWithNullLongitudeForStudentHomeSearch() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, 22.545, null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                TutorSearchResponse response = matchService.searchTutors(createSearchRequest(List.of(1L)));

                assertThat(response.getTutors()).isEmpty();
        }

        @Test
        void getTutorDetails_shouldHandleStudentWithoutAddress() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, true, false,
                                22.545, 88.352);

                Student student = Student.builder()
                                .studentId(10L)
                                .user(User.builder()
                                                .id(2L)
                                                .email("student@test.com")
                                                .build())
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(Optional.of(student.getUser()));

                when(studentRepository.findByUser(student.getUser()))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(1L))
                                .thenReturn(Optional.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                TutorDetailsResponse response = matchService.getTutorDetails("student@test.com", 1L);

                assertThat(response).isNotNull();
                assertThat(response.getTutorId()).isEqualTo(1L);
        }

        @ParameterizedTest
        @MethodSource("incompleteStudentAddresses")
        void getTutorDetails_shouldHandleStudentAddressWithMissingCoordinates(
                        Address studentAddress) {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                User studentUser = User.builder()
                                .id(2L)
                                .email("student@test.com")
                                .build();

                Student student = Student.builder()
                                .studentId(10L)
                                .user(studentUser)
                                .address(studentAddress)
                                .build();

                when(userRepository.findByEmail("student@test.com"))
                                .thenReturn(Optional.of(studentUser));

                when(studentRepository.findByUser(studentUser))
                                .thenReturn(Optional.of(student));

                when(tutorRepository.findById(1L))
                                .thenReturn(Optional.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(
                                                createPreference(1L, tutor, cbse, class8, mathematics)));

                TutorDetailsResponse response = matchService.getTutorDetails("student@test.com", 1L);

                assertThat(response).isNotNull();
        }

        private static Stream<Address> incompleteStudentAddresses() {
                return Stream.of(
                                Address.builder()
                                                .city("Kolkata")
                                                .latitude(null)
                                                .longitude(88.352)
                                                .build(),
                                Address.builder()
                                                .city("Kolkata")
                                                .latitude(22.545)
                                                .longitude(null)
                                                .build());
        }

        @Test
        void searchTutors_shouldHandleTutorWithoutActiveSubscription() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchRequest request = createOnlineSearchRequest();

                TutorSearchResponse response = matchService.searchTutors(request);

                assertThat(response.getTutors()).hasSize(1);

                verify(subscriptionRepository, atLeastOnce())
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE);
        }

        @Test
        void searchTutors_shouldHandleActiveSubscriptionWithNullPlan() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                Subscription subscription = Subscription.builder()
                                .tutor(tutor)
                                .subscriptionPlan(null)
                                .status(SubscriptionStatus.ACTIVE)
                                .build();

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.of(subscription));

                TutorSearchResponse response = matchService.searchTutors(createOnlineSearchRequest());

                assertThat(response.getTutors()).hasSize(1);
        }

        @ParameterizedTest
        @EnumSource(SubscriptionPlanType.class)
        void searchTutors_shouldHandleEachSubscriptionPlanType(
                        SubscriptionPlanType planType) {

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                SubscriptionPlan plan = SubscriptionPlan.builder()
                                .subscriptionPlanId(1L)
                                .planType(planType)
                                .price(BigDecimal.valueOf(299))
                                .durationInMonths(1)
                                .active(true)
                                .build();

                Subscription subscription = Subscription.builder()
                                .tutor(tutor)
                                .subscriptionPlan(plan)
                                .status(SubscriptionStatus.ACTIVE)
                                .build();

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.of(subscription));

                TutorSearchResponse response = matchService.searchTutors(createOnlineSearchRequest());

                assertThat(response.getTutors()).hasSize(1);
        }

        @Test
        void searchTutors_shouldHandleNullTeachingExperience() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                tutor.setTeachingExperienceYears(null);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchResponse response = matchService.searchTutors(createOnlineSearchRequest());

                assertThat(response.getTutors()).hasSize(1);
        }

        @Test
        void searchTutors_shouldHandleTeachingExperienceScoreCap() {
                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                true, false, false,
                                22.545, 88.352);

                tutor.setTeachingExperienceYears(20);

                when(tutorRepository.findSearchableTutors())
                                .thenReturn(List.of(tutor));

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(createPreference(1L, tutor, cbse, class8, mathematics)));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchResponse response = matchService.searchTutors(createOnlineSearchRequest());

                assertThat(response.getTutors()).hasSize(1);
        }

        // =========================================================
        // Targeted branch-coverage tests for remaining JaCoCo lines
        // =========================================================

        @Test
        void matchesPreferredMode_shouldCoverTutorHomeTrueAndFalseBranches() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, false, false, 22.545, 88.352);

                TutorSearchRequest tutorHomeRequest = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder()
                                                .tutorHome(true)
                                                .build())
                                .page(0)
                                .size(10)
                                .build();

                Boolean noMatch = ReflectionTestUtils.invokeMethod(
                                matchService, "matchesPreferredMode", tutor, tutorHomeRequest);
                assertThat(noMatch).isFalse();

                tutor.setPreferredMode(PreferredMode.builder()
                                .online(false)
                                .studentHome(false)
                                .tutorHome(true)
                                .build());

                Boolean match = ReflectionTestUtils.invokeMethod(
                                matchService, "matchesPreferredMode", tutor, tutorHomeRequest);
                assertThat(match).isTrue();
        }

        @Test
        void calculateMatchScore_shouldCoverNullSubjectBoardAndClassLevelBranches() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                true, false, false, 22.545, 88.352);

                TutorTeachingPreference nullSubject = createPreference(
                                1L, tutor, cbse, class8, null);
                TutorTeachingPreference nullBoard = createPreference(
                                2L, tutor, null, class8, mathematics);
                TutorTeachingPreference nullClass = createPreference(
                                3L, tutor, cbse, null, mathematics);
                TutorTeachingPreference valid = createPreference(
                                4L, tutor, cbse, class8, mathematics);

                when(preferenceRepository.findByTutor_TutorId(1L))
                                .thenReturn(List.of(nullSubject, nullBoard, nullClass, valid));
                when(subscriptionRepository.findByTutor_TutorIdAndStatus(
                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .boardId(1L)
                                .classLevelId(8L)
                                .preferredMode(PreferredModeDto.builder().online(true).build())
                                .page(0)
                                .size(10)
                                .build();

                MatchScore score = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateMatchScore", tutor, request);

                assertThat(score).isNotNull();
                assertThat(score.getMatchedSubjects()).isEqualTo(1);
                assertThat(score.getTotalRequestedSubjects()).isEqualTo(1);
                assertThat(score.getMatchPercentage()).isEqualTo(100.0);
        }

        @Test
        void calculateMatchScore_shouldReturnZeroPercentageWhenNoSubjectsRequested() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                true, false, false, 22.545, 88.352);

                when(preferenceRepository.findByTutor_TutorId(1L)).thenReturn(List.of());
                when(subscriptionRepository.findByTutor_TutorIdAndStatus(
                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorSearchRequest request = TutorSearchRequest.builder()
                                .subjectIds(List.of())
                                .page(0)
                                .size(10)
                                .build();

                MatchScore score = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateMatchScore", tutor, request);

                assertThat(score).isNotNull();
                assertThat(score.getMatchedSubjects()).isZero();
                assertThat(score.getTotalRequestedSubjects()).isZero();
                assertThat(score.getMatchPercentage()).isEqualTo(0.0);
        }

        @Test
        void calculateSearchDistance_shouldCoverMissingTutorAddressAndCoordinates() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                false, true, false, 22.545, 88.352);
                TutorSearchRequest request = createSearchRequest(List.of(1L));

                tutor.setAddress(null);
                Double noAddress = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateSearchDistance", tutor, request);
                assertThat(noAddress).isEqualTo(Double.MAX_VALUE);

                tutor.setAddress(Address.builder().latitude(null).longitude(88.352).build());
                Double noLatitude = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateSearchDistance", tutor, request);
                assertThat(noLatitude).isEqualTo(Double.MAX_VALUE);

                tutor.setAddress(Address.builder().latitude(22.545).longitude(null).build());
                Double noLongitude = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateSearchDistance", tutor, request);
                assertThat(noLongitude).isEqualTo(Double.MAX_VALUE);

                TutorSearchRequest onlineRequest = TutorSearchRequest.builder()
                                .subjectIds(List.of(1L))
                                .preferredMode(PreferredModeDto.builder().online(true).build())
                                .page(0)
                                .size(10)
                                .build();
                Double onlineDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateSearchDistance", tutor, onlineRequest);
                assertThat(onlineDistance).isEqualTo(0.0);
        }

        @Test
        void calculateSecondaryScore_shouldCoverUnapprovedTutorAndAllSubscriptionPlans() {
                Tutor tutor = createTutor(
                                1L, VerificationStatus.PENDING, ProfileStatus.LIVE,
                                true, false, false, 22.545, 88.352);
                tutor.setTeachingExperienceYears(null);

                when(subscriptionRepository.findByTutor_TutorIdAndStatus(
                                1L, SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());
                Integer noPlanScore = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateSecondaryScore", tutor);
                assertThat(noPlanScore).isZero();

                for (SubscriptionPlanType planType : SubscriptionPlanType.values()) {
                        SubscriptionPlan plan = SubscriptionPlan.builder()
                                        .subscriptionPlanId(1L)
                                        .planType(planType)
                                        .price(BigDecimal.valueOf(299))
                                        .durationInMonths(1)
                                        .active(true)
                                        .build();
                        Subscription subscription = Subscription.builder()
                                        .tutor(tutor)
                                        .subscriptionPlan(plan)
                                        .status(SubscriptionStatus.ACTIVE)
                                        .build();
                        when(subscriptionRepository.findByTutor_TutorIdAndStatus(
                                        1L, SubscriptionStatus.ACTIVE))
                                        .thenReturn(Optional.of(subscription));

                        Integer score = ReflectionTestUtils.invokeMethod(
                                        matchService, "calculateSecondaryScore", tutor);
                        int expected = switch (planType) {
                                case BASIC -> 10;
                                case PRO -> 20;
                                case ULTRA -> 30;
                        };
                        assertThat(score).isEqualTo(expected);
                }
        }

        @Test
        void calculateTutorDistance_shouldCoverOnlineOnlyAndMissingAddresses() {
                Student student = Student.builder()
                                .studentId(10L)
                                .build();

                Tutor tutor = createTutor(
                                1L, VerificationStatus.APPROVED, ProfileStatus.LIVE,
                                true, false, false, 22.545, 88.352);

                Double onlineDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateTutorDistance", student, tutor);

                assertThat(onlineDistance).isEqualTo(0.0);

                tutor.setPreferredMode(PreferredMode.builder()
                                .online(false)
                                .studentHome(true)
                                .tutorHome(false)
                                .build());

                Double missingStudentAddressDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateTutorDistance", student, tutor);

                assertThat(missingStudentAddressDistance).isNull();

                student.setAddress(Address.builder()
                                .latitude(22.545)
                                .longitude(88.352)
                                .build());

                tutor.setAddress(null);

                Double missingTutorAddressDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateTutorDistance", student, tutor);

                assertThat(missingTutorAddressDistance).isNull();

                tutor.setAddress(Address.builder()
                                .latitude(null)
                                .longitude(88.352)
                                .build());

                Double missingLatitudeDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateTutorDistance", student, tutor);

                assertThat(missingLatitudeDistance).isNull();

                tutor.setAddress(Address.builder()
                                .latitude(22.545)
                                .longitude(null)
                                .build());

                Double missingLongitudeDistance = ReflectionTestUtils.invokeMethod(
                                matchService, "calculateTutorDistance", student, tutor);

                assertThat(missingLongitudeDistance).isNull();
        }

        @Test
        void calculateTutorDistance_shouldCalculateDistanceWhenBothAddressesArePresent() {
                Student student = Student.builder()
                                .studentId(10L)
                                .address(Address.builder()
                                                .latitude(22.5726)
                                                .longitude(88.3639)
                                                .build())
                                .build();

                Tutor tutor = createTutor(
                                1L,
                                VerificationStatus.APPROVED,
                                ProfileStatus.LIVE,
                                false, true, false,
                                22.545, 88.352);

                tutor.setPreferredMode(PreferredMode.builder()
                                .online(false)
                                .studentHome(true)
                                .tutorHome(false)
                                .build());

                tutor.setAddress(Address.builder()
                                .latitude(22.545)
                                .longitude(88.352)
                                .build());

                Double distance = ReflectionTestUtils.invokeMethod(
                                matchService,
                                "calculateTutorDistance",
                                student,
                                tutor);

                assertThat(distance).isNotNull();
                assertThat(distance).isPositive();
        }

}
