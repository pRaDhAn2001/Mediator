package com.mediator.subscription.dto.response;

import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long subscriptionId;
    private SubscriptionPlanType planType;
    private Double price;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private String transactionId;
    private Long daysRemaining;
}
