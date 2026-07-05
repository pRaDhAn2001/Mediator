package com.mediator.config.seed;

import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Order(4)
public class SubscriptionPlanSeeder implements CommandLineRunner {

        private final SubscriptionPlanRepository subscriptionPlanRepository;

        @Override
        public void run(String... args) {

                createPlan(
                                SubscriptionPlanType.BASIC,
                                new BigDecimal("299"),
                                1,
                                "Basic Plan - Valid for 1 Month");

                createPlan(
                                SubscriptionPlanType.PRO,
                                new BigDecimal("1599"),
                                6,
                                "Pro Plan - Valid for 6 Months");

                createPlan(
                                SubscriptionPlanType.ULTRA,
                                new BigDecimal("2999"),
                                12,
                                "Ultra Plan - Valid for 12 Months");
        }

        private void createPlan(SubscriptionPlanType planType,
                        BigDecimal price,
                        Integer durationInMonths,
                        String description) {

                if (!subscriptionPlanRepository.existsByPlanType(planType)) {

                        subscriptionPlanRepository.save(
                                        SubscriptionPlan.builder()
                                                        .planType(planType)
                                                        .price(price)
                                                        .durationInMonths(durationInMonths)
                                                        .description(description)
                                                        .active(true)
                                                        .build());
                }
        }
}