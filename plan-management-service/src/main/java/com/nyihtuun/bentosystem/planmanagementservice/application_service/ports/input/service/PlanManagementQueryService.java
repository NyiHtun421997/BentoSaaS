package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.dto.CategoryDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanManagementQueryService {
    List<PlanResponseDto> getActivePlans(int page, int size);

    Optional<PlanResponseDto> getPlanByTitleAndCode(String title, String code);

    List<PlanResponseDto> getPlansByCategoryId(UUID categoryId, int page, int size);

    List<PlanResponseDto> getPlansByDate(LocalDate start, LocalDate end, int page, int size);

    List<PlanResponseDto> getPlansByUserId(UUID userId);

    List<PlanResponseDto> getPlansNearMe(double latitude, double longitude, int page, int size);

    List<CategoryDto> getCategories();

    Optional<DeliverySchedule> getDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end);

    Optional<PlanResponseDto> getPlanByPlanId(UUID planId);
}
