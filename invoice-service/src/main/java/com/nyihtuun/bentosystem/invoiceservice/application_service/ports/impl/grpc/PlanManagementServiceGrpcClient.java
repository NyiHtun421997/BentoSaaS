package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.impl.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import plan_management_grpc.InvoiceRequest;
import plan_management_grpc.PlanManagementResponse;
import plan_management_grpc.PlanManagementServiceGrpc;

import java.util.List;

import static com.nyihtuun.bentosystem.invoiceservice.application_service.InvoiceConstants.*;

@Slf4j
@Service
public class PlanManagementServiceGrpcClient {
    private final PlanManagementServiceGrpc.PlanManagementServiceBlockingStub planManagementServiceBlockingStub;

    public PlanManagementServiceGrpcClient(
            @Value(PLAN_MANAGEMENT_SERVICE_ADDRESS) String serverAddress,
            @Value(PLAN_MANAGEMENT_SERVICE_PORT) int serverPort
    ) {
        log.info("Connecting to PlanManagement Service gRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                                                      .usePlaintext()
                                                      .build();

        planManagementServiceBlockingStub = PlanManagementServiceGrpc.newBlockingStub(channel);
    }

    public PlanManagementResponse fetchPlanMealPrices(List<String> planIds) {
        InvoiceRequest request = InvoiceRequest.newBuilder()
                                              .addAllPlanIds(planIds)
                                              .build();

        PlanManagementResponse response = planManagementServiceBlockingStub.fetchPlanMealPrices(request);
        log.info("Received plan-management response from PlanManagement Service via gRPC: {}", response.toString());
        return response;
    }
}
