package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.service;

import com.nyihtuun.bentosystem.domain.dto.CategoryDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.security.authorization_handler.AdminOrProviderAccessDeniedAuthorizationHandler;
import com.nyihtuun.bentosystem.planmanagementservice.security.authorization_handler.GenericAccessDeniedAuthorizationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
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
        return planManagementRepository.findPlanByTitleAndCode(title, code)
                                       .map(planDataMapper::mapPlanToPlanDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansByCategoryId(UUID categoryId, int page, int size) {
        return planManagementRepository.findPlansByCategory(categoryId, page, size)
                                       .stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getPlansByDate(LocalDate start, LocalDate end, int page, int size) {
        return planManagementRepository.findActivePlansBetweenDates(start, end, page, size)
                                       .stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("principal.toString() == #userId.toString()")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public List<PlanResponseDto> getPlansByUserId(UUID userId) {
        return planManagementRepository.findPlansByUserId(userId)
                                       .stream()
                                       .map(planDataMapper::mapPlanToPlanDto)
                                       .toList();
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
        return planManagementRepository.findAllCategories()
                                       .stream()
                                       .map(category -> new CategoryDto(category.getId().getValue(), category.getName()))
                                       .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeliverySchedule> getDeliverySchedulesByPlanIdAndDate(UUID planId, LocalDate start, LocalDate end) {
        return planManagementRepository.findDeliverySchedulesByPlanIdAndDate(planId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanResponseDto> getPlanByPlanId(UUID planId) {
        return planManagementRepository.findByPlanId(planId)
                .map(planDataMapper::mapPlanToPlanDto);
    }
}
