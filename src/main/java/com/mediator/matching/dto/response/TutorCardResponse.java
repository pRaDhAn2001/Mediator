package com.mediator.matching.dto.response;

import com.mediator.tutor.entity.HighestQualification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary card containing tutor information in search results")
public class TutorCardResponse {

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

    @ArraySchema(schema = @Schema(description = "Subject name", example = "Mathematics"))
    private List<String> subjects;

    @Schema(description = "Number of selected subjects matched by this tutor", example = "3")
    private Integer matchedSubjects;

    @Schema(description = "Total number of subjects requested by the student", example = "4")
    private Integer totalRequestedSubjects;

    @Schema(description = "Percentage of requested subjects matched", example = "75.0")
    private Double matchPercentage;

    @Schema(description = "Distance from the student's location in kilometres, where applicable", example = "3.5")
    private Double distance;
}