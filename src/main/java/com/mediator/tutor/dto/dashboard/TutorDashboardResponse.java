package com.mediator.tutor.dto.dashboard;

import com.mediator.tutor.dto.response.TutorProfileResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDashboardResponse {
    private TutorProfileResponse profile;
    private Long activeSubscriptionsCount;
    private Long pendingRequestsCount;
    private Long approvedRequestsCount;
}
