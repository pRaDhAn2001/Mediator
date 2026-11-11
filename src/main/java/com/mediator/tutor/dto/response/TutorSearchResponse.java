package com.mediator.tutor.dto.response;

import java.util.List;

import com.mediator.common.base.PreferredMode;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tutor details returned as an individual search result.")
public class TutorSearchResponse {

    @Schema(description = "Unique tutor ID.", example = "101")
    private Long tutorId;

    @Schema(description = "Tutor's display name.", example = "Rahul Sharma")
    private String tutorName;

    @Schema(description = "Tutor's city.", example = "Hyderabad")
    private String city;

    @Schema(description = "Years of teaching experience.", example = "5")
    private Integer teachingExperienceYears;

    @Schema(description = "Minimum expected monthly salary.", example = "5000.0")
    private Double salaryMin;

    @Schema(description = "Maximum expected monthly salary.", example = "15000.0")
    private Double salaryMax;

    @Schema(description = "Tutor's preferred teaching modes.")
    private PreferredMode preferredMode;

    @Schema(description = "Whether the tutor is verified.", example = "true")
    private Boolean verified;

    @Schema(description = "Distance from the search location in the application's configured unit.", example = "4.5")
    private Double distance;

    @Schema(description = "Number of requested subjects matched by this tutor.", example = "3")
    private Integer matchedSubjectCount;

    @ArraySchema(arraySchema = @Schema(description = "Names of subjects matched by this tutor."), schema = @Schema(example = "Mathematics"))
    private List<String> matchedSubjects;
}