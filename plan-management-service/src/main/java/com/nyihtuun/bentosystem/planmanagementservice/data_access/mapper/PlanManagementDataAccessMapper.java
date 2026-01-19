package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.*;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper.GeoPointMapper.toPoint;

@Component
public class PlanManagementDataAccessMapper {

    public Plan planEntityToPlan(PlanEntity planEntity,
                                 boolean mapPlanMealEntities) {
        PlanId planId = new PlanId(planEntity.getId());
        return Plan.builder()
                   .planId(planId)
                   .code(new Code(planEntity.getCode()))
                   .title(planEntity.getTitle())
                   .description(planEntity.getDescription())
                   .status(planEntity.getPlanStatus())
                   .createdAt(planEntity.getCreatedAt())
                   .updatedAt(planEntity.getUpdatedAt())
                   .skipDays(planEntity.getSkipDates())
                   .address(addressEntityToAddress(planEntity.getAddressEntity()))
                   .displaySubscriptionFee(new Money(planEntity.getDisplaySubscriptionFee()))
                   .planMeals(planMealEntitiesToPlanMeals(mapPlanMealEntities ? planEntity.getPlanMealEntities() : new ArrayList<>(), planId))
                   .deleteFlag(planEntity.getDeleteFlag())
                   .deletedAt(planEntity.getDeletedAt())
                   .build();

    }

    private Address addressEntityToAddress(AddressEntity addressEntity) {
        return Address.builder()
                      .buildingNameRoomNo(addressEntity.getBuildingNameRoomNo())
                      .chomeBanGo(addressEntity.getChomeBanGo())
                      .district(addressEntity.getDistrict())
                      .postalCode(addressEntity.getPostalCode())
                      .city(addressEntity.getCity())
                      .prefecture(addressEntity.getPrefecture())
                      .location(new GeoPoint(addressEntity.getLocation().getY(), addressEntity.getLocation().getX()))
                      .build();
    }

    public List<PlanMeal> planMealEntitiesToPlanMeals(List<PlanMealEntity> planMealEntities, PlanId planId) {
        return planMealEntities.stream()
                               .map(planMealEntity ->
                                            PlanMeal.builder()
                                                    .planMealId(new PlanMealId(planMealEntity.getId()))
                                                    .planId(planId)
                                                    .name(planMealEntity.getName())
                                                    .description(planMealEntity.getDescription())
                                                    .pricePerMonth(new Money(planMealEntity.getPricePerMonth()))
                                                    .isPrimary(planMealEntity.getIsPrimary())
                                                    .minSubCount(new Threshold(planMealEntity.getMinSubCount()))
                                                    .currentSubCount(planMealEntity.getCurrentSubCount())
                                                    .imageUrl(planMealEntity.getImageUrl())
                                                    .createdAt(planMealEntity.getCreatedAt())
                                                    .updatedAt(planMealEntity.getUpdatedAt())
                                                    .deleteFlag(planMealEntity.getDeleteFlag())
                                                    .deletedAt(planMealEntity.getDeletedAt())
                                                    .build())
                               .toList();

    }

    public PlanEntity planToPlanEntity(Plan plan, List<CategoryEntity> categoryEntities) {
        PlanEntity planEntity = PlanEntity.builder()
                                          .id(plan.getId().getValue())
                                          .code(plan.getCode().getValue())
                                          .title(plan.getTitle())
                                          .description(plan.getDescription())
                                          .planStatus(plan.getStatus())
                                          .createdAt(plan.getCreatedAt())
                                          .updatedAt(plan.getUpdatedAt())
                                          .userId(plan.getProviderUserId().getValue())
                                          .skipDates(plan.getSkipDays())
                                          .addressEntity(addressToAddressEntity(plan.getAddress()))
                                          .categoryEntities(categoryEntities)
                                          .displaySubscriptionFee(plan.getDisplaySubscriptionFee().amount())
                                          .deleteFlag(plan.isDeleteFlag())
                                          .deletedAt(plan.getDeletedAt())
                                          .planMealEntities(planMealsToPlanMealEntities(plan.getPlanMeals()))
                                          .build();
        planEntity.getAddressEntity().setPlanEntity(planEntity);
        planEntity.getPlanMealEntities().forEach(planMealEntity -> planMealEntity.setPlanEntity(planEntity));
        return planEntity;
    }

    private AddressEntity addressToAddressEntity(Address address) {
        return AddressEntity.builder()
                            .buildingNameRoomNo(address.getBuildingNameRoomNo())
                            .chomeBanGo(address.getChomeBanGo())
                            .district(address.getDistrict())
                            .postalCode(address.getPostalCode())
                            .city(address.getCity())
                            .prefecture(address.getPrefecture())
                            .location(toPoint(address.getLocation()))
                            .build();
    }

