package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.CategoryDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.CategoryEntity;
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

    private static final double FIVE_KILOMETER = 5000;
    private final PlanManagementRepository planManagementRepository;
    private final PlanDataMapper planDataMapper;

    public PlanManagementQueryServiceImpl(PlanManagementRepository planManagementRepository, PlanDataMapper planDataMapper) {
        this.planManagementRepository = planManagementRepository;
        this.planDataMapper = planDataMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getActivePlans(int page, int size) {
        return planManagementRepository.findActivePlans(page, size)
                                       .stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanResponseDto> getPlanByTitleAndCode(String title, String code) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansByCategoryId(UUID categoryId, int page, int size) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansByDate(LocalDate start, LocalDate end, int page, int size) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansByUserId(UUID userId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansNearMe(double latitude, double longitude, int page, int size) {
        return planManagementRepository.findPlansNearMe(latitude, longitude, FIVE_KILOMETER, page, size)
                                       .stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeliverySchedule> getDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end) {
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanResponseDto> getPlanByPlanId(UUID planId) {
        return Optional.empty();
    }
}
