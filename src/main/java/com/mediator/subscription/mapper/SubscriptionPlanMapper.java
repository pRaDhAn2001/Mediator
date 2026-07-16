package com.mediator.subscription.mapper;

import org.springframework.stereotype.Component;

import com.mediator.subscription.dto.response.SubscriptionPlanResponse;
import com.mediator.subscription.entity.SubscriptionPlan;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {

        return SubscriptionPlanResponse.builder()
                .subscriptionPlanId(plan.getSubscriptionPlanId())
                .planType(plan.getPlanType())
                .price(plan.getPrice())
                .durationInMonths(plan.getDurationInMonths())
                .description(plan.getDescription())
                .build();
    }
}
