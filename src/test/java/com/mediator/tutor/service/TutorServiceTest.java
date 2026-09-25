package com.mediator.tutor.service;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.base.Gender;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.mediator.common.mapper.AddressMapper;
import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.tutor.dto.dashboard.TutorDashboardResponse;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.request.TutorTeachingPreferenceRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.DocumentType;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorDocument;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.mapper.AcademicProfileMapper;
import com.mediator.tutor.mapper.TutorMapper;
import com.mediator.tutor.repository.TutorDocumentRepository;
import com.mediator.tutor.repository.TutorRepository;
import com.mediator.tutor.repository.TutorTeachingPreferenceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TutorServiceTest {

        @Mock
        private TutorRepository tutorRepository;

        @Mock
        private TutorDocumentRepository tutorDocumentRepository;

        @Mock
        private TutorTeachingPreferenceRepository teachingPreferenceRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SubjectRepository subjectRepository;

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private ClassLevelRepository classLevelRepository;

        @Mock
        private SubscriptionRepository subscriptionRepository;

        @Spy
        private TutorMapper tutorMapper = new TutorMapper(
                        new AddressMapper(),
                        new PreferredModeMapper(),
                        new AcademicProfileMapper());

        @InjectMocks
        private TutorService tutorService;

        private User tutorUser;
        private Tutor tutor;

        private Board board;
        private ClassLevel classLevel;
        private Subject math;

        @BeforeEach
        void setUp() {

                tutorUser = User.builder()
                                .id(1L)
                                .firstName("John")
                                .lastName("Tutor")
                                .email("tutor@test.com")
                                .mobileNumber("9876543210")
                                .build();

                tutor = Tutor.builder()
                                .tutorId(10L)
                                .user(tutorUser)
                                .gender(Gender.MALE)
                                .description("Experienced Tutor")
                                .verificationStatus(VerificationStatus.APPROVED)
                                .profileStatus(ProfileStatus.LIVE)
                                .salaryMin(BigDecimal.valueOf(500))
                                .salaryMax(BigDecimal.valueOf(1000))
                                .build();

                board = Board.builder()
                                .id(1L)
                                .name("CBSE")
                                .build();

                classLevel = ClassLevel.builder()
                                .id(8L)
                                .standard(8)
                                .build();

                math = Subject.builder()
                                .id(1L)
                                .name("Mathematics")
                                .build();
        }

        // ============================================================
        // GET PROFILE
        // ============================================================

        @Test
        void getProfile_success() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(tutorDocumentRepository.findByTutor_TutorId(10L))
                                .thenReturn(List.of());

                TutorProfileResponse response = tutorService.getProfile("tutor@test.com");

                assertThat(response).isNotNull();
                assertThat(response.getTutorId()).isEqualTo(10L);
                assertThat(response.getFirstName()).isEqualTo("John");
                assertThat(response.getLastName()).isEqualTo("Tutor");

                verify(userRepository).findByEmail("tutor@test.com");
                verify(tutorRepository).findByUser(tutorUser);
                verify(tutorDocumentRepository, atLeastOnce())
                                .findByTutor_TutorId(10L);
        }

        @Test
        void getProfile_throwsNotFound_whenUserDoesNotExist() {

                when(userRepository.findByEmail("unknown@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.getProfile("unknown@test.com"))
                                .isInstanceOf(UsernameNotFoundException.class);

                verify(userRepository)
                                .findByEmail("unknown@test.com");

                verifyNoInteractions(tutorRepository);
        }

        @Test
        void getProfile_throwsNotFound_whenTutorProfileDoesNotExist() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.getProfile("tutor@test.com"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Tutor profile not found");

                verify(tutorRepository)
                                .findByUser(tutorUser);

                verifyNoInteractions(tutorDocumentRepository);
        }

        // ============================================================
        // UPDATE PROFILE
        // ============================================================

        @Test
        void updateProfile_success() {

                TutorProfileRequest request = TutorProfileRequest.builder()
                                .gender(Gender.MALE)
                                .description("Updated bio")
                                .salaryMin(BigDecimal.valueOf(400))
                                .salaryMax(BigDecimal.valueOf(800))
                                .address(
                                                AddressDto.builder()
                                                                .city("Kolkata")
                                                                .build())
                                .preferredMode(
                                                PreferredModeDto.builder()
                                                                .studentHome(true)
                                                                .build())
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(tutorRepository.save(any(Tutor.class)))
                                .thenReturn(tutor);

                TutorProfileResponse response = tutorService.updateProfile(
                                "tutor@test.com",
                                request);

                assertThat(response).isNotNull();

                verify(tutorRepository)
                                .save(tutor);
        }

        @Test
        void updateProfile_throwsException_whenMinimumSalaryGreaterThanMaximum() {

                TutorProfileRequest request = TutorProfileRequest.builder()
                                .salaryMin(BigDecimal.valueOf(1000))
                                .salaryMax(BigDecimal.valueOf(500))
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> tutorService.updateProfile(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining(
                                                "Minimum salary cannot be greater than maximum salary");

                verify(tutorRepository, never())
                                .save(any(Tutor.class));
        }

        @Test
        void updateProfile_throwsNotFound_whenUserDoesNotExist() {

                TutorProfileRequest request = TutorProfileRequest.builder()
                                .description("Updated")
                                .build();

                when(userRepository.findByEmail("unknown@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.updateProfile(
                                "unknown@test.com",
                                request))
                                .isInstanceOf(UsernameNotFoundException.class);

                verify(tutorRepository, never())
                                .save(any(Tutor.class));
        }

        @Test
        void updateProfile_throwsNotFound_whenTutorDoesNotExist() {

                TutorProfileRequest request = TutorProfileRequest.builder()
                                .description("Updated")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.updateProfile(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(tutorRepository, never())
                                .save(any(Tutor.class));
        }

        // ============================================================
        // TEACHING PREFERENCES
        // ============================================================

        @Test
        void updateTeachingPreferences_success() {

                TutorTeachingPreferenceDto prefDto = TutorTeachingPreferenceDto.builder()
                                .boardId(1L)
                                .classLevelId(8L)
                                .subjectId(1L)
                                .build();

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of(prefDto))
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(boardRepository.findById(1L))
                                .thenReturn(Optional.of(board));

                when(classLevelRepository.findById(8L))
                                .thenReturn(Optional.of(classLevel));

                when(subjectRepository.findById(1L))
                                .thenReturn(Optional.of(math));

                tutorService.updateTeachingPreferences(
                                "tutor@test.com",
                                request);

                verify(teachingPreferenceRepository)
                                .deleteByTutor_TutorId(10L);

                verify(teachingPreferenceRepository)
                                .saveAll(any());
        }

        @Test
        void updateTeachingPreferences_throwsException_whenPreferencesAreEmpty() {

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of())
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> tutorService.updateTeachingPreferences(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "At least one teaching preference is required");

                verify(teachingPreferenceRepository, never())
                                .deleteByTutor_TutorId(anyLong());

                verify(teachingPreferenceRepository, never())
                                .saveAll(any());
        }

        @Test
        void updateTeachingPreferences_throwsNotFound_whenBoardDoesNotExist() {

                TutorTeachingPreferenceDto prefDto = TutorTeachingPreferenceDto.builder()
                                .boardId(999L)
                                .classLevelId(8L)
                                .subjectId(1L)
                                .build();

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of(prefDto))
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(boardRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.updateTeachingPreferences(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(teachingPreferenceRepository, never())
                                .saveAll(any());
        }

        @Test
        void updateTeachingPreferences_throwsNotFound_whenClassLevelDoesNotExist() {

                TutorTeachingPreferenceDto prefDto = TutorTeachingPreferenceDto.builder()
                                .boardId(1L)
                                .classLevelId(999L)
                                .subjectId(1L)
                                .build();

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of(prefDto))
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(boardRepository.findById(1L))
                                .thenReturn(Optional.of(board));

                when(classLevelRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.updateTeachingPreferences(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(teachingPreferenceRepository, never())
                                .saveAll(any());
        }

        @Test
        void updateTeachingPreferences_throwsNotFound_whenSubjectDoesNotExist() {

                TutorTeachingPreferenceDto prefDto = TutorTeachingPreferenceDto.builder()
                                .boardId(1L)
                                .classLevelId(8L)
                                .subjectId(999L)
                                .build();

                TutorTeachingPreferenceRequest request = TutorTeachingPreferenceRequest.builder()
                                .preferences(List.of(prefDto))
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(boardRepository.findById(1L))
                                .thenReturn(Optional.of(board));

                when(classLevelRepository.findById(8L))
                                .thenReturn(Optional.of(classLevel));

                when(subjectRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.updateTeachingPreferences(
                                "tutor@test.com",
                                request))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(teachingPreferenceRepository, never())
                                .saveAll(any());
        }

        // ============================================================
        // DASHBOARD
        // ============================================================

        @Test
        void getDashboard_success_withoutSubscription() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                10L,
                                                SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.empty());

                TutorDashboardResponse dashboard = tutorService.getDashboard(
                                "tutor@test.com");

                assertThat(dashboard).isNotNull();
                assertThat(dashboard.getVerificationStatus())
                                .isEqualTo(VerificationStatus.APPROVED);
        }

        @Test
        void getDashboard_success_withActiveSubscription() {

                SubscriptionPlan plan = SubscriptionPlan.builder()
                                .subscriptionPlanId(1L)
                                .planType(SubscriptionPlanType.PRO)
                                .durationInMonths(6)
                                .price(BigDecimal.valueOf(1599))
                                .active(true)
                                .build();

                Subscription subscription = Subscription.builder()
                                .subscriptionId(100L)
                                .tutor(tutor)
                                .subscriptionPlan(plan)
                                .status(SubscriptionStatus.ACTIVE)
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(subscriptionRepository
                                .findByTutor_TutorIdAndStatus(
                                                10L,
                                                SubscriptionStatus.ACTIVE))
                                .thenReturn(Optional.of(subscription));

                TutorDashboardResponse dashboard = tutorService.getDashboard(
                                "tutor@test.com");

                assertThat(dashboard).isNotNull();
                assertThat(dashboard.getVerificationStatus())
                                .isEqualTo(VerificationStatus.APPROVED);
        }

        @Test
        void getDashboard_throwsNotFound_whenUserDoesNotExist() {

                when(userRepository.findByEmail("unknown@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.getDashboard(
                                "unknown@test.com"))
                                .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        void getDashboard_throwsNotFound_whenTutorDoesNotExist() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.getDashboard(
                                "tutor@test.com"))
                                .isInstanceOf(ResourceNotFoundException.class);
        }

        // ============================================================
        // DOCUMENT UPLOAD
        // ============================================================

        @Test
        void uploadDocuments_success() {

                TutorDocumentDto docDto = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/aadhar.pdf")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                tutorService.uploadDocuments(
                                "tutor@test.com",
                                List.of(docDto));

                verify(tutorDocumentRepository)
                                .saveAll(any());
        }

        @Test
        void uploadDocuments_success_withMultipleDocuments() {

                TutorDocumentDto aadhar = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/aadhar.pdf")
                                .build();

                TutorDocumentDto degree = TutorDocumentDto.builder()
                                .documentType(DocumentType.DEGREE_CERTIFICATE)
                                .documentUrl(
                                                "http://example.com/degree.pdf")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                tutorService.uploadDocuments(
                                "tutor@test.com",
                                List.of(aadhar, degree));

                verify(tutorDocumentRepository)
                                .saveAll(any());
        }

        @Test
        void uploadDocuments_throwsException_whenDuplicateDocumentType() {

                TutorDocumentDto doc1 = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/doc1.pdf")
                                .build();

                TutorDocumentDto doc2 = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/doc2.pdf")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                assertThatThrownBy(() -> tutorService.uploadDocuments(
                                "tutor@test.com",
                                List.of(doc1, doc2)))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                                "Duplicate document type");

                verify(tutorDocumentRepository, never())
                                .saveAll(any());
        }

        @Test
        void uploadDocuments_throwsNotFound_whenUserDoesNotExist() {

                TutorDocumentDto document = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/aadhar.pdf")
                                .build();

                when(userRepository.findByEmail("unknown@test.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.uploadDocuments(
                                "unknown@test.com",
                                List.of(document)))
                                .isInstanceOf(UsernameNotFoundException.class);

                verify(tutorDocumentRepository, never())
                                .saveAll(any());
        }

        @Test
        void uploadDocuments_throwsNotFound_whenTutorDoesNotExist() {

                TutorDocumentDto document = TutorDocumentDto.builder()
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/aadhar.pdf")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.uploadDocuments(
                                "tutor@test.com",
                                List.of(document)))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(tutorDocumentRepository, never())
                                .saveAll(any());
        }

        // ============================================================
        // DELETE DOCUMENT
        // ============================================================

        @Test
        void deleteDocument_success() {

                TutorDocument document = TutorDocument.builder()
                                .documentId(100L)
                                .tutor(tutor)
                                .documentType(DocumentType.AADHAAR_CARD)
                                .documentUrl(
                                                "http://example.com/aadhar.pdf")
                                .build();

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(tutorDocumentRepository
                                .findByDocumentIdAndTutor_TutorId(
                                                100L,
                                                10L))
                                .thenReturn(Optional.of(document));

                tutorService.deleteDocument(
                                "tutor@test.com",
                                100L);

                verify(tutorDocumentRepository)
                                .delete(document);
        }

        @Test
        void deleteDocument_throwsNotFound_whenDocumentDoesNotExist() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.of(tutor));

                when(tutorDocumentRepository
                                .findByDocumentIdAndTutor_TutorId(
                                                999L,
                                                10L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.deleteDocument(
                                "tutor@test.com",
                                999L))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(tutorDocumentRepository, never())
                                .delete(any(TutorDocument.class));
        }

        @Test
        void deleteDocument_throwsNotFound_whenTutorDoesNotExist() {

                when(userRepository.findByEmail("tutor@test.com"))
                                .thenReturn(Optional.of(tutorUser));

                when(tutorRepository.findByUser(tutorUser))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> tutorService.deleteDocument(
                                "tutor@test.com",
                                100L))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(tutorDocumentRepository, never())
                                .delete(any(TutorDocument.class));
        }
}