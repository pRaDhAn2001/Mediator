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

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed tutor information returned when viewing a tutor profile")
public class TutorDetailsResponse {

    @Schema(description = "Tutor ID", example = "12")
    private Long tutorId;

    @Schema(description = "Tutor's first name", example = "Amit")
    private String firstName;

    @Schema(description = "Tutor's last name", example = "Kumar")
    private String lastName;

    @Schema(description = "Tutor's highest qualification", example = "BACHELORS")
    private HighestQualification highestQualification;

    @Schema(description = "Teaching experience in years", example = "5")
    private Integer teachingExperienceYears;

    @Schema(description = "Industry experience in years", example = "3")
    private Integer industryExperienceYears;

    @Schema(description = "Tutor's current occupation", example = "Software Engineer")
    private String currentOccupation;

    @Schema(description = "Tutor's profile description", example = "Experienced mathematics tutor.")
    private String description;

    @Schema(description = "Tutor's preferred teaching modes")
    private PreferredModeDto preferredMode;

    @Schema(description = "Distance from the student's location in kilometres, where applicable", example = "3.5")
    private Double distance;

    @Schema(description = "Minimum expected salary", example = "3000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum expected salary", example = "5000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Tutor's demo video URL", example = "https://example.com/demo-video")
    private String demoVideoUrl;

    @ArraySchema(schema = @Schema(implementation = TutorTeachingSubjectResponse.class))
    private List<TutorTeachingSubjectResponse> subjects;
}