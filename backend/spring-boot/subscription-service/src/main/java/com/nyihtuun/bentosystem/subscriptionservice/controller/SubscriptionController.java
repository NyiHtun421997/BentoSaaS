package com.nyihtuun.bentosystem.subscriptionservice.controller;

import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.subscriptionservice.controller.ApiPaths.SUBSCRIPTION_ID;
import static com.nyihtuun.bentosystem.subscriptionservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1)
@Tag(name = "Subscription", description = "Endpoints for managing user subscriptions to bento plans.")
public class SubscriptionController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    @Autowired
    public SubscriptionController(SubscriptionCommandService subscriptionCommandService,
                                  SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @GetMapping(SUBSCRIPTION_ID)
    @Operation(summary = "Get subscription details", description = "Retrieves detailed information of a subscription by ID.")
    @ApiResponse(responseCode = "200", description = "Subscription details retrieved successfully")
    public ResponseEntity<SubscriptionResponseDto> findSubscriptionDetails(@PathVariable UUID subscriptionId) {
        log.info("Fetching subscription details by subscriptionId: {}", subscriptionId);
        return subscriptionQueryService.getSubscriptionById(subscriptionId)
                                       .map(subscription -> {
                                           log.info("Subscription details by subscriptionId: {} : {}", subscriptionId, subscription);
                                           return ResponseEntity.ok(subscription);
                                       })
                                       .orElseThrow(() -> {
                                           log.error("No subscription found for subscriptionId: {}", subscriptionId);
                                           return new SubscriptionDomainException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID);
                                       });
    }

    @GetMapping("byuseridanddate")
    @Operation(summary = "Find user subscriptions", description = "Retrieves a list of subscriptions for a specific user since a given date.")
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    public ResponseEntity<List<SubscriptionResponseDto>> findMySubscriptions(UUID userId, LocalDate since) {
        log.info("Fetching subscriptions by userId and date");
        List<SubscriptionResponseDto> mySubscriptions = subscriptionQueryService.getMySubscriptions(userId, since);
        log.info("Subscriptions by userId and date: {}", mySubscriptions);
        return ResponseEntity.ok(mySubscriptions);
    }

    @PostMapping
    @Operation(summary = "Create a subscription", description = "Initiates a new subscription to a bento plan.")
    @ApiResponse(responseCode = "200", description = "Subscription created successfully")
    public ResponseEntity<SubscriptionResponseDto> createSubscription(
            @Valid @RequestBody SubscriptionRequestDto subscriptionRequestDto,
            @RequestHeader(name = "X-USER-ID") String userIdStr) {

        UUID userId = UUID.fromString(userIdStr);

        log.info("Creating subscription: {}", subscriptionRequestDto);
        SubscriptionResponseDto createdSubscription = subscriptionCommandService.validateAndInitiateSubscription(subscriptionRequestDto,
                                                                                                                 new UserId(userId));
        log.info("Subscription created: {}", createdSubscription);
        return ResponseEntity.ok(createdSubscription);
    }

    @PutMapping( SUBSCRIPTION_ID)
    @Operation(summary = "Update subscription", description = "Updates an existing subscription's meal selections or other details.")
    @ApiResponse(responseCode = "200", description = "Subscription updated successfully")
    public ResponseEntity<SubscriptionResponseDto> updateSubscription(@PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        log.info("Updating subscription with id: {} with: {}", subscriptionId, subscriptionRequestDto);
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.validateAndUpdateSubscription(new SubscriptionId(
                subscriptionId), subscriptionRequestDto);
        log.info("Subscription with id: {} is updated: {}", subscriptionId, subscriptionResponseDto);
        return ResponseEntity.ok(subscriptionResponseDto);
    }

    @DeleteMapping(SUBSCRIPTION_ID)
    @Operation(summary = "Cancel subscription", description = "Cancels an active subscription.")
    @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully")
    public ResponseEntity<SubscriptionResponseDto> cancelSubscription(@PathVariable UUID subscriptionId) {
        log.info("Cancelling subscription with id: {}", subscriptionId);
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.cancelSubscription(new SubscriptionId(
                subscriptionId));
        log.info("Subscription with id: {} is cancelled", subscriptionId);
        return ResponseEntity.ok(subscriptionResponseDto);
    }

}
