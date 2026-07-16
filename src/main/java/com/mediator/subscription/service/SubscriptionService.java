package com.mediator.subscription.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.common.exception.BadRequestException;
import com.mediator.subscription.dto.request.CreateSubscriptionRequest;
import com.mediator.subscription.dto.response.SubscriptionPlanResponse;
import com.mediator.subscription.dto.response.SubscriptionResponse;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.mapper.SubscriptionMapper;
import com.mediator.subscription.mapper.SubscriptionPlanMapper;
import com.mediator.subscription.repository.SubscriptionPlanRepository;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAvailablePlans() {

        return subscriptionPlanRepository.findAll()
                .stream()
                .filter(SubscriptionPlan::getActive)
                .map(subscriptionPlanMapper::toResponse)
                .toList();
    }

    /**
     * Generates internal transaction id.
     *
     * Example:
     * TXN-20260712-A83KQ9
     */
    private String generateTransactionId() {

        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return "TXN-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + random;
    }

    private LocalDate calculateEndDate(
            SubscriptionPlan plan) {

        return LocalDate.now()
                .plusMonths(plan.getDurationInMonths());
    }

    public SubscriptionResponse createSubscription(
            String email,
            CreateSubscriptionRequest request) {

        Tutor tutor = getTutorByEmail(email);

        if (tutor.getVerificationStatus() != VerificationStatus.APPROVED) {

            throw new BadRequestException(
                    "Your profile must be verified before purchasing a subscription.");
        }

        subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        SubscriptionStatus.ACTIVE)
                .ifPresent(subscription -> {

                    throw new BadRequestException(
                            "An active subscription already exists.");
                });

        SubscriptionPlan plan = subscriptionPlanRepository
                .findByPlanTypeAndActiveTrue(
                        request.getPlanType())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription plan not found."));

        Subscription subscription = Subscription.builder()

                .tutor(tutor)

                .subscriptionPlan(plan)

                .startDate(LocalDate.now())

                .endDate(calculateEndDate(plan))

                .transactionId(generateTransactionId())

                .status(SubscriptionStatus.ACTIVE)

                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(
            String email) {

        Tutor tutor = getTutorByEmail(email);

        Subscription subscription = subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        tutor.getTutorId(),
                        SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription found."));

        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionHistory(
            String email) {

        Tutor tutor = getTutorByEmail(email);

        return subscriptionRepository
                .findAllByTutor_TutorIdOrderByEndDateDesc(
                        tutor.getTutorId())
                .stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    private Tutor getTutorByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return tutorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));
    }

}
