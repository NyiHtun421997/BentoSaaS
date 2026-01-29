package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.grpc;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import subscription_grpc.SubscriptionResponse;
import subscription_grpc.SubscriptionServiceGrpc;

import java.time.Instant;
import java.util.List;

@Slf4j
@GrpcService
public class SubscriptionGrpcService extends SubscriptionServiceGrpc.SubscriptionServiceImplBase {

    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionGrpcService(SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @Override
    public void fetchActiveSubscriptions(subscription_grpc.InvoiceRequest request,
                                         io.grpc.stub.StreamObserver<subscription_grpc.SubscriptionResponse> responseObserver) {
        log.info("Received invoice request: {}", request.toString());

        Instant instant = Instant.ofEpochSecond(request.getInvoicingEventTime().getSeconds(),
                                                request.getInvoicingEventTime().getNanos());

        List<SubscriptionResponseDto> activeSubscriptionsBefore = subscriptionQueryService.getActiveSubscriptionsBefore(instant);

        log.info("Active subscriptions before: {}", activeSubscriptionsBefore);

        List<SubscriptionResponse.Subscription> subscriptionStream =
                activeSubscriptionsBefore
                        .stream()
                        .map(subscriptionResponseDto -> SubscriptionResponse.Subscription.newBuilder()
                                                                                         .setSubscriptionId(
                                                                                                 subscriptionResponseDto.getSubscriptionId()
                                                                                                                        .toString())
                                                                                         .setPlanId(subscriptionResponseDto.getPlanId()
                                                                                                                           .toString())
                                                                                         .setUserId(
                                                                                                 subscriptionResponseDto.getUserId()
                                                                                                                        .toString())
                                                                                         .setProvidedUserId(
                                                                                                 subscriptionResponseDto.getProvidedUserId()
                                                                                                                        .toString())
                                                                                         .addAllPlanMealIds(
                                                                                                 subscriptionResponseDto.getMealSelectionResponseDtos()
                                                                                                                        .stream()
                                                                                                                        .map(mealSelectionResponseDto -> mealSelectionResponseDto.getPlanMealId()
                                                                                                                                                                                 .toString())
                                                                                                                        .toList())
                                                                                         .build())
                        .toList();

        SubscriptionResponse subscriptionResponse = SubscriptionResponse.newBuilder()
                                                                        .addAllSubscriptions(subscriptionStream)
                                                                        .build();
        log.info("Sending subscription response: {}", subscriptionResponse.toString());

        responseObserver.onNext(subscriptionResponse);
        responseObserver.onCompleted();
    }
}
