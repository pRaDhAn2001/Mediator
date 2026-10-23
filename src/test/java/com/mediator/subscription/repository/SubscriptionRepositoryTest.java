package com.mediator.subscription.repository;

import com.mediator.auth.entity.Role;
import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.repository.TutorRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    private int tutorCounter = 0;
    private int planCounter = 0;
    private int transactionCounter = 0;

    private Tutor createTutor() {
        tutorCounter++;
        String suffix = String.valueOf(tutorCounter);

        User user = User.builder()
                .email("tutor" + suffix + "@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("Tutor")
                .mobileNumber(
                        "9" + String.format("%09d", tutorCounter))
                .role(Role.ROLE_TUTOR)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        Tutor tutor = Tutor.builder()
                .user(savedUser)
                .build();

        return tutorRepository.saveAndFlush(tutor);
    }

    private SubscriptionPlan createPlan() {
        planCounter++;

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planType(SubscriptionPlanType.BASIC)
                .durationInMonths(1)
                .price(new BigDecimal("299.00"))
                .active(true)
                .build();

        return subscriptionPlanRepository.saveAndFlush(plan);
    }

    private Subscription createSubscription(
            Tutor tutor,
            SubscriptionPlan plan,
            SubscriptionStatus status,
            LocalDate startDate,
            LocalDate endDate) {
        transactionCounter++;

        Subscription subscription = Subscription.builder()
                .tutor(tutor)
                .subscriptionPlan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .transactionId("TEST-TXN-" + transactionCounter)
                .paymentMode("MANUAL")
                .status(status)
                .build();

        return subscriptionRepository.saveAndFlush(subscription);
    }

    @Test
    void findByTutorTutorId_shouldReturnAllTutorSubscriptions() {
        Tutor tutor = createTutor();
        SubscriptionPlan plan = createPlan();

        createSubscription(
                tutor, plan, SubscriptionStatus.ACTIVE,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        createSubscription(
                tutor, plan, SubscriptionStatus.EXPIRED,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 1));

        assertThat(
                subscriptionRepository.findByTutor_TutorId(
                        tutor.getTutorId()))
                .hasSize(2);
    }

    @Test
    void findByTutorTutorIdAndStatus_shouldReturnMatchingSubscription() {
        Tutor tutor = createTutor();
        SubscriptionPlan plan = createPlan();

        createSubscription(
                tutor, plan, SubscriptionStatus.EXPIRED,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        Subscription active = createSubscription(
                tutor, plan, SubscriptionStatus.ACTIVE,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 1));

        Optional<Subscription> result = subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        SubscriptionStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getSubscriptionId())
                .isEqualTo(active.getSubscriptionId());
    }

    @Test
    void findByTutorTutorIdAndStatus_shouldReturnEmptyWhenNoMatchExists() {
        Tutor tutor = createTutor();
        SubscriptionPlan plan = createPlan();

        createSubscription(
                tutor, plan, SubscriptionStatus.EXPIRED,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        assertThat(
                subscriptionRepository.findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        SubscriptionStatus.ACTIVE))
                .isEmpty();
    }

    @Test
    void findAllByTutorTutorIdOrderByEndDateDesc_shouldReturnDescendingHistory() {
        Tutor tutor = createTutor();
        SubscriptionPlan plan = createPlan();

        Subscription older = createSubscription(
                tutor, plan, SubscriptionStatus.EXPIRED,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        Subscription newer = createSubscription(
                tutor, plan, SubscriptionStatus.ACTIVE,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 1));

        Subscription newest = createSubscription(
                tutor, plan, SubscriptionStatus.EXPIRED,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 4, 1));

        List<Subscription> results = subscriptionRepository
                .findAllByTutor_TutorIdOrderByEndDateDesc(
                        tutor.getTutorId());

        assertThat(results)
                .extracting(Subscription::getSubscriptionId)
                .containsExactly(
                        newest.getSubscriptionId(),
                        newer.getSubscriptionId(),
                        older.getSubscriptionId());
    }

    @Test
    void findAllByTutorTutorIdOrderByEndDateDesc_shouldReturnEmptyForTutorWithoutSubscriptions() {
        Tutor tutor = createTutor();

        assertThat(
                subscriptionRepository
                        .findAllByTutor_TutorIdOrderByEndDateDesc(
                                tutor.getTutorId()))
                .isEmpty();
    }
}
