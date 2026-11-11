package com.mediator.tutor.dto.dashboard;

import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.VerificationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Summary information displayed on the tutor dashboard.")
public class TutorDashboardResponse {

    @Schema(description = "Tutor profile summary.")
    private TutorProfileResponse profile;

    @Schema(description = "Total number of match requests received.", example = "15")
    private Long totalMatchRequests;

    @Schema(description = "Number of pending match requests.", example = "4")
    private Long pendingMatchRequests;

    @Schema(description = "Number of active students.", example = "8")
    private Long activeStudents;

    @Schema(description = "Current subscription status.", example = "ACTIVE")
    private SubscriptionStatus subscriptionStatus;

    @Schema(description = "Current subscription plan.", example = "PRO")
    private SubscriptionPlanType currentPlan;

    @Schema(description = "Tutor's document verification status.", example = "APPROVED")
    private VerificationStatus verificationStatus;
}