package com.mediator.matching.dto.response;

import java.util.List;

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
public class StudentMatchDashboardResponse {

    private Long requestedCount;

    private Long connectedCount;

    private Long partiallyFinalizedCount;

    private Long finalizedCount;

    private Long cancelledCount;

    private List<StudentMatchRequestResponse> requests;
}