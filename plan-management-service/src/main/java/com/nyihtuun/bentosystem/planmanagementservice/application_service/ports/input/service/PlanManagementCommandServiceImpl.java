package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.CategoryDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.JobRunRepository;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunStatus;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.PlanMeal;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.GenerateSchedulesResult;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PeriodContext;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.PLAN_ERROR;
import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.toKey;

@Slf4j
@Service
public class PlanManagementCommandServiceImpl implements PlanManagementCommandService {

    private static final String DELIVERY_SCHEDULE_JOB_NAME = "GENERATING DELIVERY SCHEDULE";
    public static final String PLAN_ID = "planId";
    public static final String SUCCESS = "success";
    public static final String DELIVERY_SCHEDULE_ID = "deliveryScheduleId";
    public static final String ERROR_CODE = "errorCode";
    public static final String MESSAGE_KEY = "messageKey";
    public static final String MESSAGE = "message";

    private final PlanDataMapper planDataMapper;
    private final PlanManagementRepository planManagementRepository;
    private final PlanManagementDomainService planManagementDomainService;
    private final BusinessCalendarService businessCalendarService;
    private final MessageSource messageSource;
    private final JobRunRepository jobRunRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PlanManagementCommandServiceImpl(PlanDataMapper planDataMapper, PlanManagementRepository planManagementRepository,
                                            PlanManagementDomainService planManagementDomainService,
                                            BusinessCalendarService businessCalendarService, MessageSource messageSource,
                                            JobRunRepository jobRunRepository, ObjectMapper objectMapper) {
        this.planDataMapper = planDataMapper;
        this.planManagementRepository = planManagementRepository;
        this.planManagementDomainService = planManagementDomainService;
        this.businessCalendarService = businessCalendarService;
        this.messageSource = messageSource;
        this.jobRunRepository = jobRunRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public PlanResponseDto validateAndInitiatePlan(PlanRequestDto planRequestDto, UserId userId) {
        log.info("Validating and initiating plan for plan: {}", planRequestDto);
        Plan plan = planDataMapper.mapPlanDtoToPlan(planRequestDto);
        plan.validatePlan();
        plan.initializePlan(userId);
        log.info("Plan with id: {} is validated and initiated", plan.getId().getValue());

        Plan savedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(savedPlan);
    }

    @Override
    @Transactional
    public PlanResponseDto validateAndUpdatePlanInfo(PlanId planId, PlanRequestDto planRequestDto) {
        log.info("Validating and updating plan with id: {} with plan: {}", planId, planRequestDto);
        PlanUpdateCommand planUpdateCommand = planDataMapper.mapPlanDtoToPlanUpdateCommand(planRequestDto);

        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));
        plan.updatePlan(planUpdateCommand);
        plan.validatePlan();
        log.info("Plan with id: {} is updated and validated", planId.getValue());

