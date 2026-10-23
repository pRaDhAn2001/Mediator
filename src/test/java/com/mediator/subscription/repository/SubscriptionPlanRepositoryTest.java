package com.mediator.subscription.repository;

import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("SubscriptionPlanRepository Data JPA Tests")
class SubscriptionPlanRepositoryTest {

    @Autowired
    private SubscriptionPlanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private SubscriptionPlan createPlan(
            SubscriptionPlanType type,
            boolean active) {

        return SubscriptionPlan.builder()
                .planType(type)
                .durationInMonths(1)
                .price(BigDecimal.valueOf(499))
                .active(active)
                .build();
    }

    @Test
    @DisplayName("Should find active plan by plan type")
    void findByPlanTypeAndActiveTrue_success() {

        SubscriptionPlan plan = createPlan(
                SubscriptionPlanType.BASIC,
                true);

        entityManager.persistAndFlush(plan);

        Optional<SubscriptionPlan> result = repository.findByPlanTypeAndActiveTrue(
                SubscriptionPlanType.BASIC);

        assertThat(result).isPresent();
        assertThat(result.get().getPlanType())
                .isEqualTo(SubscriptionPlanType.BASIC);
        assertThat(result.get().getActive())
                .isTrue();
    }

    @Test
    @DisplayName("Should not return inactive plan")
    void findByPlanTypeAndActiveTrue_inactive_returnsEmpty() {

        SubscriptionPlan plan = createPlan(
                SubscriptionPlanType.PRO,
                false);

        entityManager.persistAndFlush(plan);

        Optional<SubscriptionPlan> result = repository.findByPlanTypeAndActiveTrue(
                SubscriptionPlanType.PRO);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when requested plan type does not exist")
    void findByPlanTypeAndActiveTrue_notFound() {

        Optional<SubscriptionPlan> result = repository.findByPlanTypeAndActiveTrue(
                SubscriptionPlanType.ULTRA);

        assertThat(result).isEmpty();
    }
}