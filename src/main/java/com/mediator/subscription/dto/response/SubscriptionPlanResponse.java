package com.mediator.subscription.dto.response;

import java.math.BigDecimal;

import com.mediator.subscription.entity.SubscriptionPlanType;

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
public class SubscriptionPlanResponse {

    private Long subscriptionPlanId;

    private SubscriptionPlanType planType;

    private BigDecimal price;

    private Integer durationInMonths;

    private String description;
}
