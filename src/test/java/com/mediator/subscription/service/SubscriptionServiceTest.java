package com.mediator.subscription.service;

import com.mediator.auth.entity.User;
import com.mediator.auth.repository.UserRepository;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.subscription.dto.request.CreateSubscriptionRequest;
import com.mediator.subscription.dto.response.SubscriptionPlanResponse;
import com.mediator.subscription.dto.response.SubscriptionResponse;
import com.mediator.subscription.entity.Subscription;
import com.mediator.subscription.entity.SubscriptionPlan;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.mapper.SubscriptionMapper;
import com.mediator.subscription.mapper.SubscriptionPlanMapper;
import com.mediator.subscription.repository.SubscriptionPlanRepository;
import com.mediator.subscription.repository.SubscriptionRepository;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.VerificationStatus;
import com.mediator.tutor.repository.TutorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private SubscriptionMapper subscriptionMapper = new SubscriptionMapper();

    @Spy
    private SubscriptionPlanMapper subscriptionPlanMapper = new SubscriptionPlanMapper();

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User tutorUser;

    private Tutor tutor;

    private SubscriptionPlan basicPlan;

    @BeforeEach
    void setUp() {

        tutorUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Tutor")
                .email("tutor@test.com")
                .build();

        tutor = Tutor.builder()
                .tutorId(10L)
                .user(tutorUser)
                .verificationStatus(
                        VerificationStatus.APPROVED)
                .build();

        basicPlan = SubscriptionPlan.builder()
                .subscriptionPlanId(1L)
                .planType(SubscriptionPlanType.BASIC)
                .durationInMonths(1)
                .price(BigDecimal.valueOf(499))
                .active(true)
                .build();
    }

    // =========================================================
    // GET AVAILABLE PLANS
    // =========================================================

    @Test
    void getAvailablePlans_success() {

        when(subscriptionPlanRepository.findAll())
                .thenReturn(List.of(basicPlan));

        List<SubscriptionPlanResponse> plans = subscriptionService.getAvailablePlans();

        assertThat(plans)
                .isNotNull()
                .hasSize(1);

        assertThat(plans.get(0).getPlanType())
                .isEqualTo(SubscriptionPlanType.BASIC);
    }

    @Test
    void getAvailablePlans_returnsEmptyList_whenNoPlansExist() {

        when(subscriptionPlanRepository.findAll())
                .thenReturn(List.of());

        List<SubscriptionPlanResponse> plans = subscriptionService.getAvailablePlans();

        assertThat(plans)
                .isNotNull()
                .isEmpty();
    }

    // =========================================================
    // CREATE SUBSCRIPTION - SUCCESS
    // =========================================================

    @Test
    void createSubscription_success() {

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.BASIC)
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        10L,
                        SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(subscriptionPlanRepository
                .findByPlanTypeAndActiveTrue(
                        SubscriptionPlanType.BASIC))
                .thenReturn(Optional.of(basicPlan));

        when(subscriptionRepository.save(
                any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionResponse response = subscriptionService.createSubscription(
                "tutor@test.com",
                request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getPlanType())
                .isEqualTo(SubscriptionPlanType.BASIC);

        assertThat(response.getStatus())
                .isEqualTo(SubscriptionStatus.ACTIVE);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(
                Subscription.class);

        verify(subscriptionRepository)
                .save(captor.capture());

        Subscription savedSubscription = captor.getValue();

        assertThat(savedSubscription.getTutor())
                .isEqualTo(tutor);

        assertThat(savedSubscription.getSubscriptionPlan())
                .isEqualTo(basicPlan);

        assertThat(savedSubscription.getStatus())
                .isEqualTo(SubscriptionStatus.ACTIVE);

        assertThat(savedSubscription.getTransactionId())
                .isNotNull()
                .isNotBlank();

        assertThat(savedSubscription.getStartDate())
                .isNotNull();

        assertThat(savedSubscription.getEndDate())
                .isNotNull();

        assertThat(savedSubscription.getEndDate())
                .isEqualTo(
                        savedSubscription
                                .getStartDate()
                                .plusMonths(
                                        basicPlan
                                                .getDurationInMonths()));
    }

    // =========================================================
    // CREATE SUBSCRIPTION - USER NOT FOUND
    // =========================================================

    @Test
    void createSubscription_throwsException_whenUserNotFound() {

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.BASIC)
                .build();

        when(userRepository.findByEmail(
                "unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                "unknown@test.com",
                request))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "User not found");

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    // =========================================================
    // CREATE SUBSCRIPTION - TUTOR PROFILE NOT FOUND
    // =========================================================

    @Test
    void createSubscription_throwsException_whenTutorProfileNotFound() {

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.BASIC)
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                "tutor@test.com",
                request))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Tutor profile not found");

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    // =========================================================
    // CREATE SUBSCRIPTION - TUTOR NOT VERIFIED
    // =========================================================

    @Test
    void createSubscription_throwsException_whenTutorUnapproved() {

        Tutor unapprovedTutor = Tutor.builder()
                .tutorId(10L)
                .user(tutorUser)
                .verificationStatus(
                        VerificationStatus.PENDING)
                .build();

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.BASIC)
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(
                        Optional.of(unapprovedTutor));

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                "tutor@test.com",
                request))
                .isInstanceOf(
                        BadRequestException.class)
                .hasMessageContaining(
                        "verified before purchasing a subscription");

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    // =========================================================
    // CREATE SUBSCRIPTION - ACTIVE SUBSCRIPTION EXISTS
    // =========================================================

    @Test
    void createSubscription_throwsException_whenActiveSubscriptionAlreadyExists() {

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.BASIC)
                .build();

        Subscription activeSubscription = Subscription.builder()
                .subscriptionId(100L)
                .tutor(tutor)
                .subscriptionPlan(basicPlan)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        10L,
                        SubscriptionStatus.ACTIVE))
                .thenReturn(
                        Optional.of(activeSubscription));

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                "tutor@test.com",
                request))
                .isInstanceOf(
                        BadRequestException.class)
                .hasMessageContaining(
                        "An active subscription already exists");

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    // =========================================================
    // CREATE SUBSCRIPTION - PLAN NOT FOUND
    // =========================================================

    @Test
    void createSubscription_throwsException_whenPlanTypeNotFound() {

        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .planType(
                        SubscriptionPlanType.PRO)
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        10L,
                        SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(subscriptionPlanRepository
                .findByPlanTypeAndActiveTrue(
                        SubscriptionPlanType.PRO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(
                "tutor@test.com",
                request))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Subscription plan not found");

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    // =========================================================
    // GET CURRENT SUBSCRIPTION - SUCCESS
    // =========================================================

    @Test
    void getCurrentSubscription_success() {

        LocalDate startDate = LocalDate.now();

        LocalDate endDate = startDate.plusMonths(1);

        Subscription activeSubscription = Subscription.builder()
                .subscriptionId(100L)
                .tutor(tutor)
                .subscriptionPlan(basicPlan)
                .startDate(startDate)
                .endDate(endDate)
                .status(
                        SubscriptionStatus.ACTIVE)
                .transactionId(
                        "TXN-123456")
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        10L,
                        SubscriptionStatus.ACTIVE))
                .thenReturn(
                        Optional.of(activeSubscription));

        SubscriptionResponse response = subscriptionService
                .getCurrentSubscription(
                        "tutor@test.com");

        assertThat(response)
                .isNotNull();

        assertThat(response.getStatus())
                .isEqualTo(
                        SubscriptionStatus.ACTIVE);

        assertThat(response.getPlanType())
                .isEqualTo(
                        SubscriptionPlanType.BASIC);
    }

    // =========================================================
    // GET CURRENT SUBSCRIPTION - NOT FOUND
    // =========================================================

    @Test
    void getCurrentSubscription_throwsNotFound_whenNoActiveSubscription() {

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findByTutor_TutorIdAndStatus(
                        10L,
                        SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService
                .getCurrentSubscription(
                        "tutor@test.com"))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "No active subscription found");
    }

    // =========================================================
    // GET CURRENT SUBSCRIPTION - USER NOT FOUND
    // =========================================================

    @Test
    void getCurrentSubscription_throwsException_whenUserNotFound() {

        when(userRepository.findByEmail(
                "unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService
                .getCurrentSubscription(
                        "unknown@test.com"))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "User not found");
    }

    // =========================================================
    // GET CURRENT SUBSCRIPTION - TUTOR NOT FOUND
    // =========================================================

    @Test
    void getCurrentSubscription_throwsException_whenTutorProfileNotFound() {

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService
                .getCurrentSubscription(
                        "tutor@test.com"))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Tutor profile not found");
    }

    // =========================================================
    // GET SUBSCRIPTION HISTORY - SUCCESS
    // =========================================================

    @Test
    void getSubscriptionHistory_success() {

        Subscription expiredSubscription = Subscription.builder()
                .subscriptionId(100L)
                .tutor(tutor)
                .subscriptionPlan(basicPlan)
                .startDate(
                        LocalDate.now()
                                .minusMonths(2))
                .endDate(
                        LocalDate.now()
                                .minusMonths(1))
                .status(
                        SubscriptionStatus.EXPIRED)
                .transactionId(
                        "TXN-123456")
                .build();

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findAllByTutor_TutorIdOrderByEndDateDesc(
                        10L))
                .thenReturn(
                        List.of(expiredSubscription));

        List<SubscriptionResponse> history = subscriptionService
                .getSubscriptionHistory(
                        "tutor@test.com");

        assertThat(history)
                .isNotNull()
                .hasSize(1);

        assertThat(history.get(0).getStatus())
                .isEqualTo(
                        SubscriptionStatus.EXPIRED);

        assertThat(history.get(0).getPlanType())
                .isEqualTo(
                        SubscriptionPlanType.BASIC);
    }

    // =========================================================
    // GET SUBSCRIPTION HISTORY - EMPTY
    // =========================================================

    @Test
    void getSubscriptionHistory_returnsEmptyList_whenNoHistoryExists() {

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.of(tutor));

        when(subscriptionRepository
                .findAllByTutor_TutorIdOrderByEndDateDesc(
                        10L))
                .thenReturn(List.of());

        List<SubscriptionResponse> history = subscriptionService
                .getSubscriptionHistory(
                        "tutor@test.com");

        assertThat(history)
                .isNotNull()
                .isEmpty();
    }

    // =========================================================
    // GET SUBSCRIPTION HISTORY - USER NOT FOUND
    // =========================================================

    @Test
    void getSubscriptionHistory_throwsException_whenUserNotFound() {

        when(userRepository.findByEmail(
                "unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService
                .getSubscriptionHistory(
                        "unknown@test.com"))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "User not found");
    }

    // =========================================================
    // GET SUBSCRIPTION HISTORY - TUTOR NOT FOUND
    // =========================================================

    @Test
    void getSubscriptionHistory_throwsException_whenTutorProfileNotFound() {

        when(userRepository.findByEmail(
                "tutor@test.com"))
                .thenReturn(Optional.of(tutorUser));

        when(tutorRepository.findByUser(tutorUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService
                .getSubscriptionHistory(
                        "tutor@test.com"))
                .isInstanceOf(
                        ResourceNotFoundException.class)
                .hasMessageContaining(
                        "Tutor profile not found");
    }
}