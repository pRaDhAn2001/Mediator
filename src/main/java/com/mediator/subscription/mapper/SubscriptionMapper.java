package com.mediator.subscription.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.dto.response.SubscriptionResponse;

@Component
public class SubscriptionMapper {

    public SubscriptionResponse toResponse(
            Subscription subscription) {

        long daysRemaining = ChronoUnit.DAYS.between(
                LocalDate.now(),
                subscription.getEndDate());

        return SubscriptionResponse.builder()

                .subscriptionId(
                        subscription.getSubscriptionId())

                .planType(
                        subscription.getSubscriptionPlan()
                                .getPlanType())

                .price(
                        subscription.getSubscriptionPlan()
                                .getPrice()
                                .doubleValue())

                .description(
                        subscription.getSubscriptionPlan()
                                .getDescription())

                .startDate(
                        subscription.getStartDate())

                .endDate(
                        subscription.getEndDate())

                .status(
                        subscription.getStatus())

                .transactionId(
                        subscription.getTransactionId())

                .daysRemaining(
                        Math.max(daysRemaining, 0))

                .build();
    }

}
