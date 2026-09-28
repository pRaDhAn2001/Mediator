package com.mediator.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediator.common.exception.BadRequestException;
import com.mediator.common.exception.ResourceNotFoundException;
import com.mediator.common.exception.GlobalExceptionHandler;
import com.mediator.subscription.dto.request.CreateSubscriptionRequest;
import com.mediator.subscription.dto.response.SubscriptionPlanResponse;
import com.mediator.subscription.dto.response.SubscriptionResponse;
import com.mediator.subscription.entity.SubscriptionPlanType;
import com.mediator.subscription.entity.SubscriptionStatus;
import com.mediator.subscription.service.SubscriptionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionController.class, excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.mediator.auth.config.SecurityConfig.class,
                com.mediator.auth.security.JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("SubscriptionController WebMvc Tests")
class SubscriptionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private SubscriptionService subscriptionService;

        private static final String BASE_URL = "/api/v1/subscriptions";

        private static final String TUTOR_EMAIL = "tutor@mediator.com";

        // ============================================================
        // Helper Methods
        // ============================================================

        private UsernamePasswordAuthenticationToken authPrincipal() {
                return new UsernamePasswordAuthenticationToken(
                                TUTOR_EMAIL,
                                null);
        }

        private SubscriptionResponse buildSampleSubscriptionResponse() {

                return SubscriptionResponse.builder()
                                .subscriptionId(101L)
                                .planType(SubscriptionPlanType.PRO)
                                .price(999.0)
                                .description("Pro Plan - 3 Months")
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(3))
                                .status(SubscriptionStatus.ACTIVE)
                                .transactionId("TXN-20260925-ABC123")
                                .daysRemaining(90L)
                                .build();
        }

        // ============================================================
        // PURCHASE SUBSCRIPTION
        // ============================================================

        @Nested
        @DisplayName("POST /api/v1/subscriptions")
        class PurchaseSubscriptionTests {

                @Test
                @DisplayName("Should successfully purchase subscription with HTTP 201")
                void purchaseSubscription_Success() throws Exception {

                        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                                        .planType(SubscriptionPlanType.PRO)
                                        .build();

                        SubscriptionResponse response = buildSampleSubscriptionResponse();

                        when(subscriptionService.createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class)))
                                        .thenReturn(response);

                        mockMvc.perform(
                                        post(BASE_URL)
                                                        .principal(authPrincipal())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription purchased successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.subscriptionId").value(101))
                                        .andExpect(jsonPath("$.data.planType").value("PRO"))
                                        .andExpect(jsonPath("$.data.price").value(999.0))
                                        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                                        .andExpect(jsonPath("$.data.transactionId")
                                                        .value("TXN-20260925-ABC123"))
                                        .andExpect(jsonPath("$.timestamp").exists());

                        verify(subscriptionService).createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class));

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 400 when tutor profile is not verified")
                void purchaseSubscription_UnverifiedTutor_BadRequest()
                                throws Exception {

                        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                                        .planType(SubscriptionPlanType.PRO)
                                        .build();

                        when(subscriptionService.createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class)))
                                        .thenThrow(
                                                        new BadRequestException(
                                                                        "Your profile must be verified before purchasing a subscription."));

                        mockMvc.perform(
                                        post(BASE_URL)
                                                        .principal(authPrincipal())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Your profile must be verified before purchasing a subscription."));

                        verify(subscriptionService).createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class));

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 400 when active subscription already exists")
                void purchaseSubscription_AlreadyActiveSubscription_BadRequest()
                                throws Exception {

                        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                                        .planType(SubscriptionPlanType.PRO)
                                        .build();

                        when(subscriptionService.createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class)))
                                        .thenThrow(
                                                        new BadRequestException(
                                                                        "An active subscription already exists."));

                        mockMvc.perform(
                                        post(BASE_URL)
                                                        .principal(authPrincipal())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("An active subscription already exists."));

                        verify(subscriptionService).createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class));

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 404 when subscription plan is not found")
                void purchaseSubscription_PlanNotFound_NotFound()
                                throws Exception {

                        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                                        .planType(SubscriptionPlanType.ULTRA)
                                        .build();

                        when(subscriptionService.createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class)))
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "Subscription plan not found."));

                        mockMvc.perform(
                                        post(BASE_URL)
                                                        .principal(authPrincipal())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription plan not found."));

                        verify(subscriptionService).createSubscription(
                                        eq(TUTOR_EMAIL),
                                        any(CreateSubscriptionRequest.class));

                        verifyNoMoreInteractions(subscriptionService);
                }

                /*
                 * Add a validation test here ONLY if CreateSubscriptionRequest
                 * contains validation annotations such as @NotNull.
                 *
                 * Example:
                 *
                 * @Test
                 * void purchaseSubscription_InvalidRequest_BadRequest()
                 * throws Exception {
                 *
                 * CreateSubscriptionRequest request =
                 * CreateSubscriptionRequest.builder()
                 * .planType(null)
                 * .build();
                 *
                 * mockMvc.perform(
                 * post(BASE_URL)
                 * .principal(authPrincipal())
                 * .contentType(MediaType.APPLICATION_JSON)
                 * .content(objectMapper.writeValueAsString(request))
                 * )
                 * .andExpect(status().isBadRequest());
                 *
                 * verifyNoInteractions(subscriptionService);
                 * }
                 *
                 * Do not add this blindly unless planType is actually annotated
                 * with @NotNull in CreateSubscriptionRequest.
                 */
        }

        // ============================================================
        // CURRENT SUBSCRIPTION
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/subscriptions/current")
        class GetCurrentSubscriptionTests {

                @Test
                @DisplayName("Should return HTTP 200 with current active subscription")
                void getCurrentSubscription_Success() throws Exception {

                        SubscriptionResponse response = buildSampleSubscriptionResponse();

                        when(subscriptionService.getCurrentSubscription(TUTOR_EMAIL))
                                        .thenReturn(response);

                        mockMvc.perform(
                                        get(BASE_URL + "/current")
                                                        .principal(authPrincipal()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Current subscription fetched successfully."))
                                        .andExpect(jsonPath("$.data").exists())
                                        .andExpect(jsonPath("$.data.subscriptionId").value(101))
                                        .andExpect(jsonPath("$.data.planType").value("PRO"))
                                        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                                        .andExpect(jsonPath("$.data.daysRemaining").value(90))
                                        .andExpect(jsonPath("$.timestamp").exists());

                        verify(subscriptionService)
                                        .getCurrentSubscription(TUTOR_EMAIL);

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 404 when no active subscription exists")
                void getCurrentSubscription_NotFound() throws Exception {

                        when(subscriptionService.getCurrentSubscription(TUTOR_EMAIL))
                                        .thenThrow(
                                                        new ResourceNotFoundException(
                                                                        "No active subscription found."));

                        mockMvc.perform(
                                        get(BASE_URL + "/current")
                                                        .principal(authPrincipal()))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.success").value(false))
                                        .andExpect(jsonPath("$.message")
                                                        .value("No active subscription found."));

                        verify(subscriptionService)
                                        .getCurrentSubscription(TUTOR_EMAIL);

                        verifyNoMoreInteractions(subscriptionService);
                }
        }

        // ============================================================
        // SUBSCRIPTION HISTORY
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/subscriptions/history")
        class GetSubscriptionHistoryTests {

                @Test
                @DisplayName("Should return HTTP 200 with subscription history")
                void getSubscriptionHistory_Success() throws Exception {

                        SubscriptionResponse activeSubscription = buildSampleSubscriptionResponse();

                        SubscriptionResponse expiredSubscription = SubscriptionResponse.builder()
                                        .subscriptionId(100L)
                                        .planType(SubscriptionPlanType.BASIC)
                                        .price(499.0)
                                        .description("Basic Plan")
                                        .startDate(LocalDate.now().minusMonths(4))
                                        .endDate(LocalDate.now().minusMonths(1))
                                        .status(SubscriptionStatus.EXPIRED)
                                        .transactionId("TXN-20260525-OLD001")
                                        .daysRemaining(0L)
                                        .build();

                        when(subscriptionService.getSubscriptionHistory(TUTOR_EMAIL))
                                        .thenReturn(
                                                        List.of(
                                                                        activeSubscription,
                                                                        expiredSubscription));

                        mockMvc.perform(
                                        get(BASE_URL + "/history")
                                                        .principal(authPrincipal()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription history fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(2))

                                        .andExpect(jsonPath("$.data[0].subscriptionId")
                                                        .value(101))
                                        .andExpect(jsonPath("$.data[0].planType")
                                                        .value("PRO"))
                                        .andExpect(jsonPath("$.data[0].status")
                                                        .value("ACTIVE"))

                                        .andExpect(jsonPath("$.data[1].subscriptionId")
                                                        .value(100))
                                        .andExpect(jsonPath("$.data[1].planType")
                                                        .value("BASIC"))
                                        .andExpect(jsonPath("$.data[1].status")
                                                        .value("EXPIRED"));

                        verify(subscriptionService)
                                        .getSubscriptionHistory(TUTOR_EMAIL);

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 200 with empty list when no history exists")
                void getSubscriptionHistory_Empty() throws Exception {

                        when(subscriptionService.getSubscriptionHistory(TUTOR_EMAIL))
                                        .thenReturn(List.of());

                        mockMvc.perform(
                                        get(BASE_URL + "/history")
                                                        .principal(authPrincipal()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription history fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(0));

                        verify(subscriptionService)
                                        .getSubscriptionHistory(TUTOR_EMAIL);

                        verifyNoMoreInteractions(subscriptionService);
                }
        }

        // ============================================================
        // AVAILABLE PLANS
        // ============================================================

        @Nested
        @DisplayName("GET /api/v1/subscriptions/plans")
        class GetPlansTests {

                @Test
                @DisplayName("Should return HTTP 200 with available subscription plans")
                void getPlans_Success() throws Exception {

                        SubscriptionPlanResponse plan1 = SubscriptionPlanResponse.builder()
                                        .subscriptionPlanId(1L)
                                        .planType(SubscriptionPlanType.BASIC)
                                        .price(BigDecimal.valueOf(499.00))
                                        .durationInMonths(1)
                                        .description("Basic 1 month plan")
                                        .build();

                        SubscriptionPlanResponse plan2 = SubscriptionPlanResponse.builder()
                                        .subscriptionPlanId(2L)
                                        .planType(SubscriptionPlanType.PRO)
                                        .price(BigDecimal.valueOf(1299.00))
                                        .durationInMonths(3)
                                        .description("Pro 3 months plan")
                                        .build();

                        when(subscriptionService.getAvailablePlans())
                                        .thenReturn(List.of(plan1, plan2));

                        mockMvc.perform(
                                        get(BASE_URL + "/plans"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription plans fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(2))

                                        .andExpect(jsonPath("$.data[0].subscriptionPlanId")
                                                        .value(1))
                                        .andExpect(jsonPath("$.data[0].planType")
                                                        .value("BASIC"))
                                        .andExpect(jsonPath("$.data[0].price")
                                                        .value(499.00))
                                        .andExpect(jsonPath("$.data[0].durationInMonths")
                                                        .value(1))

                                        .andExpect(jsonPath("$.data[1].subscriptionPlanId")
                                                        .value(2))
                                        .andExpect(jsonPath("$.data[1].planType")
                                                        .value("PRO"))
                                        .andExpect(jsonPath("$.data[1].price")
                                                        .value(1299.00))
                                        .andExpect(jsonPath("$.data[1].durationInMonths")
                                                        .value(3));

                        verify(subscriptionService).getAvailablePlans();

                        verifyNoMoreInteractions(subscriptionService);
                }

                @Test
                @DisplayName("Should return HTTP 200 with empty list when no plans are available")
                void getPlans_Empty() throws Exception {

                        when(subscriptionService.getAvailablePlans())
                                        .thenReturn(List.of());

                        mockMvc.perform(
                                        get(BASE_URL + "/plans"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Subscription plans fetched successfully."))
                                        .andExpect(jsonPath("$.data").isArray())
                                        .andExpect(jsonPath("$.data.length()").value(0));

                        verify(subscriptionService).getAvailablePlans();

                        verifyNoMoreInteractions(subscriptionService);
                }
        }
}