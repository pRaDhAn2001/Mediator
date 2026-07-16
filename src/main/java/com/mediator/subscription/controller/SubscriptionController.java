package com.mediator.subscription.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mediator.common.dto.ApiResponse;
import com.mediator.subscription.dto.request.CreateSubscriptionRequest;
import com.mediator.subscription.dto.response.SubscriptionPlanResponse;
import com.mediator.subscription.dto.response.SubscriptionResponse;
import com.mediator.subscription.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

        private final SubscriptionService subscriptionService;

        /**
         * Purchase Subscription
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<SubscriptionResponse> purchaseSubscription(
                        Authentication authentication,
                        @Valid @RequestBody CreateSubscriptionRequest request) {

                SubscriptionResponse response = subscriptionService.createSubscription(
                                authentication.getName(),
                                request);

                return ApiResponse.success(
                                "Subscription purchased successfully.",
                                response);
        }

        /**
         * Current Active Subscription
         */
        @GetMapping("/current")
        public ApiResponse<SubscriptionResponse> getCurrentSubscription(
                        Authentication authentication) {

                SubscriptionResponse response = subscriptionService.getCurrentSubscription(
                                authentication.getName());

                return ApiResponse.success(
                                "Current subscription fetched successfully.",
                                response);
        }

        /**
         * Subscription History
         */
        @GetMapping("/history")
        public ApiResponse<List<SubscriptionResponse>> getSubscriptionHistory(
                        Authentication authentication) {

                List<SubscriptionResponse> response = subscriptionService.getSubscriptionHistory(
                                authentication.getName());

                return ApiResponse.success(
                                "Subscription history fetched successfully.",
                                response);
        }

        /**
         * Available Plans
         */
        @GetMapping("/plans")
        public ApiResponse<List<SubscriptionPlanResponse>> getPlans() {

                return ApiResponse.success(
                                "Subscription plans fetched successfully.",
                                subscriptionService.getAvailablePlans());
        }
}
