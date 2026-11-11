package com.mediator.tutor.dto.request;

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
@Schema(description = "Filters used when searching for tutors.")
public class TutorSearchRequest {

    @ArraySchema(arraySchema = @Schema(description = "Subject IDs to match against tutors."), schema = @Schema(type = "integer", format = "int64", example = "1"))
    private List<Long> subjectIds;

    @Schema(description = "Class level ID.", example = "3")
    private Long classLevelId;

    @Schema(description = "Education board ID.", example = "1")
    private Long boardId;

    @Schema(description = "Preferred teaching mode filter.")
    private PreferredMode preferredMode;

    @Schema(description = "Maximum search radius in the application's configured unit.", example = "10.0", minimum = "0")
    private Double radius;

    @Schema(description = "Maximum tutor salary per month.", example = "10000.0", minimum = "0")
    private Double maxSalary;

    @Schema(description = "Whether to return only verified tutors.", example = "true")
    private Boolean verifiedOnly;

    @Schema(description = "Zero-based page number, if supported by the search implementation.", example = "0", minimum = "0")
    private Integer page;

    @Schema(description = "Number of records per page, if supported by the search implementation.", example = "10", minimum = "1")
    private Integer size;
}