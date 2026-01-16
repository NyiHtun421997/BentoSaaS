package com.nyihtuun.bentosystem.planmanagementservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.valueobject.CategoryId;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.CategoryEntity;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.DeliveryScheduleEntity;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanEntity;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository.CategoryJpaRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository.DeliveryScheduleJpaRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository.PlanJpaRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper.PlanManagementDataAccessMapper;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlanManagementRepositoryImpl implements PlanManagementRepository {

    private final PlanJpaRepository planJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final DeliveryScheduleJpaRepository deliveryScheduleJpaRepository;
    private final PlanManagementDataAccessMapper mapper;

    private Pageable getPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 5);
        return PageRequest.of(safePage, safeSize);
    }

    @Autowired
    public PlanManagementRepositoryImpl(PlanJpaRepository planJpaRepository,
                                        CategoryJpaRepository categoryJpaRepository,
                                        DeliveryScheduleJpaRepository deliveryScheduleJpaRepository,
                                        PlanManagementDataAccessMapper mapper) {
        this.planJpaRepository = planJpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
        this.deliveryScheduleJpaRepository = deliveryScheduleJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Plan> findActivePlans(int page, int size) {
        return planJpaRepository.findPlanEntitiesByDeleteFlag(false, getPageable(page, size)).stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public Optional<Plan> findPlanByTitleAndCode(String title, String code) {
        return planJpaRepository.findEntityByTitleAndCode(title, code)
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, true));
    }

    @Override
    public List<Plan> findPlansByCategory(UUID categoryId, int page, int size) {
        return planJpaRepository.findPlanEntityByCategoryId(categoryId).stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public List<Plan> findActivePlansBetweenDates(LocalDate start, LocalDate end, int page, int size) {
        return planJpaRepository.findPlanEntitiesByDeleteFlagFalseAndCreatedAtBetween(start.atStartOfDay(),
                                                                                      end.atStartOfDay(),
                                                                                      getPageable(page, size))
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public Optional<Plan> findByPlanId(UUID planId) {
        return planJpaRepository.findById(planId)
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, true));
    }

    @Override
    public List<Plan> findPlansByUserId(UUID userId) {
        return planJpaRepository.findPlanEntitiesByUserId(userId)
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public List<Plan> findPlansNearMe(double latitude, double longitude, double radiusMeters, int page, int size) {
        return planJpaRepository.findActivePlansNearLocation(latitude, longitude, radiusMeters)
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public Plan save(Plan plan) {
        List<CategoryEntity> categoryEntities = categoryJpaRepository.findAllById(plan.getCategoryIds()
                                                                                      .stream()
                                                                                      .map(CategoryId::getValue)
                                                                                      .toList());
        PlanEntity planEntity = mapper.planToPlanEntity(plan, categoryEntities);
        PlanEntity savedPlanEntity = planJpaRepository.save(planEntity);
        return mapper.planEntityToPlan(savedPlanEntity, true);
    }

    @Override
    public void deleteByPlanId(UUID planId) {
        planJpaRepository.findById(planId).ifPresent(planEntity -> {
            planEntity.setDeleteFlag(true);
            planEntity.setDeletedAt(LocalDateTime.now());
            planJpaRepository.save(planEntity);
        });
    }

    @Override
    public Category saveCategory(Category category) {
        CategoryEntity categoryEntity = mapper.categoryToCategoryEntity(category);
        CategoryEntity savedCategoryEntity = categoryJpaRepository.save(categoryEntity);
        return mapper.categoryEntityToCategory(savedCategoryEntity);
    }

    @Override
    public List<DeliverySchedule> findDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end) {
        return deliveryScheduleJpaRepository.findDeliveryScheduleByPlanIdAndCreatedAtBetween(planId,
                                                                                             start.atStartOfDay(),
                                                                                             end.atStartOfDay())
                                            .stream()
                                            .map(mapper::deliveryScheduleEntityToDeliverySchedule)
                                            .toList();
    }

    @Override
    public DeliverySchedule saveDeliverySchedule(DeliverySchedule deliverySchedule) {
        PlanEntity planEntity = planJpaRepository.findById(deliverySchedule.getPlanId().getValue())
                                                 .orElseThrow(() -> new PlanManagementDomainException(
                                                         PlanManagementErrorCode.INVALID_PLAN_ID));

        DeliveryScheduleEntity deliveryScheduleEntity = mapper.deliveryScheduleToDeliveryScheduleEntity(deliverySchedule, planEntity);
        DeliveryScheduleEntity savedDeliveryScheduleEntity = deliveryScheduleJpaRepository.save(deliveryScheduleEntity);
        return mapper.deliveryScheduleEntityToDeliverySchedule(savedDeliveryScheduleEntity);
    }
}
