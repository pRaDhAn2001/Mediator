package com.mediator.matching.dto.response;

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
public class TutorMatchDashboardResponse {

    private Long requestedCount;

    private Long connectedCount;

    private Long partiallyFinalizedCount;

    private Long finalizedCount;

    private Long cancelledCount;

    private List<TutorMatchRequestResponse> requests;
}
