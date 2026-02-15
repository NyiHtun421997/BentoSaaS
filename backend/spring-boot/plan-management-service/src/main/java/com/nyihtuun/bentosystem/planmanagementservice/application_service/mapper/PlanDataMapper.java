package com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.domain.dto.AddressDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanMealResponseDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.PlanMeal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlanDataMapper {
    public Plan mapPlanDtoToPlan(PlanRequestDto planRequestDto) {
        return Plan.builder()
                   .title(planRequestDto.getTitle())
                   .description(planRequestDto.getDescription())
                   .categoryIds(planRequestDto.getCategoryIds().stream().map(CategoryId::new).collect(Collectors.toSet()))
                   .skipDays(planRequestDto.getSkipDays())
                   .address(mapAddressDtoToAddress(planRequestDto.getAddress()))
                   .imageUrl(planRequestDto.getImageUrl())
                   .displaySubscriptionFee(new Money(planRequestDto.getDisplaySubscriptionFee()))
                   .planMeals(mapPlanMealRequestDtosToPlanMeals(planRequestDto.getPlanMealRequestDtos()))
                   .build();
    }

    private List<PlanMeal> mapPlanMealRequestDtosToPlanMeals(List<PlanMealRequestDto> planMealRequestDtos) {
        return planMealRequestDtos.stream()
                                  .map(this::mapPlanMealRequestDtoToPlanMeal)
                                  .toList();
    }

    public PlanMeal mapPlanMealRequestDtoToPlanMeal(PlanMealRequestDto planMealRequestDto) {
        return PlanMeal.builder()
                       .name(planMealRequestDto.getName())
                       .description(planMealRequestDto.getDescription())
                       .pricePerMonth(new Money(planMealRequestDto.getPricePerMonth()))
                       .isPrimary(planMealRequestDto.isPrimary())
                       .minSubCount(new Threshold(planMealRequestDto.getMinSubCount()))
                       .imageUrl(planMealRequestDto.getImageUrl())
                       .build();
    }

    private Address mapAddressDtoToAddress(AddressDto addressDto) {
        return Address.builder()
                      .buildingNameRoomNo(addressDto.getBuildingNameRoomNo())
                      .chomeBanGo(addressDto.getChomeBanGo())
                      .district(addressDto.getDistrict())
                      .city(addressDto.getCity())
                      .prefecture(addressDto.getPrefecture())
                      .postalCode(addressDto.getPostalCode())
                      .location(addressDto.getLocation())
                      .build();
    }

    public PlanResponseDto mapPlanToPlanDto(Plan plan) {
        return PlanResponseDto.builder()
                              .planId(plan.getId().getValue())
                              .code(plan.getCode().getValue())
                              .title(plan.getTitle())
                              .status(plan.getStatus())
                              .description(plan.getDescription())
                              .categoryIds(plan.getCategoryIds().stream().map(BaseId::getValue).collect(Collectors.toSet()))
                              .address(mapAddressToAddressDto(plan.getAddress()))
                              .imageUrl(plan.getImageUrl())
                              .providerUserId(plan.getProviderUserId().getValue())
                              .skipDays(plan.getSkipDays())
                              .displaySubscriptionFee(plan.getDisplaySubscriptionFee().amount())
                              .planMealResponseDtos(mapPlanMealsToPlanMealResponseDtos(plan.getPlanMeals()))
                              .build();
    }

    private List<PlanMealResponseDto> mapPlanMealsToPlanMealResponseDtos(List<PlanMeal> planMeals) {
        return planMeals.stream()
                        .filter(planMeal -> !planMeal.isDeleteFlag())
                        .<PlanMealResponseDto>map(planMeal -> PlanMealResponseDto.builder()
                                                                                 .planMealId(planMeal.getId().getValue())
                                                                                 .planId(planMeal.getPlanId().getValue())
                                                                                 .name(planMeal.getName())
                                                                                 .description(planMeal.getDescription())
                                                                                 .pricePerMonth(planMeal.getPricePerMonth().amount())
                                                                                 .primary(planMeal.isPrimary())
                                                                                 .minSubCount(planMeal.getMinSubCount().min())
                                                                                 .currentSubCount(planMeal.getCurrentSubCount())
                                                                                 .imageUrl(planMeal.getImageUrl())
                                                                                 .build())
                        .toList();
    }

    private AddressDto mapAddressToAddressDto(Address address) {
        return AddressDto.builder()
                         .buildingNameRoomNo(address.getBuildingNameRoomNo())
                         .chomeBanGo(address.getChomeBanGo())
                         .district(address.getDistrict())
                         .city(address.getCity())
                         .prefecture(address.getPrefecture())
                         .postalCode(address.getPostalCode())
                         .location(address.getLocation())
                         .build();
    }

    public PlanUpdateCommand mapPlanDtoToPlanUpdateCommand(PlanRequestDto planRequestDto) {
        return PlanUpdateCommand.builder()
                                .title(planRequestDto.getTitle())
                                .description(planRequestDto.getDescription())
                                .categoryIds(planRequestDto.getCategoryIds()
                                                           .stream()
                                                           .map(CategoryId::new)
                                                           .collect(Collectors.toSet()))
                                .skipDays(planRequestDto.getSkipDays())
                                .address(mapAddressDtoToAddress(planRequestDto.getAddress()))
                                .imageUrl(planRequestDto.getImageUrl())
                                .displaySubscriptionFee(new Money(planRequestDto.getDisplaySubscriptionFee()))
                                .build();
    }

    public PlanMealUpdateCommand mapPlanMealDtoToPlanMealUpdateCommand(PlanMealRequestDto planMealRequestDto) {
        return PlanMealUpdateCommand.builder()
                                    .name(planMealRequestDto.getName())
                                    .description(planMealRequestDto.getDescription())
                                    .pricePerMonth(new Money(planMealRequestDto.getPricePerMonth()))
                                    .isPrimary(planMealRequestDto.isPrimary())
                                    .minSubCount(new Threshold(planMealRequestDto.getMinSubCount()))
                                    .imageUrl(planMealRequestDto.getImageUrl())
                                    .build();
    }
}
