
package com.mediator.tutor.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TutorTeachingPreferenceRepositoryTest {

    @Autowired
    private TutorTeachingPreferenceRepository preferenceRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ClassLevelRepository classLevelRepository;

    @Autowired
    private BoardRepository boardRepository;

    private int userCounter = 0;

    private User createUser() {
        userCounter++;
        String suffix = String.valueOf(userCounter);

        User user = User.builder()
                .email("tutor" + suffix + "@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("Tutor")
                .mobileNumber(
                        "9" + String.format("%09d", userCounter))
                .role(Role.ROLE_TUTOR)
                .build();

        return userRepository.saveAndFlush(user);
    }

    private Tutor createTutor() {
        User user = createUser();

        Tutor tutor = Tutor.builder()
                .user(user)
                .build();

        return tutorRepository.saveAndFlush(tutor);
    }

    private Subject createSubject(String name) {
        return subjectRepository.saveAndFlush(
                Subject.builder().name(name).build());
    }

    private ClassLevel createClassLevel(Integer standard) {
        return classLevelRepository.saveAndFlush(
                ClassLevel.builder().standard(standard).build());
    }

    private Board createBoard(String name) {
        return boardRepository.saveAndFlush(
                Board.builder().name(name).build());
    }

    private TutorTeachingPreference createPreference(
            Tutor tutor,
            Subject subject,
            ClassLevel classLevel,
            Board board) {
        TutorTeachingPreference preference = new TutorTeachingPreference();

        preference.setTutor(tutor);
        preference.setSubject(subject);
        preference.setClassLevel(classLevel);
        preference.setBoard(board);

        return preferenceRepository.saveAndFlush(preference);
    }

    @Test
    void findByTutorTutorId_shouldReturnPreferencesForTutor() {
        Tutor tutor = createTutor();
        Subject maths = createSubject("Mathematics");
        Subject science = createSubject("Science");
        ClassLevel level = createClassLevel(8);
        Board board = createBoard("CBSE");

        createPreference(tutor, maths, level, board);
        createPreference(tutor, science, level, board);

        assertThat(
                preferenceRepository.findByTutor_TutorId(
                        tutor.getTutorId()))
                .hasSize(2);
    }

    @Test
    void findByTutorTutorId_shouldReturnEmptyListWhenNoPreferencesExist() {
        Tutor tutor = createTutor();

        assertThat(
                preferenceRepository.findByTutor_TutorId(
                        tutor.getTutorId()))
                .isEmpty();
    }

    @Test
    void findBySubjectId_shouldReturnPreferencesForSubject() {
        Tutor tutor = createTutor();
        Subject maths = createSubject("Mathematics");
        Subject science = createSubject("Science");
        ClassLevel level = createClassLevel(9);
        Board board = createBoard("ICSE");

        createPreference(tutor, maths, level, board);
        createPreference(tutor, science, level, board);

        List<TutorTeachingPreference> results = preferenceRepository.findBySubject_Id(maths.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubject().getId())
                .isEqualTo(maths.getId());
    }

    @Test
    void findByClassLevelId_shouldReturnMatchingPreferences() {
        Tutor tutor = createTutor();
        Subject maths = createSubject("Mathematics");
        ClassLevel level8 = createClassLevel(8);
        ClassLevel level9 = createClassLevel(9);
        Board board = createBoard("CBSE");

        createPreference(tutor, maths, level8, board);
        createPreference(tutor, maths, level9, board);

        assertThat(
                preferenceRepository.findByClassLevel_Id(level8.getId())).hasSize(1);
    }

    @Test
    void findByBoardId_shouldReturnMatchingPreferences() {
        Tutor tutor = createTutor();
        Subject maths = createSubject("Mathematics");
        ClassLevel level = createClassLevel(10);
        Board cbse = createBoard("CBSE");
        Board icse = createBoard("ICSE");

        createPreference(tutor, maths, level, cbse);
        createPreference(tutor, maths, level, icse);

        assertThat(
                preferenceRepository.findByBoard_Id(cbse.getId())).hasSize(1);
    }

    @Test
    void deleteByTutorTutorId_shouldDeleteOnlyThatTutorsPreferences() {
        Tutor tutor1 = createTutor();
        Tutor tutor2 = createTutor();

        Subject maths = createSubject("Mathematics");
        ClassLevel level = createClassLevel(11);
        Board board = createBoard("CBSE");

        createPreference(tutor1, maths, level, board);
        createPreference(tutor2, maths, level, board);

        preferenceRepository.deleteByTutor_TutorId(
                tutor1.getTutorId());

        assertThat(
                preferenceRepository.findByTutor_TutorId(
                        tutor1.getTutorId()))
                .isEmpty();

        assertThat(
                preferenceRepository.findByTutor_TutorId(
                        tutor2.getTutorId()))
                .hasSize(1);
    }
}
