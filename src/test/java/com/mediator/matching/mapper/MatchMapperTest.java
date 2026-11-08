package com.mediator.matching.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mediator.auth.entity.User;
import com.mediator.common.base.PreferredMode;
import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.matching.dto.response.TutorCardResponse;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorTeachingSubjectResponse;
import com.mediator.matching.entity.MatchScore;
import com.mediator.tutor.entity.AcademicProfile;
import com.mediator.tutor.entity.HighestQualification;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;

class MatchMapperTest {

        private MatchMapper mapper;

        @BeforeEach
        void setUp() {
                mapper = new MatchMapper(new PreferredModeMapper());
        }

        @Test
        void toCard_mapsTutorAndScoreFields() {
                Tutor tutor = createTutor();
                AcademicProfile academicProfile = AcademicProfile.builder()
                                .highestQualification(HighestQualification.BACHELORS)
                                .build();
                tutor.setAcademicProfile(academicProfile);

                Subject math = createSubject(1L, "Mathematics");
                Subject physics = createSubject(2L, "Physics");

                List<TutorTeachingPreference> preferences = Arrays.asList(
                                createPreference(math, null, null),
                                createPreference(physics, null, null),
                                createPreference(createSubject(3L, "Mathematics"), null, null),
                                createPreference(null, null, null),
                                createPreference(createSubject(4L, null), null, null));

                MatchScore score = MatchScore.builder()
                                .tutor(tutor)
                                .matchedSubjects(2)
                                .totalRequestedSubjects(3)
                                .matchPercentage(66.67)
                                .distance(4.5)
                                .score(10)
                                .build();

                TutorCardResponse response = mapper.toCard(score, preferences);

                assertEquals(20L, response.getTutorId());
                assertEquals("Amit", response.getFirstName());
                assertEquals("Sharma", response.getLastName());
                assertEquals(HighestQualification.BACHELORS,
                                response.getHighestQualification());
                assertEquals(5, response.getTeachingExperienceYears());
                assertEquals(List.of("Mathematics", "Physics"), response.getSubjects());
                assertEquals(2, response.getMatchedSubjects());
                assertEquals(3, response.getTotalRequestedSubjects());
                assertEquals(66.67, response.getMatchPercentage());
                assertEquals(4.5, response.getDistance());
        }

        @Test
        void toCard_nullPreferences_returnsEmptySubjects() {
                Tutor tutor = createTutor();

                MatchScore score = MatchScore.builder()
                                .tutor(tutor)
                                .matchedSubjects(0)
                                .totalRequestedSubjects(2)
                                .matchPercentage(0.0)
                                .distance(0.0)
                                .build();

                TutorCardResponse response = mapper.toCard(score, null);

                assertNotNull(response.getSubjects());
                assertTrue(response.getSubjects().isEmpty());
                assertNull(response.getHighestQualification());
        }

        @Test
        void toCard_emptyPreferences_returnsEmptySubjects() {
                MatchScore score = MatchScore.builder()
                                .tutor(createTutor())
                                .matchedSubjects(0)
                                .totalRequestedSubjects(0)
                                .matchPercentage(0.0)
                                .distance(0.0)
                                .build();

                TutorCardResponse response = mapper.toCard(score, List.of());

                assertTrue(response.getSubjects().isEmpty());
        }

