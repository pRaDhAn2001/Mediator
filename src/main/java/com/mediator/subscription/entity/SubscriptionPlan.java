package com.mediator.subscription.entity;

import java.math.BigDecimal;

import com.mediator.common.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_plan_id")
    private Long subscriptionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SubscriptionPlanType planType;

    /**
     * Price in INR
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Plan validity in months
     */
    @Column(nullable = false)
    private Integer durationInMonths;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
