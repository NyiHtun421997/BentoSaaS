package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.impl.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import subscription_grpc.InvoiceRequest;
import subscription_grpc.SubscriptionResponse;
import subscription_grpc.SubscriptionServiceGrpc;
import com.google.protobuf.Timestamp;

import java.time.Instant;

import static com.nyihtuun.bentosystem.invoiceservice.application_service.InvoiceConstants.*;

@Slf4j
@Service
public class SubscriptionServiceGrpcClient {
    private final SubscriptionServiceGrpc.SubscriptionServiceBlockingStub subscriptionServiceBlockingStub;

    public SubscriptionServiceGrpcClient(
            @Value(SUBSCRIPTION_SERVICE_ADDRESS) String serverAddress,
            @Value(SUBSCRIPTION_SERVICE_PORT) int serverPort
    ) {
        log.info("Connecting to Subscription Service gRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                                                      .usePlaintext()
                                                      .build();

        subscriptionServiceBlockingStub = SubscriptionServiceGrpc.newBlockingStub(channel);
    }

    public SubscriptionResponse fetchActiveSubscriptions(Instant instant) {
        Timestamp invoicingTimestamp = Timestamp.newBuilder()
                                       .setSeconds(instant.getEpochSecond())
                                       .setNanos(instant.getNano())
                                       .build();

        InvoiceRequest request = InvoiceRequest.newBuilder()
                .setInvoicingEventTime(invoicingTimestamp)
                .build();

        SubscriptionResponse response = subscriptionServiceBlockingStub.fetchActiveSubscriptions(request);
        log.info("Received subscription response from Subscription Service via gRPC: {}", response.toString());
        return response;
    }
}
