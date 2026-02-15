package com.nyihtuun.bentosystem.planmanagementservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.valueobject.CategoryId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.ASIA_TOKYO_ZONE;

@Component
public class PlanManagementRepositoryImpl implements PlanManagementRepository {

    private final PlanJpaRepository planJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final DeliveryScheduleJpaRepository deliveryScheduleJpaRepository;
    private final PlanManagementDataAccessMapper mapper;

    @Value("${plan-management.max-page-size:5}")
    private int maxPageSize;

    private Pageable getPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), maxPageSize);
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
        return planJpaRepository.findPlanEntityByTitleContainingIgnoreCaseAndCodeAndDeleteFlagFalse(title, code)
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
        Instant from = start.atStartOfDay(ZoneId.of(ASIA_TOKYO_ZONE)).toInstant();
        Instant to = end.plusDays(1).atStartOfDay(ZoneId.of(ASIA_TOKYO_ZONE)).toInstant();

        return planJpaRepository.findPlanEntitiesByDeleteFlagFalseAndCreatedAtBetween(from,
                                                                                      to,
                                                                                      getPageable(page, size))
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public List<Plan> findActivePlansBetweenDates(LocalDate before) {
        Instant beforeInst = before.atStartOfDay(ZoneId.of(ASIA_TOKYO_ZONE)).toInstant();

        return planJpaRepository.findPlanEntitiesByDeleteFlagFalseAndCreatedAtBeforeAndPlanStatus(beforeInst,
                                                                                                   PlanStatus.ACTIVE)
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, true))
                                .toList();
    }

    @Override
    public Optional<Plan> findByPlanId(UUID planId) {
        return planJpaRepository.findByIdAndDeleteFlagFalse(planId)
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, true));
    }

    @Override
    public List<Plan> findPlansByUserId(UUID userId) {
        return planJpaRepository.findPlanEntitiesByUserIdAndDeleteFlagFalse(userId)
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public List<Plan> findPlansNearMe(double latitude, double longitude, double radiusMeters, int page, int size) {
        return planJpaRepository.findActivePlansNearLocationAndDeleteFlagFalse(latitude, longitude, radiusMeters)
                                .stream()
                                .map(planEntity -> mapper.planEntityToPlan(planEntity, false))
                                .toList();
    }

    @Override
    public Plan save(Plan plan, boolean flush) {
        List<CategoryEntity> categoryEntities = categoryJpaRepository.findAllById(plan.getCategoryIds()
                                                                                      .stream()
                                                                                      .map(CategoryId::getValue)
                                                                                      .toList());
        PlanEntity planEntity = mapper.planToPlanEntity(plan, categoryEntities);
        PlanEntity savedPlanEntity = planJpaRepository.save(planEntity);
        if (flush) {
            planJpaRepository.flush();
        }
        return mapper.planEntityToPlan(savedPlanEntity, true);
    }

    @Override
    public Category saveCategory(Category category) {
        CategoryEntity categoryEntity = mapper.categoryToCategoryEntity(category);
        CategoryEntity savedCategoryEntity = categoryJpaRepository.saveAndFlush(categoryEntity);
        return mapper.categoryEntityToCategory(savedCategoryEntity);
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryJpaRepository.findAll()
                                    .stream()
                                    .map(mapper::categoryEntityToCategory)
                                    .toList();
    }

    @Override
    public Optional<DeliverySchedule> findDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end) {
        Instant startInstant = start.atStartOfDay(ZoneId.of(ASIA_TOKYO_ZONE)).toInstant();
        Instant endInstant = end.atStartOfDay(ZoneId.of(ASIA_TOKYO_ZONE)).toInstant();

        return deliveryScheduleJpaRepository.findDeliveryScheduleByPlanIdAndCreatedAtBetween(planId,
                                                                                             startInstant,
                                                                                             endInstant)
                                            .map(mapper::deliveryScheduleEntityToDeliverySchedule);
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
