package com.mediator.matching.dto.response;

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
@Schema(description = "Paginated tutor search results")
public class TutorSearchResponse {

    @ArraySchema(schema = @Schema(implementation = TutorCardResponse.class))
    private List<TutorCardResponse> tutors;

    @Schema(description = "Current zero-based page number", example = "0")
    private Integer currentPage;

    @Schema(description = "Total number of available pages", example = "5")
    private Integer totalPages;

    @Schema(description = "Total number of matching tutors", example = "42")
    private Long totalElements;

    @Schema(description = "Indicates whether another page is available", example = "true")
    private Boolean hasNext;
}