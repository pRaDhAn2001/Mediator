package com.mediator.tutor.dto.dashboard;

import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.VerificationStatus;

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
public class TutorDashboardResponse {

    private TutorProfileResponse profile;

    private Long totalMatchRequests;

    private Long pendingMatchRequests;

    private Long activeStudents;

    private SubscriptionStatus subscriptionStatus;

    private SubscriptionPlanType currentPlan;

    private VerificationStatus verificationStatus;
}
