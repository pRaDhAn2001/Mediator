package com.mediator.subscription.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.tutor.entity.Tutor;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(length = 100)
    private String paymentOrderId; // Razorpay Order Id (Future)

    @Column(length = 100)
    private String paymentId; // Razorpay Payment Id (Future)

    @Column(length = 255)
    private String paymentSignature;// Future verification

    @Column(length = 50)
    @Builder.Default
    private String paymentMode = "MANUAL";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;
}
