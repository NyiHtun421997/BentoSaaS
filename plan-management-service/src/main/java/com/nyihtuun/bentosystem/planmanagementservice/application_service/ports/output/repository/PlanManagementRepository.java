package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanManagementRepository {
    List<Plan> findActivePlans(int page, int size);
    Optional<Plan> findPlanByTitleAndCode(String planName, String code);
    List<Plan> findPlansByCategory(UUID categoryId, int page, int size);
    List<Plan> findActivePlansBetweenDates(LocalDate start, LocalDate end, int page, int size);
    List<Plan> findActivePlansBetweenDates(LocalDate start, LocalDate end);
    Optional<Plan> findByPlanId(UUID planId);
    List<Plan> findPlansByUserId(UUID userId);
    List<Plan> findPlansNearMe(double latitude, double longitude, double radiusMeters, int page, int size);
    Plan save(Plan plan);
    Category saveCategory(Category category);
    List<Category> findAllCategories();
    Optional<DeliverySchedule> findDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end);
    DeliverySchedule saveDeliverySchedule(DeliverySchedule deliverySchedule);
}

