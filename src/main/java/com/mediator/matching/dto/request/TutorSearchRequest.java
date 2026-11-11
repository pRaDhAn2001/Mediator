package com.mediator.matching.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.mediator.common.dto.PreferredModeDto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filters and pagination details for searching tutors")
public class TutorSearchRequest {

    @NotEmpty(message = "Please select at least one subject.")
    @ArraySchema(schema = @Schema(description = "Subject ID", example = "1"), arraySchema = @Schema(description = "Subjects selected by the student"))
    private List<Long> subjectIds;

    @Schema(description = "Optional board filter", example = "1")
    private Long boardId;

    @Schema(description = "Optional class-level filter", example = "8")
    private Long classLevelId;

    @Schema(description = "Requested teaching mode")
    private PreferredModeDto preferredMode;

    @Schema(description = "Student's latitude. Required only when studentHome is true.", example = "17.4401")
    private Double latitude;

    @Schema(description = "Student's longitude. Required only when studentHome is true.", example = "78.3489")
    private Double longitude;

    @Builder.Default
    @DecimalMin(value = "0.1", message = "Radius must be greater than 0.")
    @Schema(description = "Search radius in kilometres. Used only for studentHome searches.", example = "10.0", defaultValue = "10.0", minimum = "0.1")
    private Double radius = 10.0;

    @Builder.Default
    @Min(value = 0, message = "Page must be 0 or greater.")
    @Schema(description = "Zero-based page number", example = "0", defaultValue = "0", minimum = "0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1.")
    @Max(value = 100, message = "Page size cannot exceed 100.")
    @Schema(description = "Number of records per page", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
    private Integer size = 10;
}