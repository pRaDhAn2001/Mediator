package com.mediator.subscription.repository;

import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByTutor_TutorId(Long tutorId);

    Optional<Subscription> findByTutor_TutorIdAndStatus(
            Long tutorId,
            SubscriptionStatus status);

    Optional<Subscription> findTopByTutor_TutorIdOrderByEndDateDesc(Long tutorId);
}
