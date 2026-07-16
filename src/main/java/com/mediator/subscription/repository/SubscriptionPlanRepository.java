package com.mediator.subscription.repository;

import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    boolean existsByPlanType(SubscriptionPlanType planType);

    Optional<SubscriptionPlan> findByPlanTypeAndActiveTrue(SubscriptionPlanType planType);

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();
}
