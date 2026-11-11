package com.mediator.subscription.dto.request;

import com.mediator.subscription.entity.SubscriptionPlanType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for purchasing a subscription")
public class CreateSubscriptionRequest {

    @Schema(description = "Type of subscription plan to purchase", example = "PRO", allowableValues = { "BASIC", "PRO",
            "ULTRA" })
    private SubscriptionPlanType planType;
}