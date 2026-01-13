package com.nyihtuun.bentosystem.planmanagementservice.data_access.adapter;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlanManagementRepositoryImpl implements PlanManagementRepository {
    @Override
    public List<Plan> findActivePlans() {
        return List.of();
    }

    @Override
    public Optional<Plan> findPlanByName(String planName) {
        return Optional.empty();
    }

    @Override
    public List<Plan> findPlansByCategory(UUID categoryId) {
        return List.of();
    }

    @Override
    public List<Plan> findPlansBetweenDates(LocalDate start, LocalDate end) {
        return List.of();
    }

    @Override
    public List<Plan> findActivePlansBetweenDates(LocalDate start, LocalDate end) {
        return List.of();
    }

    @Override
    public Plan save(Plan plan) {
        return null;
    }

    @Override
    public Optional<Plan> findByPlanId(UUID planId) {
        return Optional.empty();
    }

    @Override
    public void deleteByPlanId(UUID planId) {

    }

    @Override
    public Optional<Category> findCategoryById(UUID categoryId) {
        return Optional.empty();
    }

    @Override
    public Category saveCategory(Category category) {
        return null;
    }

    @Override
    public List<DeliverySchedule> findDeliverySchedulesByPlanId(UUID planId) {
        return List.of();
    }
}
