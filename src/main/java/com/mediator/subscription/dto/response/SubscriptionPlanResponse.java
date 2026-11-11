package com.mediator.subscription.dto.response;

import java.math.BigDecimal;

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
@Schema(description = "Details of an available subscription plan")
public class SubscriptionPlanResponse {

    @Schema(description = "Unique subscription plan ID", example = "1")
    private Long subscriptionPlanId;

    @Schema(description = "Subscription plan type", example = "PRO", allowableValues = { "BASIC", "PRO", "ULTRA" })
    private SubscriptionPlanType planType;

    @Schema(description = "Subscription plan price in INR", example = "1599.00")
    private BigDecimal price;

    @Schema(description = "Subscription duration in months", example = "6")
    private Integer durationInMonths;

    @Schema(description = "Description of the subscription plan", example = "Six-month subscription with access to premium features.")
    private String description;
}