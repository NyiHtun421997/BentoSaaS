package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PlanManagementQueryServiceImpl implements PlanManagementQueryService {

    private final PlanManagementRepository planManagementRepository;
    private final PlanDataMapper planDataMapper;

    public PlanManagementQueryServiceImpl(PlanManagementRepository planManagementRepository, PlanDataMapper planDataMapper) {
        this.planManagementRepository = planManagementRepository;
        this.planDataMapper = planDataMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getActivePlans() {
        return planManagementRepository.findActivePlans().stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
    }

    @Override
    public Optional<PlanResponseDto> getPlanByName(String planName) {
        return Optional.empty();
    }

    @Override
    public List<PlanResponseDto> getPlansByCategoryId(UUID categoryId) {
        return List.of();
    }

    @Override
    public List<PlanResponseDto> getPlansByDate(LocalDate start, LocalDate end) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(UUID categoryId) {
        return planManagementRepository.findCategoryById(categoryId)
                                       .orElseThrow(() -> new PlanManagementDomainException(PlanManagementErrorCode.INVALID_CATEGORY_ID));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverySchedule> getDeliverySchedulesByPlanId(UUID planId) {
        return List.of();
    }
}
