package com.mediator.student.service;

import com.mediator.auth.entity.User;
import com.mediator.common.base.Gender;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.common.mapper.AddressMapper;
import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.student.dto.dashboard.StudentDashboardResponse;
import com.mediator.student.dto.preference.StudentPreferenceDto;
import com.mediator.student.dto.request.StudentProfileRequest;
import com.mediator.student.dto.response.StudentProfileResponse;
import com.mediator.student.entity.ContactOwner;
import com.mediator.student.entity.Student;
import com.mediator.student.entity.StudentPreference;
import com.mediator.student.mapper.StudentMapper;
import com.mediator.student.repository.StudentPreferenceRepository;
import com.mediator.student.repository.StudentRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService.
 *
 * Tests:
 * 1. Profile retrieval
 * 2. Dashboard retrieval
 * 3. Profile update
 * 4. Parent/self contact handling
 * 5. Subject preference update
 * 6. Subject preference retrieval
 * 7. Validation/error scenarios
 * 8. Empty preference scenarios
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentPreferenceRepository studentPreferenceRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ClassLevelRepository classLevelRepository;

    @Spy
    private StudentMapper studentMapper = new StudentMapper(
            new AddressMapper(),
            new PreferredModeMapper());

    @InjectMocks
    private StudentService studentService;

    private User studentUser;
    private Student student;
    private Board board;
    private ClassLevel classLevel;
    private Subject math;
    private Subject physics;

    @BeforeEach
    void setUp() {

        /*
         * User
         */
        studentUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("student@test.com")
                .mobileNumber("9876543210")
                .build();

        /*
         * Board
         */
        board = Board.builder()
                .id(1L)
                .name("CBSE")
                .build();

        /*
         * Class Level
         */
        classLevel = ClassLevel.builder()
                .id(8L)
                .standard(8)
                .build();

        /*
         * Subjects
         */
        math = Subject.builder()
                .id(1L)
                .name("Mathematics")
                .build();

        physics = Subject.builder()
                .id(2L)
                .name("Physics")
                .build();

        /*
         * Student
         */
        student = Student.builder()
                .studentId(10L)
                .user(studentUser)
                .gender(Gender.MALE)
                .board(board)
                .classLevel(classLevel)
                .schoolName("St Xavier")
                .parentName("Jane Doe")
                .parentPhone("9876543210")
                .parentEmail("student@test.com")
                .build();

        /*
         * Mock Security Context
         */
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(authentication.getName())
                .thenReturn("student@test.com");

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // GET PROFILE
    // ============================================================

    @Test
    void getProfile_success() {

        StudentPreference preference = new StudentPreference();

        preference.setStudent(student);
        preference.setSubject(math);

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(List.of(preference));

        StudentProfileResponse response = studentService.getProfile();

        assertThat(response)
                .isNotNull();

        assertThat(response.getFirstName())
                .isEqualTo("John");

        assertThat(response.getBoardName())
                .isEqualTo("CBSE");

        assertThat(response.getSubjects())
                .hasSize(1);

        assertThat(response.getSubjects().get(0).getName())
                .isEqualTo("Mathematics");
    }

    @Test
    void getProfile_success_whenNoPreferencesExist() {

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(Collections.emptyList());

        StudentProfileResponse response = studentService.getProfile();

        assertThat(response)
                .isNotNull();

        assertThat(response.getSubjects())
                .isEmpty();
    }

    @Test
    void getProfile_throwsNotFound_whenStudentNotExists() {

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getProfile())

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Student profile not found");
    }

    // ============================================================
    // GET DASHBOARD
    // ============================================================

    @Test
    void getDashboard_success() {

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(List.of());

        StudentDashboardResponse response = studentService.getDashboard();

        assertThat(response)
                .isNotNull();

        assertThat(response.getProfile())
                .isNotNull();

        assertThat(response.getProfileCompletion())
                .isNotNull();

        assertThat(response.getActiveMatchesCount())
                .isEqualTo(0L);

        assertThat(response.getPendingMatchesCount())
                .isEqualTo(0L);

        assertThat(response.getCancelledMatchesCount())
                .isEqualTo(0L);
    }

    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    @Test
    void updateProfile_success_sameAsPrimaryPhoneAndEmail() {

        StudentProfileRequest request = StudentProfileRequest.builder()

                .gender(Gender.MALE)

                .boardId(1L)

                .classLevelId(8L)

                .schoolName("St Xavier")

                .parentName("Jane Doe")

                .sameAsPrimaryPhone(true)

                .sameAsPrimaryEmail(true)

                .address(
                        AddressDto.builder()
                                .city("Kolkata")
                                .build())

                .preferredMode(
                        PreferredModeDto.builder()
                                .studentHome(true)
                                .build())

                .preferredRadius(5.0)

                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(boardRepository.findById(1L))
                .thenReturn(Optional.of(board));

        when(classLevelRepository.findById(8L))
                .thenReturn(Optional.of(classLevel));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(List.of());

        StudentProfileResponse response = studentService.updateProfile(request);

        assertThat(response)
                .isNotNull();

        assertThat(student.getParentPhone())
                .isEqualTo("9876543210");

        assertThat(student.getPrimaryMobileOwner())
                .isEqualTo(ContactOwner.SELF);

        assertThat(student.getParentEmail())
                .isEqualTo("student@test.com");

        assertThat(student.getPrimaryEmailOwner())
                .isEqualTo(ContactOwner.SELF);

        verify(studentRepository)
                .save(student);
    }

    @Test
    void updateProfile_success_customParentPhoneAndEmail() {

        StudentProfileRequest request = StudentProfileRequest.builder()

                .gender(Gender.MALE)

                .boardId(1L)

                .classLevelId(8L)

                .sameAsPrimaryPhone(false)

                .sameAsPrimaryEmail(false)

                .parentPhone("9123456789")

                .parentEmail("parent@test.com")

                .address(
                        AddressDto.builder()
                                .city("Kolkata")
                                .build())

                .preferredMode(
                        PreferredModeDto.builder()
                                .studentHome(true)
                                .build())

                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(boardRepository.findById(1L))
                .thenReturn(Optional.of(board));

        when(classLevelRepository.findById(8L))
                .thenReturn(Optional.of(classLevel));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(List.of());

        studentService.updateProfile(request);

        assertThat(student.getParentPhone())
                .isEqualTo("9123456789");

        assertThat(student.getPrimaryMobileOwner())
                .isEqualTo(ContactOwner.PARENT);

        assertThat(student.getParentEmail())
                .isEqualTo("parent@test.com");

        assertThat(student.getPrimaryEmailOwner())
                .isEqualTo(ContactOwner.PARENT);

        verify(studentRepository)
                .save(student);
    }

    @Test
    void updateProfile_throwsNotFound_whenBoardDoesNotExist() {

        StudentProfileRequest request = StudentProfileRequest.builder()
                .boardId(999L)
                .classLevelId(8L)
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(boardRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateProfile(request))

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Board not found with id");
    }

    @Test
    void updateProfile_throwsNotFound_whenClassLevelDoesNotExist() {

        StudentProfileRequest request = StudentProfileRequest.builder()
                .boardId(1L)
                .classLevelId(999L)
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(boardRepository.findById(1L))
                .thenReturn(Optional.of(board));

        when(classLevelRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateProfile(request))

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Class Level not found with id");
    }

    @Test
    void updateProfile_throwsNotFound_whenStudentDoesNotExist() {

        StudentProfileRequest request = StudentProfileRequest.builder()
                .boardId(1L)
                .classLevelId(8L)
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updateProfile(request))

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Student profile not found");
    }

    // ============================================================
    // UPDATE PREFERENCES
    // ============================================================

    @Test
    void updatePreferences_success_singleSubject() {

        StudentPreferenceDto dto = StudentPreferenceDto.builder()
                .subjectIds(List.of(1L))
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(subjectRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(math));

        List<SubjectResponse> responses = studentService.updatePreferences(dto);

        assertThat(responses)
                .hasSize(1);

        assertThat(responses.get(0).getName())
                .isEqualTo("Mathematics");

        verify(studentPreferenceRepository)
                .deleteByStudent(student);

        verify(studentPreferenceRepository)
                .saveAll(any());
    }

    @Test
    void updatePreferences_success_multipleSubjects() {

        StudentPreferenceDto dto = StudentPreferenceDto.builder()
                .subjectIds(List.of(1L, 2L))
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(subjectRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(math, physics));

        List<SubjectResponse> responses = studentService.updatePreferences(dto);

        assertThat(responses)
                .hasSize(2);

        assertThat(responses)
                .extracting(SubjectResponse::getName)
                .containsExactly(
                        "Mathematics",
                        "Physics");

        verify(studentPreferenceRepository)
                .deleteByStudent(student);

        verify(studentPreferenceRepository)
                .saveAll(any());
    }

    @Test
    void updatePreferences_success_emptySubjectList() {

        StudentPreferenceDto dto = StudentPreferenceDto.builder()
                .subjectIds(List.of())
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(subjectRepository.findAllById(List.of()))
                .thenReturn(List.of());

        List<SubjectResponse> responses = studentService.updatePreferences(dto);

        assertThat(responses)
                .isEmpty();

        /*
         * Existing preferences must still be deleted.
         */
        verify(studentPreferenceRepository)
                .deleteByStudent(student);

        /*
         * No new preferences should be inserted.
         */
        verify(studentPreferenceRepository, never())
                .saveAll(any());
    }

    @Test
    void updatePreferences_throwsExceptionWhenSubjectNotFound() {

        StudentPreferenceDto dto = StudentPreferenceDto.builder()
                .subjectIds(List.of(1L, 99L))
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        /*
         * Only Mathematics exists.
         * Subject 99 does not exist.
         */
        when(subjectRepository.findAllById(List.of(1L, 99L)))
                .thenReturn(List.of(math));

        assertThatThrownBy(() -> studentService.updatePreferences(dto))

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "One or more selected subjects do not exist");

        /*
         * Existing preferences should not be deleted
         * when validation fails before replacement.
         */
        verify(studentPreferenceRepository, never())
                .deleteByStudent(any());

        verify(studentPreferenceRepository, never())
                .saveAll(any());
    }

    @Test
    void updatePreferences_throwsNotFound_whenStudentDoesNotExist() {

        StudentPreferenceDto dto = StudentPreferenceDto.builder()
                .subjectIds(List.of(1L))
                .build();

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updatePreferences(dto))

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Student profile not found");

        verifyNoInteractions(subjectRepository);
        verifyNoInteractions(studentPreferenceRepository);
    }

    // ============================================================
    // GET PREFERENCES
    // ============================================================

    @Test
    void getPreferences_success() {

        StudentPreference pref = new StudentPreference();

        pref.setStudent(student);
        pref.setSubject(math);

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(List.of(pref));

        List<SubjectResponse> responses = studentService.getPreferences();

        assertThat(responses)
                .hasSize(1);

        assertThat(responses.get(0).getName())
                .isEqualTo("Mathematics");
    }

    @Test
    void getPreferences_success_whenNoPreferencesExist() {

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.of(student));

        when(studentPreferenceRepository.findByStudent_StudentId(10L))
                .thenReturn(Collections.emptyList());

        List<SubjectResponse> responses = studentService.getPreferences();

        assertThat(responses)
                .isEmpty();
    }

    @Test
    void getPreferences_throwsNotFound_whenStudentDoesNotExist() {

        when(studentRepository.findByUserEmail("student@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getPreferences())

                .isInstanceOf(ResourceNotFoundException.class)

                .hasMessageContaining(
                        "Student profile not found");

        verifyNoInteractions(studentPreferenceRepository);
    }
}