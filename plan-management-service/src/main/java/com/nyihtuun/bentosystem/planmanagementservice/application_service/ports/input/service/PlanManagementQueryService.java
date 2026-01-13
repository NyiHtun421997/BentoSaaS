package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanManagementQueryService {
    List<PlanResponseDto> getActivePlans();

    Optional<PlanResponseDto> getPlanByName(String planName);

    List<PlanResponseDto> getPlansByCategoryId(UUID categoryId);

    List<PlanResponseDto> getPlansByDate(LocalDate start, LocalDate end);

    Category getCategoryById(UUID categoryId);

    List<Category> getCategories();

    List<DeliverySchedule> getDeliverySchedulesByPlanId(UUID planId);
}