    private List<PlanMealEntity> planMealsToPlanMealEntities(List<PlanMeal> planMeals) {
        return planMeals.stream()
                        .map(planMeal ->
                                     PlanMealEntity.builder()
                                                   .id(planMeal.getId().getValue())
                                                   .name(planMeal.getName())
                                                   .description(planMeal.getDescription())
                                                   .pricePerMonth(planMeal.getPricePerMonth().amount())
                                                   .isPrimary(planMeal.isPrimary())
                                                   .minSubCount(planMeal.getMinSubCount().min())
                                                   .currentSubCount(planMeal.getCurrentSubCount())
                                                   .imageUrl(planMeal.getImageUrl())
                                                   .createdAt(planMeal.getCreatedAt())
                                                   .updatedAt(planMeal.getUpdatedAt())
                                                   .deleteFlag(planMeal.isDeleteFlag())
                                                   .deletedAt(planMeal.getDeletedAt())
                                                   .build()
                        )
                        .toList();
    }

    public Category categoryEntityToCategory(CategoryEntity categoryEntity) {
        return new Category(new CategoryId(categoryEntity.getId()), categoryEntity.getName());
    }

    public CategoryEntity categoryToCategoryEntity(Category category) {
        return CategoryEntity.builder()
                             .id(category.getId().getValue())
                             .name(category.getName())
                             .build();
    }

    public DeliverySchedule deliveryScheduleEntityToDeliverySchedule(DeliveryScheduleEntity deliveryScheduleEntity) {
        DeliveryScheduleId deliveryScheduleId =
                new DeliveryScheduleId(deliveryScheduleEntity.getId());

        return DeliverySchedule.builder()
                               .deliveryScheduleId(deliveryScheduleId)
                               .planId(new PlanId(deliveryScheduleEntity.getPlanEntity().getId()))
                               .periodStart(deliveryScheduleEntity.getPeriodStart())
                               .periodEnd(deliveryScheduleEntity.getPeriodEnd())
                               .createdAt(deliveryScheduleEntity.getCreatedAt())
                               .deliverySchedules(
                                       deliveryScheduleDetailEntitiesToDeliveryScheduleDetails(
                                               deliveryScheduleEntity.getDeliveryScheduleDetailEntities(),
                                               deliveryScheduleId
                                       )
                               )
                               .build();
    }

    public DeliveryScheduleEntity deliveryScheduleToDeliveryScheduleEntity(DeliverySchedule deliverySchedule, PlanEntity planEntity) {
        DeliveryScheduleEntity deliveryScheduleEntity = DeliveryScheduleEntity.builder()
                                                                              .id(deliverySchedule.getId().getValue())
                                                                              .planEntity(planEntity)
                                                                              .periodStart(deliverySchedule.getPeriodStart())
                                                                              .periodEnd(deliverySchedule.getPeriodEnd())
                                                                              .createdAt(deliverySchedule.getCreatedAt())
                                                                              .build();

        List<DeliveryScheduleDetailEntity> detailEntities =
                deliveryScheduleDetailsToDeliveryScheduleDetailEntities(
                        deliverySchedule.getDeliveryScheduleDetails(),
                        deliveryScheduleEntity,
                        planEntity.getPlanMealEntities()
                );

        deliveryScheduleEntity.setDeliveryScheduleDetailEntities(detailEntities);
        return deliveryScheduleEntity;
    }

    private List<DeliveryScheduleDetail> deliveryScheduleDetailEntitiesToDeliveryScheduleDetails(
            List<DeliveryScheduleDetailEntity> detailEntities,
            DeliveryScheduleId deliveryScheduleId
    ) {
        return detailEntities.stream()
                             .map(detailEntity -> deliveryScheduleDetailEntityToDeliveryScheduleDetail(detailEntity,
                                                                                                       deliveryScheduleId))
                             .toList();
    }

    private List<DeliveryScheduleDetailEntity> deliveryScheduleDetailsToDeliveryScheduleDetailEntities(
            List<DeliveryScheduleDetail> details,
            DeliveryScheduleEntity parent,
            List<PlanMealEntity> planMealEntities) {
        Map<UUID, PlanMealEntity> planMealEntityMap = planMealEntities.stream()
                                                                      .collect(Collectors.toMap(
                                                                              PlanMealEntity::getId,
                                                                              Function.identity()));

        return details.stream()
                      .map(detail -> deliveryScheduleDetailToDeliveryScheduleDetailEntity(detail, parent, planMealEntityMap))
                      .toList();
    }

    private DeliveryScheduleDetail deliveryScheduleDetailEntityToDeliveryScheduleDetail(
            DeliveryScheduleDetailEntity detailEntity,
            DeliveryScheduleId deliveryScheduleId
    ) {
        return DeliveryScheduleDetail.builder()
                                     .deliveryScheduleDetailId(new DeliveryScheduleDetailId(detailEntity.getId()))
                                     .deliveryScheduleId(deliveryScheduleId)
                                     .planMealId(new PlanMealId(detailEntity.getPlanMealEntity().getId()))
                                     .deliveryDate(detailEntity.getDeliveryDate())
                                     .build();
    }

    private DeliveryScheduleDetailEntity deliveryScheduleDetailToDeliveryScheduleDetailEntity(
            DeliveryScheduleDetail detail,
            DeliveryScheduleEntity parent,
            Map<UUID, PlanMealEntity> planMealEntityMap) {
        return DeliveryScheduleDetailEntity.builder()
                                           .id(detail.getId().getValue())
                                           .deliveryScheduleEntity(parent)
                                           .planMealEntity(planMealEntityMap.get(detail.getPlanMealId()))
                                           .deliveryDate(detail.getDeliveryDate())
                                           .build();
    }
}
