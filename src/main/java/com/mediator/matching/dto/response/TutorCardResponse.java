package com.mediator.matching.dto.response;

import com.mediator.tutor.entity.HighestQualification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorCardResponse {

    private Long tutorId;

    private String firstName;

    private String lastName;

    private HighestQualification highestQualification;

    private Integer teachingExperienceYears;

    private List<String> subjects;

    private Integer matchedSubjects;

    private Integer totalRequestedSubjects;

    private Double matchPercentage;

    private Double distance;
}