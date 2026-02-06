package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.grpc;

import com.nyihtuun.bentosystem.domain.dto.response.PlanMealResponseDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import plan_management_grpc.PlanManagementResponse;
import plan_management_grpc.PlanManagementServiceGrpc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@GrpcService
public class PlanManagementGrpcService extends PlanManagementServiceGrpc.PlanManagementServiceImplBase {

    private final PlanManagementQueryService planManagementQueryService;

    public PlanManagementGrpcService(PlanManagementQueryService planManagementQueryService) {
        this.planManagementQueryService = planManagementQueryService;
    }

    @Override
    public void fetchPlanMealPrices(plan_management_grpc.InvoiceRequest request,
                                    io.grpc.stub.StreamObserver<plan_management_grpc.PlanManagementResponse> responseObserver) {
        log.info("Received invoice request: {}", request.toString());

        List<PlanManagementResponse.PlanDetail> planDetails =
                request.getPlanIdsList()
                       .stream()
                       .map(planIdString -> {
                           UUID planId = UUID.fromString(planIdString);
                           return planManagementQueryService.getPlanByPlanId(planId);
                       })
                       .flatMap(Optional::stream)
                       .map(this::toPlanDetail)
                       .toList();

        log.info("Sending plan details: {}", planDetails);

        PlanManagementResponse response = PlanManagementResponse.newBuilder()
                                                                .addAllPlanDetails(planDetails)
                                                                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private PlanManagementResponse.PlanDetail toPlanDetail(PlanResponseDto planResponseDto) {
        List<PlanManagementResponse.PlanDetail.PlanMeal> planMeals = planResponseDto.getPlanMealResponseDtos()
                                                                                    .stream()
                                                                                    .filter(planMealResponseDto ->
                                                                                                    planMealResponseDto.getCurrentSubCount() >= planMealResponseDto.getMinSubCount())
                                                                                    .map(this::toPlanMeal)
                                                                                    .toList();

        return PlanManagementResponse.PlanDetail.newBuilder()
                                                .setPlanId(planResponseDto.getPlanId().toString())
                                                .addAllPlanMeals(planMeals)
                                                .build();
    }

    private PlanManagementResponse.PlanDetail.PlanMeal toPlanMeal(PlanMealResponseDto planMealResponseDto) {
        return PlanManagementResponse.PlanDetail.PlanMeal.newBuilder()
                                                         .setPlanMealId(planMealResponseDto.getPlanMealId().toString())
                                                         .setPricePerMonth(planMealResponseDto.getPricePerMonth().toString())
                                                         .build();
    }
}
