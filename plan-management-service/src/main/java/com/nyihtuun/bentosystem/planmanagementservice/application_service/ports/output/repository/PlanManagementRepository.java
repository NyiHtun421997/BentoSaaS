package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanManagementRepository {
    List<Plan> findActivePlans();
    Optional<Plan> findPlanByName(String planName);
    List<Plan> findPlansByCategory(UUID categoryId);
    List<Plan> findPlansBetweenDates(LocalDate start, LocalDate end);
    List<Plan> findActivePlansBetweenDates(LocalDate start, LocalDate end);
    Plan save(Plan plan);
    Optional<Plan> findByPlanId(UUID planId);
    void deleteByPlanId(UUID planId);
    Optional<Category> findCategoryById(UUID categoryId);
    Category saveCategory(Category category);
    List<DeliverySchedule> findDeliverySchedulesByPlanId(UUID planId);
    DeliverySchedule saveDeliverySchedule(DeliverySchedule deliverySchedule);
}
