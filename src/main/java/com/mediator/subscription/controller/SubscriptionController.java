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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "APIs for purchasing subscriptions, viewing subscription details and checking available plans")
@SecurityRequirement(name = "Bearer Authentication")
public class SubscriptionController {

        private final SubscriptionService subscriptionService;

        /**
         * Purchase Subscription
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Purchase a subscription", description = "Allows the authenticated user to purchase a subscription by selecting an available plan.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subscription purchased successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid subscription request or plan"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subscription plan not found")
        })
        public ApiResponse<SubscriptionResponse> purchaseSubscription(
                        @Parameter(hidden = true) Authentication authentication,
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
        @Operation(summary = "Get current subscription", description = "Retrieves the authenticated user's current active subscription.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current subscription fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        public ApiResponse<SubscriptionResponse> getCurrentSubscription(
                        @Parameter(hidden = true) Authentication authentication) {

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
        @Operation(summary = "Get subscription history", description = "Retrieves the subscription purchase history of the authenticated user.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription history fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
        })
        public ApiResponse<List<SubscriptionResponse>> getSubscriptionHistory(
                        @Parameter(hidden = true) Authentication authentication) {

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
        @Operation(summary = "Get available subscription plans", description = "Retrieves all subscription plans currently available for purchase.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription plans fetched successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required")
        })
        public ApiResponse<List<SubscriptionPlanResponse>> getPlans() {

                return ApiResponse.success(
                                "Subscription plans fetched successfully.",
                                subscriptionService.getAvailablePlans());
        }
}