        Plan updatedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(updatedPlan);
    }

    @Override
    @Transactional
    public void deletePlan(PlanId planId) {
        log.info("Deleting plan with id: {}", planId);
        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));
        plan.deletePlan();
        log.info("Plan with id: {} is deleted", planId.getValue());
        persistPlan(plan);
    }

    @Override
    @Transactional
    public PlanResponseDto reflectUserSubscription(PlanId planId,
                                                   List<PlanMealId> appliedPlanMealIds,
                                                   List<PlanMealId> unappliedPlanMealIds) {
        log.info("Reflecting user subscription for plan with id: {}", planId);
        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));
        plan.reflectMealSelection(appliedPlanMealIds, unappliedPlanMealIds);
        plan.updatePlanStatus();
        log.info("Plan with id: {} is reflected with new user selections", planId.getValue());

        Plan updatedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(updatedPlan);
    }

    @Override
    @Transactional
    public PlanResponseDto addMealToPlan(PlanId planId, PlanMealRequestDto planMealRequestDto) {
        log.info("Adding meal to plan with id: {}", planId);
        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));
        PlanMeal planMeal = planDataMapper.mapPlanMealRequestDtoToPlanMeal(planMealRequestDto);
        plan.addMeal(planMeal);
        plan.updatePlanStatus();
        log.info("Plan with id: {} is added to meals", planId.getValue());

        Plan updatedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(updatedPlan);
    }

    @Override
    @Transactional
    public PlanResponseDto removeMealFromPlan(PlanId planId, PlanMealId planMealId) {
        log.info("Removing meal with id:{} from plan with id: {}", planMealId, planId);
        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));
        plan.removeMeal(planMealId);
        plan.updatePlanStatus();
        log.info("Plan Meal with id: {} is removed from Plan with id: {}", planMealId, planId.getValue());

        Plan updatedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(updatedPlan);
    }

    @Override
    @Transactional
    public PlanResponseDto updateMealFromPlan(PlanId planId, PlanMealId planMealId, PlanMealRequestDto planMealRequestDto) {
        log.info("Updating meal with id: {} from plan with id: {}", planMealId, planId);
        Plan plan = planManagementRepository.findByPlanId(planId.getValue())
                                            .orElseThrow(() -> new PlanManagementDomainException(
                                                    PlanManagementErrorCode.INVALID_PLAN_ID));

        PlanMealUpdateCommand planMealUpdateCommand = planDataMapper.mapPlanMealDtoToPlanMealUpdateCommand(planMealRequestDto);
        plan.updateMeal(planMealId, planMealUpdateCommand);
        plan.updatePlanStatus();
        log.info("Plan Meal with id: {} is updated", planMealId.getValue());

        Plan updatedPlan = persistPlan(plan);
        return planDataMapper.mapPlanToPlanDto(updatedPlan);
    }

    private Plan persistPlan(Plan plan) {
        Plan savedPlan = planManagementRepository.save(plan);
        log.info("Plan with id: {} is persisted", plan.getId().getValue());
        return savedPlan;
    }

    @Override
    @Transactional
    public List<DeliverySchedule> generateSchedules() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        log.info("Generating schedules at [{}]", formattedNow);

        PeriodContext currentMonthBusinessPeriod = businessCalendarService.getCurrentMonthBusinessPeriod(YearMonth.now());

        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = today.with(TemporalAdjusters.lastDayOfMonth());

        List<Plan> activePlansBetweenDates = planManagementRepository.findActivePlansBetweenDates(startDate, endDate);

        List<DeliverySchedule> deliverySchedules = new ArrayList<>();

        UUID jobRunId = jobRunRepository.startRun(
                DELIVERY_SCHEDULE_JOB_NAME,
                startDate,
                endDate,
                now
        );

        ArrayNode successfulResults = objectMapper.createArrayNode();
        ArrayNode failedResults = objectMapper.createArrayNode();

        for (Plan plan : activePlansBetweenDates) {
            log.info("Generating schedule for plan with id: {}", plan.getId().getValue());
            GenerateSchedulesResult generateSchedulesResult = planManagementDomainService.generateSchedules(plan,
                                                                                                            currentMonthBusinessPeriod);

            if (generateSchedulesResult.isSuccess()) {
                DeliverySchedule deliverySchedule = generateSchedulesResult.getDeliverySchedule();
                log.info("Schedule for Plan with id: {} is successfully generated with id: {}",
                         plan.getId().getValue(),
                         deliverySchedule.getId().getValue());

                deliverySchedules.add(deliverySchedule);
                planManagementRepository.saveDeliverySchedule(deliverySchedule);
                log.info("Delivery Schedule with id:{} for Plan with id: {} is persisted",
                         deliverySchedule.getId().getValue(),
                         plan.getId().getValue());

                ObjectNode result = objectMapper.createObjectNode();
                result.put(PLAN_ID, plan.getId().getValue().toString());
                result.put(SUCCESS, true);
                result.put(DELIVERY_SCHEDULE_ID, deliverySchedule.getId().getValue().toString());
                successfulResults.add(result);

            } else {
                String messageKey = PLAN_ERROR + toKey(generateSchedulesResult.getErrorCode().name());
                String message =
                        messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());


                ObjectNode result = objectMapper.createObjectNode();
                result.put(PLAN_ID, plan.getId().getValue().toString());
                result.put(SUCCESS, false);
                result.put(ERROR_CODE, generateSchedulesResult.getErrorCode().name());
                result.put(MESSAGE_KEY, messageKey);
                result.put(MESSAGE, message);
                failedResults.add(result);

                log.warn("Schedule generation failed for planId={}, errorCode={}",
                         plan.getId().getValue(),
                         generateSchedulesResult.getErrorCode().name());
            }
        }
        // if the list of all the successfully generated schedules equal to the list of available plans
        // then, status is SUCCESS, if reverse is true, status is FAILED
        // if both cases are not true, status is PARTIAL_SUCCESS
        JobRunStatus status = successfulResults.size() == activePlansBetweenDates.size() ?
                JobRunStatus.SUCCESS : failedResults.size() == activePlansBetweenDates.size() ?
                JobRunStatus.FAILED : JobRunStatus.PARTIAL_SUCCESS;

        int successCount = successfulResults.size();
        int failureCount = failedResults.size();

        jobRunRepository.finishRun(jobRunId,
                                   status,
                                   activePlansBetweenDates.size(),
                                   successCount,
                                   failureCount,
                                   successfulResults,
                                   failedResults,
                                   LocalDateTime.now());

        return deliverySchedules;
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryDto == null || categoryDto.getName().isBlank())
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_CATEGORY_NAME);

        try {
            Category savedCategory = planManagementRepository.saveCategory(new Category(new CategoryId(UUID.randomUUID()),
                                                                               categoryDto.getName()));
            return new CategoryDto(savedCategory.getId().getValue(), savedCategory.getName());
        } catch (Exception e) {
            if (e instanceof DataIntegrityViolationException)
                throw new PlanManagementDomainException(PlanManagementErrorCode.DUPLICATED_CATEGORY_NAME);
            throw e;
        }
    }
}