        @Test
        void toDetails_mapsSubjectAndClassBoardInformation() {
                Tutor tutor = createTutor();
                tutor.setAcademicProfile(AcademicProfile.builder()
                                .highestQualification(HighestQualification.BACHELORS)
                                .build());
                tutor.setPreferredMode(PreferredMode.builder()
                                .online(true)
                                .tutorHome(false)
                                .studentHome(true)
                                .build());
                tutor.setSalaryMin(new BigDecimal("500"));
                tutor.setSalaryMax(new BigDecimal("1000"));
                tutor.setDescription("Experienced tutor");
                tutor.setCurrentOccupation("Teacher");
                tutor.setIndustryExperienceYears(2);
                tutor.setDemoVideoUrl("https://example.com/demo");

                ClassLevel classLevel = new ClassLevel();
                classLevel.setId(8L);
                classLevel.setStandard(8);

                Board board = new Board();
                board.setId(1L);
                board.setName("CBSE");

                TutorTeachingPreference preference = createPreference(
                                createSubject(10L, "Mathematics"),
                                classLevel,
                                board);

                TutorDetailsResponse response = mapper.toDetails(
                                tutor, 3.25, List.of(preference));

                assertEquals(20L, response.getTutorId());
                assertEquals("Amit", response.getFirstName());
                assertEquals("Sharma", response.getLastName());
                assertEquals(HighestQualification.BACHELORS,
                                response.getHighestQualification());
                assertEquals(5, response.getTeachingExperienceYears());
                assertEquals(2, response.getIndustryExperienceYears());
                assertEquals("Teacher", response.getCurrentOccupation());
                assertEquals("Experienced tutor", response.getDescription());
                assertEquals(3.25, response.getDistance());
                assertEquals(new BigDecimal("500"), response.getSalaryMin());
                assertEquals(new BigDecimal("1000"), response.getSalaryMax());
                assertEquals("https://example.com/demo", response.getDemoVideoUrl());

                assertNotNull(response.getPreferredMode());
                assertTrue(response.getPreferredMode().isOnline());
                assertFalse(response.getPreferredMode().isTutorHome());
                assertTrue(response.getPreferredMode().isStudentHome());

                assertEquals(1, response.getSubjects().size());

                TutorTeachingSubjectResponse subjectResponse = response.getSubjects().get(0);

                assertEquals(10L, subjectResponse.getSubjectId());
                assertEquals("Mathematics", subjectResponse.getSubjectName());
                assertEquals(8L, subjectResponse.getClassLevelId());
                assertEquals("Class 8", subjectResponse.getClassLevelName());
                assertEquals(1L, subjectResponse.getBoardId());
                assertEquals("CBSE", subjectResponse.getBoardName());
        }

        @Test
        void toDetails_nullPreferences_returnsEmptySubjects() {
                TutorDetailsResponse response = mapper.toDetails(createTutor(), 2.0, null);

                assertNotNull(response.getSubjects());
                assertTrue(response.getSubjects().isEmpty());
                assertNull(response.getHighestQualification());
                assertNull(response.getPreferredMode());
        }

        @Test
        void toDetails_skipsPreferenceWithNullSubject() {
                TutorTeachingPreference invalidPreference = createPreference(null, null, null);

                TutorDetailsResponse response = mapper.toDetails(
                                createTutor(), 1.0, List.of(invalidPreference));

                assertTrue(response.getSubjects().isEmpty());
        }

        @Test
        void toDetails_handlesNullClassLevelAndBoard() {
                TutorTeachingPreference preference = createPreference(
                                createSubject(10L, "Mathematics"), null, null);

                TutorDetailsResponse response = mapper.toDetails(
                                createTutor(), 1.0, List.of(preference));

                TutorTeachingSubjectResponse subjectResponse = response.getSubjects().get(0);

                assertEquals("Mathematics", subjectResponse.getSubjectName());
                assertNull(subjectResponse.getClassLevelId());
                assertNull(subjectResponse.getClassLevelName());
                assertNull(subjectResponse.getBoardId());
                assertNull(subjectResponse.getBoardName());
        }

        @Test
        void toDetails_handlesClassLevelWithNullStandard() {
                ClassLevel classLevel = new ClassLevel();
                classLevel.setId(8L);
                classLevel.setStandard(null);

                TutorTeachingPreference preference = createPreference(
                                createSubject(10L, "Mathematics"), classLevel, null);

                TutorDetailsResponse response = mapper.toDetails(
                                createTutor(), 1.0, List.of(preference));

                TutorTeachingSubjectResponse subjectResponse = response.getSubjects().get(0);

                assertEquals(8L, subjectResponse.getClassLevelId());
                assertNull(subjectResponse.getClassLevelName());
        }

        private Tutor createTutor() {
                User user = new User();
                user.setFirstName("Amit");
                user.setLastName("Sharma");

                Tutor tutor = new Tutor();
                tutor.setTutorId(20L);
                tutor.setUser(user);
                tutor.setTeachingExperienceYears(5);
                return tutor;
        }

        private Subject createSubject(Long id, String name) {
                Subject subject = new Subject();
                subject.setId(id);
                subject.setName(name);
                return subject;
        }

        private TutorTeachingPreference createPreference(
                        Subject subject,
                        ClassLevel classLevel,
                        Board board) {

                TutorTeachingPreference preference = new TutorTeachingPreference();
                preference.setSubject(subject);
                preference.setClassLevel(classLevel);
                preference.setBoard(board);
                return preference;
        }
}
