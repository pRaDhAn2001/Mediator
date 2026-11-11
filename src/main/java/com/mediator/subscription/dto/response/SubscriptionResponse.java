package com.mediator.subscription.dto.response;

import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subscription details returned after purchase or when viewing subscription information")
public class SubscriptionResponse {

    @Schema(description = "Unique subscription ID", example = "101")
    private Long subscriptionId;

    @Schema(description = "Purchased subscription plan", example = "PRO", allowableValues = { "BASIC", "PRO", "ULTRA" })
    private SubscriptionPlanType planType;

    @Schema(description = "Subscription price in INR", example = "1599.00")
    private Double price;

    @Schema(description = "Description of the purchased subscription", example = "Six-month subscription with access to premium features.")
    private String description;

    @Schema(description = "Subscription start date", example = "2026-11-11")
    private LocalDate startDate;

    @Schema(description = "Subscription expiry date", example = "2027-05-11")
    private LocalDate endDate;

    @Schema(description = "Current subscription status", example = "ACTIVE")
    private SubscriptionStatus status;

    @Schema(description = "Transaction reference ID associated with the subscription", example = "TXN202611110001")
    private String transactionId;

    @Schema(description = "Number of days remaining until subscription expiry", example = "180")
    private Long daysRemaining;
}