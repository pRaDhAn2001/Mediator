package com.mediator.matching.dto.response;

import java.util.List;

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
@Schema(description = "Tutor dashboard summary of match requests")
public class TutorMatchDashboardResponse {

    @Schema(description = "Number of requests in requested status", example = "5")
    private Long requestedCount;

    @Schema(description = "Number of connected requests", example = "2")
    private Long connectedCount;

    @Schema(description = "Number of partially finalized requests", example = "1")
    private Long partiallyFinalizedCount;

    @Schema(description = "Number of finalized requests", example = "3")
    private Long finalizedCount;

    @Schema(description = "Number of cancelled requests", example = "1")
    private Long cancelledCount;

    @ArraySchema(schema = @Schema(implementation = TutorMatchRequestResponse.class))
    private List<TutorMatchRequestResponse> requests;
}