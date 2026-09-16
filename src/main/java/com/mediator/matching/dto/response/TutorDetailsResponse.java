package com.mediator.matching.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.mediator.common.dto.PreferredModeDto;
import com.mediator.tutor.entity.HighestQualification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDetailsResponse {

    private Long tutorId;

    private String firstName;

    private String lastName;

    private HighestQualification highestQualification;

    private Integer teachingExperienceYears;

    private Integer industryExperienceYears;

    private String currentOccupation;

    private String description;

    private PreferredModeDto preferredMode;

    private Double distance;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String demoVideoUrl;

    private List<TutorTeachingSubjectResponse> subjects;
}