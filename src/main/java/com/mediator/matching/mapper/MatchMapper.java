package com.mediator.matching.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.matching.dto.response.TutorCardResponse;
import com.mediator.matching.dto.response.TutorDetailsResponse;
import com.mediator.matching.dto.response.TutorTeachingSubjectResponse;
import com.mediator.matching.entity.MatchScore;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorTeachingPreference;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MatchMapper {

        private final PreferredModeMapper preferredModeMapper;

        // ============================================================
        // TUTOR CARD
        // ============================================================

        public TutorCardResponse toCard(
                        MatchScore score,
                        List<TutorTeachingPreference> preferences) {

                Tutor tutor = score.getTutor();

                List<String> subjects = new ArrayList<>();

                if (preferences != null) {

                        for (TutorTeachingPreference preference : preferences) {

                                if (preference.getSubject() != null
                                                && preference.getSubject().getName() != null) {

                                        String subjectName = preference.getSubject().getName();

                                        if (!subjects.contains(subjectName)) {
                                                subjects.add(subjectName);
                                        }
                                }
                        }
                }

                return TutorCardResponse.builder()
                                .tutorId(tutor.getTutorId())

                                .firstName(
                                                tutor.getUser().getFirstName())

                                .lastName(
                                                tutor.getUser().getLastName())

                                .highestQualification(
                                                tutor.getAcademicProfile() != null
                                                                ? tutor.getAcademicProfile()
                                                                                .getHighestQualification()
                                                                : null)

                                .teachingExperienceYears(
                                                tutor.getTeachingExperienceYears())

                                .subjects(subjects)

                                .matchedSubjects(
                                                score.getMatchedSubjects())

                                .totalRequestedSubjects(
                                                score.getTotalRequestedSubjects())

                                .matchPercentage(
                                                score.getMatchPercentage())

                                .distance(
                                                score.getDistance())

                                .build();
        }

        // ============================================================
        // TUTOR DETAILS
        // ============================================================

        public TutorDetailsResponse toDetails(
                        Tutor tutor,
                        Double distance,
                        List<TutorTeachingPreference> preferences) {

                List<TutorTeachingSubjectResponse> subjects = new ArrayList<>();

                if (preferences != null) {

                        for (TutorTeachingPreference preference : preferences) {

                                if (preference.getSubject() == null) {
                                        continue;
                                }

                                String classLevelName = null;

                                if (preference.getClassLevel() != null
                                                && preference.getClassLevel()
                                                                .getStandard() != null) {

                                        classLevelName = "Class "
                                                        + preference.getClassLevel()
                                                                        .getStandard();
                                }

                                TutorTeachingSubjectResponse subjectResponse = TutorTeachingSubjectResponse.builder()

                                                .subjectId(
                                                                preference.getSubject()
                                                                                .getId())

                                                .subjectName(
                                                                preference.getSubject()
                                                                                .getName())

                                                .classLevelId(
                                                                preference.getClassLevel() != null
                                                                                ? preference.getClassLevel()
                                                                                                .getId()
                                                                                : null)

                                                .classLevelName(
                                                                classLevelName)

                                                .boardId(
                                                                preference.getBoard() != null
                                                                                ? preference.getBoard()
                                                                                                .getId()
                                                                                : null)

                                                .boardName(
                                                                preference.getBoard() != null
                                                                                ? preference.getBoard()
                                                                                                .getName()
                                                                                : null)

                                                .build();

                                subjects.add(subjectResponse);
                        }
                }

                return TutorDetailsResponse.builder()

                                .tutorId(
                                                tutor.getTutorId())

                                .firstName(
                                                tutor.getUser().getFirstName())

                                .lastName(
                                                tutor.getUser().getLastName())

                                .highestQualification(
                                                tutor.getAcademicProfile() != null
                                                                ? tutor.getAcademicProfile()
                                                                                .getHighestQualification()
                                                                : null)

                                .teachingExperienceYears(
                                                tutor.getTeachingExperienceYears())

                                .industryExperienceYears(
                                                tutor.getIndustryExperienceYears())

                                .currentOccupation(
                                                tutor.getCurrentOccupation())

                                .description(
                                                tutor.getDescription())

                                .preferredMode(
                                                preferredModeMapper.toDto(
                                                                tutor.getPreferredMode()))

                                .distance(distance)

                                .salaryMin(
                                                tutor.getSalaryMin())

                                .salaryMax(
                                                tutor.getSalaryMax())

                                .demoVideoUrl(
                                                tutor.getDemoVideoUrl())

                                .subjects(subjects)

                                .build();
        }
}