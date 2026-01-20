package com.nyihtuun.bentosystem.planmanagementservice;

import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.AddressDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.CategoryDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanMealResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.BusinessCalendarService;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.JobRunRepository;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunStatus;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Category;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.PlanMeal;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PeriodContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class PlanManagementServiceTest {

    private static final UUID USER_ID_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID CATEGORY_ID_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID DUMMY_CATEGORY_ID_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222223");

    private static final UUID PLAN_ID_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID PLAN_MEAL_ID_UUID1 =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID PLAN_MEAL_ID_UUID2 =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID JOB_RUN_ID_UUID =
            UUID.fromString("84444444-8444-8444-8444-844444444444");

    private static final BigDecimal INITIAL_DISPLAY_SUBFEE = new BigDecimal("2000.00");
    private static final BigDecimal NEW_PLANMEAL_PRICE = new BigDecimal("800.00");
    private static final BigDecimal REMOVED_PLANMEAL_PRICE = new BigDecimal("800.00");
    private static final BigDecimal ORIGINAL_PLANMEAL1_PRICE = new BigDecimal("1200.00");

    @Autowired
    private PlanManagementCommandService planManagementCommandService;

    @Autowired
    private PlanManagementQueryService planManagementQueryService;

    @Autowired
    private PlanDataMapper planDataMapper;

    @MockitoBean
    private PlanManagementRepository planManagementRepository;

    @MockitoBean
    private JobRunRepository jobRunRepository;

    @MockitoBean
    private BusinessCalendarService businessCalendarService;

    private PlanRequestDto planRequestDto;
    private PlanRequestDto noMealPlan;
    private PlanRequestDto noPrimaryMealPlan;
    private PlanRequestDto invalidSubscriptionFeePlan;
    private PlanRequestDto invalidSkipDaysPlan;
    private List<PlanMealRequestDto> planMealRequestDtos;
    private PlanMealRequestDto validPlanMealRequestDto;
    private PlanMealRequestDto negativePricePlanMealRequestDto;
    private PlanMealRequestDto negativeMinSubCountPlanMealRequestDto;
    private UserId userId;
    private Category category;
    private Plan dummyPlan;
    private PlanId dummyPlanId = new PlanId(PLAN_ID_UUID);
    private PlanMealId dummyPlanMealId1 = new PlanMealId(PLAN_MEAL_ID_UUID1);
    private PlanMealId dummyPlanMealId2 = new PlanMealId(PLAN_MEAL_ID_UUID2);
    private PlanMeal dummyPlanMeal1;
    private PlanMeal dummyPlanMeal2;
    private List<PlanMeal> dummyPlanMeals;
    private PeriodContext periodContext;
    private LocalDate startDate = LocalDate.of(2026, 1, 1);
    private LocalDate endDate = LocalDate.of(2026, 1, 31);

    @BeforeEach
    void setUp() {
        userId = new UserId(USER_ID_UUID);

        planMealRequestDtos = new ArrayList<>();

        PlanMealRequestDto planMealRequestDto1 = PlanMealRequestDto.builder()
                .name("Standard Bento")
                .pricePerMonth(new BigDecimal("800.00"))
                .minSubCount(10)
                .primary(true)
                .imageUrl("https://example.com/bento-standard.jpg")
                .description("Standard healthy bento meal")
                .build();

        PlanMealRequestDto planMealRequestDto2 = PlanMealRequestDto.builder()
                .name("Premium Bento")
                .pricePerMonth(new BigDecimal("1200.00"))
                .minSubCount(5)
                .primary(false)
                .imageUrl("https://example.com/bento-premium.jpg")
                .description("Premium bento with higher quality ingredients")
                .build();

        planMealRequestDtos.add(planMealRequestDto1);
        planMealRequestDtos.add(planMealRequestDto2);

        planRequestDto = PlanRequestDto.builder()
                .title("Healthy Lunch Plan")
                .description("Weekly healthy lunch subscription")
                .categoryIds(Set.of(CATEGORY_ID_UUID))
                .skipDays(new ArrayList<>())
                .planMealRequestDtos(planMealRequestDtos)
                .address(AddressDto.builder()
                                   .prefecture("Tokyo")
                                   .city("Shibuya")
                                   .district("Jingumae")
                                   .chomeBanGo("1-2-3")
                                   .buildingNameRoomNo("Building 101")
                                   .postalCode("150-0001")
                                   .location(GeoPoint.of(
                                35.658034,
                                139.701636
                        ))
                                   .build())
                .displaySubscriptionFee(new BigDecimal("2000.00"))
                .build();

        category = new Category(
          new CategoryId(CATEGORY_ID_UUID),
          "HEALTHY"
        );

        // given: invalid plan request (no meals)
        noMealPlan = PlanRequestDto.builder()
                .title("Healthy Lunch Plan")
                .description("Weekly healthy lunch subscription")
                .categoryIds(Set.of(CATEGORY_ID_UUID))
                .skipDays(new ArrayList<>())
                .planMealRequestDtos(new ArrayList<>())
                .address(AddressDto.builder()
                        .prefecture("Tokyo")
                        .city("Shibuya")
                        .district("Jingumae")
                        .chomeBanGo("1-2-3")
                        .buildingNameRoomNo("Building 101")
                        .postalCode("150-0001")
                        .location(GeoPoint.of(35.658034, 139.701636))
                        .build())
                .displaySubscriptionFee(new BigDecimal("2000.00"))
                .build();

        // given: invalid plan request (no primary meal)
        List<PlanMealRequestDto> noPrimaryMealRequestDtos = new ArrayList<>();

        PlanMealRequestDto noPrimaryMeal1 = PlanMealRequestDto.builder()
                .name("Standard Bento")
                .pricePerMonth(new BigDecimal("800.00"))
                .minSubCount(10)
                .primary(false)
                .imageUrl("https://example.com/bento-standard.jpg")
                .description("Standard healthy bento meal")
                .build();

        PlanMealRequestDto noPrimaryMeal2 = PlanMealRequestDto.builder()
                .name("Premium Bento")
                .pricePerMonth(new BigDecimal("1200.00"))
                .minSubCount(5)
                .primary(false)
                .imageUrl("https://example.com/bento-premium.jpg")
                .description("Premium bento with higher quality ingredients")
                .build();

        noPrimaryMealRequestDtos.add(noPrimaryMeal1);
        noPrimaryMealRequestDtos.add(noPrimaryMeal2);

        noPrimaryMealPlan = PlanRequestDto.builder()
                .title("Healthy Lunch Plan")
                .description("Weekly healthy lunch subscription")
                .categoryIds(Set.of(CATEGORY_ID_UUID))
                .skipDays(new ArrayList<>())
                .planMealRequestDtos(noPrimaryMealRequestDtos)
                .address(AddressDto.builder()
                        .prefecture("Tokyo")
                        .city("Shibuya")
                        .district("Jingumae")
                        .chomeBanGo("1-2-3")
                        .buildingNameRoomNo("Building 101")
                        .postalCode("150-0001")
                        .location(GeoPoint.of(35.658034, 139.701636))
                        .build())
                .displaySubscriptionFee(new BigDecimal("2000.00"))
                .build();

        // given: invalid plan request (invalid subscription fee)
        invalidSubscriptionFeePlan = PlanRequestDto.builder()
                .title("Healthy Lunch Plan")
                .description("Weekly healthy lunch subscription")
                .categoryIds(Set.of(CATEGORY_ID_UUID))
                .skipDays(new ArrayList<>())
                .planMealRequestDtos(planMealRequestDtos)
                .address(AddressDto.builder()
                        .prefecture("Tokyo")
                        .city("Shibuya")
                        .district("Jingumae")
                        .chomeBanGo("1-2-3")
                        .buildingNameRoomNo("Building 101")
                        .postalCode("150-0001")
                        .location(GeoPoint.of(35.658034, 139.701636))
                        .build())
                .displaySubscriptionFee(new BigDecimal("2100.00"))
                .build();

        // given: invalid plan request (invalid subscription fee)
        invalidSkipDaysPlan = PlanRequestDto.builder()
                                                   .title("Healthy Lunch Plan")
                                                   .description("Weekly healthy lunch subscription")
                                                   .categoryIds(Set.of(CATEGORY_ID_UUID))
                                                   .skipDays(List.of(LocalDate.of(2025, 1, 13),
                                                                     LocalDate.of(2025, 1, 14),
                                                                     LocalDate.of(2025, 1, 15)))
                                                   .planMealRequestDtos(planMealRequestDtos)
                                                   .address(AddressDto.builder()
                                                                      .prefecture("Tokyo")
                                                                      .city("Shibuya")
                                                                      .district("Jingumae")
                                                                      .chomeBanGo("1-2-3")
                                                                      .buildingNameRoomNo("Building 101")
                                                                      .postalCode("150-0001")
                                                                      .location(GeoPoint.of(35.658034, 139.701636))
                                                                      .build())
                                                   .displaySubscriptionFee(new BigDecimal("2000.00"))
                                                   .build();

         dummyPlanMeal1 = PlanMeal.builder()
                .planMealId(dummyPlanMealId1)
                .planId(dummyPlanId)
                .name("Standard Bento")
                .description("Standard daily bento")
                .pricePerMonth(new Money(ORIGINAL_PLANMEAL1_PRICE))
                .isPrimary(true)
                .minSubCount(new Threshold(1))
                .currentSubCount(0)
                .imageUrl("https://example.com/standard.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

         dummyPlanMeal2 = PlanMeal.builder()
                .planMealId(dummyPlanMealId2)
                .planId(dummyPlanId)
                .name("Premium Bento")
                .description("Premium ingredients bento")
                .pricePerMonth(new Money(REMOVED_PLANMEAL_PRICE))
                .isPrimary(false)
                .minSubCount(new Threshold(5))
                .currentSubCount(0)
                .imageUrl("https://example.com/premium.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        dummyPlanMeals = new ArrayList<>(Arrays.asList(dummyPlanMeal1, dummyPlanMeal2));

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.RECRUITING)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(new ArrayList<>())
                        .address(Address.builder()
                        .prefecture("Osaka")
                        .city("Osaka")
                        .district("NishiKu,Honden")
                        .chomeBanGo("3-2-1")
                        .buildingNameRoomNo("UpdatedBuilding 1001")
                        .postalCode("550-0022")
                        .location(GeoPoint.of(65.658034, 109.701636))
                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();

         validPlanMealRequestDto = PlanMealRequestDto.builder()
                                                                   .name("Valid Test Plan Meal")
                                                                   .pricePerMonth(NEW_PLANMEAL_PRICE)
                                                                   .minSubCount(10)
                                                                   .primary(true)
                                                                   .imageUrl("https://example.com/bento-standard.jpg")
                                                                   .description("It is for testing")
                                                                   .build();

         negativePricePlanMealRequestDto = PlanMealRequestDto.builder()
                                                                   .name("Test Plan Meal")
                                                                   .pricePerMonth(new BigDecimal("-1200.00"))
                                                                   .minSubCount(5)
                                                                   .primary(false)
                                                                   .imageUrl("https://example.com/bento-premium.jpg")
                                                                   .description("It is for testing")
                                                                   .build();

        negativeMinSubCountPlanMealRequestDto = PlanMealRequestDto.builder()
                                                                  .name("Test Plan Meal")
                                                                  .pricePerMonth(new BigDecimal("1200.00"))
                                                                  .minSubCount(-5)
                                                                  .primary(false)
                                                                  .imageUrl("https://example.com/bento-premium.jpg")
                                                                  .description("It is for testing")
                                                                  .build();

        List<LocalDate> businessDays = new ArrayList<>();
        businessDays.add(LocalDate.of(2026, 1, 5));
        businessDays.add(LocalDate.of(2026, 1, 6));
        businessDays.add(LocalDate.of(2026, 1, 7));

        periodContext = new PeriodContext(startDate, endDate, businessDays);

        when(planManagementRepository.findActivePlans(anyInt(), anyInt()))
            .thenAnswer(inv -> dummyPlan == null ? List.of() : List.of(dummyPlan));
        when(planManagementRepository.save(any(Plan.class), anyBoolean())).thenAnswer(inv -> inv.getArgument(0));
        when(planManagementRepository.findByPlanId(any(UUID.class)))
            .thenAnswer(inv -> Optional.ofNullable(dummyPlan));
        when(planManagementRepository.findActivePlansBetweenDates(any(LocalDate.class), any(LocalDate.class)))
            .thenAnswer(inv -> dummyPlan == null ? List.of() : List.of(dummyPlan));
        when(planManagementRepository.saveCategory(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(businessCalendarService.getCurrentMonthBusinessPeriod(any(YearMonth.class)))
                .thenAnswer(inv -> periodContext);
        when(jobRunRepository.startRun(any(String.class),any(LocalDate.class), any(LocalDate.class), any(LocalDateTime.class)))
                .thenAnswer(inv -> JOB_RUN_ID_UUID);

        doNothing().when(jobRunRepository).finishRun(
                eq(JOB_RUN_ID_UUID),
                eq(JobRunStatus.SUCCESS),
                eq(1),
                eq(1),
                eq(0),
                any(),
                any(),
                any(LocalDateTime.class)
        );
    }

    @Test
    void testValidateAndInitiatePlan() {
        // when
        PlanResponseDto planResponseDto = planManagementCommandService.validateAndInitiatePlan(planRequestDto, userId);

        // then
        ArgumentCaptor<Plan> planArgumentCaptor = ArgumentCaptor.forClass(Plan.class);
        verify(planManagementRepository).save(planArgumentCaptor.capture(), anyBoolean());

        Plan savedPlan = planArgumentCaptor.getValue();

        assertNotNull(savedPlan);
        assertNotNull(planResponseDto.getPlanId()); // assert id is not null

        // assert code
        String code = planResponseDto.getCode();
        assertEquals(7, code.length());
        // first two characters: alphabets (A–Z or a–z)
        assertTrue(
                Character.isLetter(code.charAt(0)),
                "First character should be a letter"
        );
        assertTrue(
                Character.isLetter(code.charAt(1)),
                "Second character should be a letter"
        );
        // remaining five characters: digits (0–9)
        for (int i = 2; i < 7; i++) {
            assertTrue(
                    Character.isDigit(code.charAt(i)),
                    "Character at index " + i + " should be a digit"
            );
        }

        // assert status
        assertEquals(PlanStatus.RECRUITING, planResponseDto.getStatus());

        // assert category
        assertEquals(category.getId().getValue(), planResponseDto.getCategoryIds().stream().toList().getFirst());

        // assert plan meals
        assertEquals(2, planResponseDto.getPlanMealResponseDtos().size());
        assertEquals(planResponseDto.getPlanId(), planResponseDto.getPlanMealResponseDtos().getFirst().getPlanId());
        assertEquals(planResponseDto.getPlanId(), planResponseDto.getPlanMealResponseDtos().getLast().getPlanId());
        assertEquals(0, planResponseDto.getPlanMealResponseDtos().getFirst().getCurrentSubCount());
        assertEquals(0, planResponseDto.getPlanMealResponseDtos().getLast().getCurrentSubCount());
    }

    @Test
    void testValidateAndInitiatePlan_noMeal_shouldThrowAndNotPersist() {
        // when + then
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.validateAndInitiatePlan(
                                                                                           noMealPlan,
                                                                                           userId));
        assertEquals(PlanManagementErrorCode.EMPTY_MEALS, planManagementDomainException.getErrorCode());
        // must not persist anything when validation fails
        verify(planManagementRepository, never()).save(any(Plan.class), anyBoolean());
    }

    @Test
    void testValidateAndInitiatePlan_noPrimaryMeal_shouldThrowAndNotPersist() {
        // when + then
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.validateAndInitiatePlan(
                                                                                           noPrimaryMealPlan,
                                                                                           userId));
        assertEquals(PlanManagementErrorCode.NO_PRIMARY_MEAL, planManagementDomainException.getErrorCode());
        // must not persist anything when validation fails
        verify(planManagementRepository, never()).save(any(Plan.class), anyBoolean());
    }

    @Test
    void testValidateAndInitiatePlan_invalidSubFee_shouldThrowAndNotPersist() {
        // when + then
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.validateAndInitiatePlan(
                                                                                           invalidSubscriptionFeePlan,
                                                                                           userId));
        assertEquals(PlanManagementErrorCode.INVALID_SUB_FEE, planManagementDomainException.getErrorCode());
        // must not persist anything when validation fails
        verify(planManagementRepository, never()).save(any(Plan.class), anyBoolean());
    }

    @Test
    void testValidateAndInitiatePlan_invalidSkipDays_shouldThrowAndNotPersist() {
        // when + then
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.validateAndInitiatePlan(
                                                                                           invalidSkipDaysPlan,
                                                                                           userId));
        assertEquals(PlanManagementErrorCode.INVALID_SKIPDAYS, planManagementDomainException.getErrorCode());
        // must not persist anything when validation fails
        verify(planManagementRepository, never()).save(any(Plan.class), anyBoolean());
    }

    @Test
    void testValidateAndUpdatePlanInfo() {
        // when
        PlanResponseDto updated = planManagementCommandService.validateAndUpdatePlanInfo(dummyPlanId, planRequestDto);

        // assert plan information
        assertNotNull(updated);
        assertEquals(dummyPlanId.getValue(), updated.getPlanId());
        assertEquals("Healthy Lunch Plan", updated.getTitle());
        assertEquals("Weekly healthy lunch subscription", updated.getDescription());
        assertEquals(CATEGORY_ID_UUID, updated.getCategoryIds().stream().toList().getFirst());
        assertEquals("Tokyo", updated.getAddress().getPrefecture());
        assertEquals("Shibuya", updated.getAddress().getCity());
        assertEquals("Jingumae", updated.getAddress().getDistrict());
        assertEquals("1-2-3", updated.getAddress().getChomeBanGo());
        assertEquals("Building 101", updated.getAddress().getBuildingNameRoomNo());
        assertEquals("150-0001", updated.getAddress().getPostalCode());
        assertEquals(35.658034, updated.getAddress().getLocation().getLatitude());
        assertEquals(139.701636, updated.getAddress().getLocation().getLongitude());
    }

    @Test
    void testValidateAndUpdatePlan_invalidSubFee_shouldThrowAndNotPersist() {
        // when + then
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.validateAndUpdatePlanInfo(
                                                                                           dummyPlanId,
                                                                                           invalidSubscriptionFeePlan));
        assertEquals(PlanManagementErrorCode.INVALID_SUB_FEE, planManagementDomainException.getErrorCode());
        // must not persist anything when validation fails
        verify(planManagementRepository, never()).save(any(Plan.class), anyBoolean());
    }

    @Test
    void testReflectUserSubscriptionSubscribed_statusChanged() {
        // test data : primaryPlanMeal threshold = 1, non-primaryPlanMeal = 5
        PlanResponseDto planResponseDto = planManagementCommandService.reflectUserSubscription(dummyPlanId,
                                                                                               List.of(dummyPlanMealId1, dummyPlanMealId2),
                                                                                               Collections.emptyList());
        for (PlanMealResponseDto planMealResponseDto : planResponseDto.getPlanMealResponseDtos()) {
            assertEquals(1, planMealResponseDto.getCurrentSubCount());
        }
        assertEquals(PlanStatus.ACTIVE, planResponseDto.getStatus());
    }

    @Test
    void testReflectUserSubscriptionSubscribed_statusUnchanged() {
        // test data : primaryPlanMeal threshold = 2, non-primaryPlanMeal = 5
         dummyPlanMeal1 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId1)
                                          .planId(dummyPlanId)
                                          .name("Standard Bento")
                                          .description("Standard daily bento")
                                          .pricePerMonth(new Money(new BigDecimal("1200.00")))
                                          .isPrimary(true)
                                          .minSubCount(new Threshold(2))
                                          .currentSubCount(0)
                                          .imageUrl("https://example.com/standard.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

         dummyPlanMeal2 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId2)
                                          .planId(dummyPlanId)
                                          .name("Premium Bento")
                                          .description("Premium ingredients bento")
                                          .pricePerMonth(new Money(new BigDecimal("800.00")))
                                          .isPrimary(false)
                                          .minSubCount(new Threshold(5))
                                          .currentSubCount(0)
                                          .imageUrl("https://example.com/premium.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        List<PlanMeal> dummyPlanMeals = new ArrayList<>();
        dummyPlanMeals.add(dummyPlanMeal1);
        dummyPlanMeals.add(dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.RECRUITING)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(new ArrayList<>())
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();
        
        PlanResponseDto planResponseDto = planManagementCommandService.reflectUserSubscription(dummyPlanId,
                                                                                               List.of(dummyPlanMealId1, dummyPlanMealId2),
                                                                                               Collections.emptyList());
        for (PlanMealResponseDto planMealResponseDto : planResponseDto.getPlanMealResponseDtos()) {
            assertEquals(1, planMealResponseDto.getCurrentSubCount());
        }
        assertEquals(PlanStatus.RECRUITING, planResponseDto.getStatus());
    }

    @Test
    void testReflectUserSubscriptionUnsubscribed_statusChanged() {
        // test data : primaryPlanMeal threshold = 1, non-primaryPlanMeal = 5
        PlanMeal dummyPlanMeal1 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId1)
                                          .planId(dummyPlanId)
                                          .name("Standard Bento")
                                          .description("Standard daily bento")
                                          .pricePerMonth(new Money(new BigDecimal("1200.00")))
                                          .isPrimary(true)
                                          .minSubCount(new Threshold(1))
                                          .currentSubCount(1)
                                          .imageUrl("https://example.com/standard.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        PlanMeal dummyPlanMeal2 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId2)
                                          .planId(dummyPlanId)
                                          .name("Premium Bento")
                                          .description("Premium ingredients bento")
                                          .pricePerMonth(new Money(new BigDecimal("800.00")))
                                          .isPrimary(false)
                                          .minSubCount(new Threshold(5))
                                          .currentSubCount(0)
                                          .imageUrl("https://example.com/premium.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        List<PlanMeal> dummyPlanMeals = new ArrayList<>();
        dummyPlanMeals.add(dummyPlanMeal1);
        dummyPlanMeals.add(dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .skipDays(new ArrayList<>())
                        .providerUserId(userId)
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(new BigDecimal("2000.00")))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();
        PlanResponseDto planResponseDto = planManagementCommandService.reflectUserSubscription(dummyPlanId,
                                                                                               Collections.emptyList(),
                                                                                               List.of(dummyPlanMealId1, dummyPlanMealId2));
        for (PlanMealResponseDto planMealResponseDto : planResponseDto.getPlanMealResponseDtos()) {
            assertEquals(0, planMealResponseDto.getCurrentSubCount());
        }
        assertEquals(PlanStatus.SUSPENDED, planResponseDto.getStatus());
    }

    @Test
    void testReflectUserSubscriptionUnsubscribed_statusUnchanged() {
        // test data : primaryPlanMeal threshold = 1, non-primaryPlanMeal = 5
        PlanMeal dummyPlanMeal1 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId1)
                                          .planId(dummyPlanId)
                                          .name("Standard Bento")
                                          .description("Standard daily bento")
                                          .pricePerMonth(new Money(new BigDecimal("1200.00")))
                                          .isPrimary(true)
                                          .minSubCount(new Threshold(1))
                                          .currentSubCount(2)
                                          .imageUrl("https://example.com/standard.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        PlanMeal dummyPlanMeal2 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId2)
                                          .planId(dummyPlanId)
                                          .name("Premium Bento")
                                          .description("Premium ingredients bento")
                                          .pricePerMonth(new Money(new BigDecimal("800.00")))
                                          .isPrimary(false)
                                          .minSubCount(new Threshold(5))
                                          .currentSubCount(2)
                                          .imageUrl("https://example.com/premium.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        List<PlanMeal> dummyPlanMeals = new ArrayList<>();
        dummyPlanMeals.add(dummyPlanMeal1);
        dummyPlanMeals.add(dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .skipDays(new ArrayList<>())
                        .providerUserId(userId)
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(new BigDecimal("2000.00")))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();

        PlanResponseDto planResponseDto = planManagementCommandService.reflectUserSubscription(dummyPlanId,
                                                                                               Collections.emptyList(),
                                                                                               List.of(dummyPlanMealId1, dummyPlanMealId2));
        for (PlanMealResponseDto planMealResponseDto : planResponseDto.getPlanMealResponseDtos()) {
            assertEquals(1, planMealResponseDto.getCurrentSubCount());
        }
        assertEquals(PlanStatus.ACTIVE, planResponseDto.getStatus());
    }

    @Test
    void testReflectUserSubscription_invalidId_shouldThrow() {
        when(planManagementRepository.findByPlanId(any())).thenReturn(Optional.empty());
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService
                                                                                           .reflectUserSubscription(dummyPlanId,
                                                                                                                    List.of(dummyPlanMealId1, dummyPlanMealId2),
                                                                                                                    Collections.emptyList()));
        assertEquals(PlanManagementErrorCode.INVALID_PLAN_ID, planManagementDomainException.getErrorCode());

    }

    @Test
    void testAddMealToPlan() {
        PlanResponseDto planResponseDto = planManagementCommandService.addMealToPlan(dummyPlanId, validPlanMealRequestDto);
        PlanMealResponseDto planMealResponseDto = planResponseDto.getPlanMealResponseDtos().stream()
                .filter(planMeal -> "Valid Test Plan Meal".equals(planMeal.getName()))
                .findFirst().orElse(null);

        assertEquals(3, planResponseDto.getPlanMealResponseDtos().size());
        assertNotNull(planResponseDto);
        assertNotNull(planMealResponseDto);
        assertEquals(0, planMealResponseDto.getCurrentSubCount());
        assertEquals(INITIAL_DISPLAY_SUBFEE.add(NEW_PLANMEAL_PRICE), planResponseDto.getDisplaySubscriptionFee());
    }

    @Test
    void testAddMealToPlan_statusChanged() {
        PlanMeal dummyPlanMeal1 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId1)
                                          .planId(dummyPlanId)
                                          .name("Standard Bento")
                                          .description("Standard daily bento")
                                          .pricePerMonth(new Money(new BigDecimal("1200.00")))
                                          .isPrimary(true)
                                          .minSubCount(new Threshold(1))
                                          .currentSubCount(1)
                                          .imageUrl("https://example.com/standard.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        PlanMeal dummyPlanMeal2 = PlanMeal.builder()
                                          .planMealId(dummyPlanMealId2)
                                          .planId(dummyPlanId)
                                          .name("Premium Bento")
                                          .description("Premium ingredients bento")
                                          .pricePerMonth(new Money(new BigDecimal("800.00")))
                                          .isPrimary(false)
                                          .minSubCount(new Threshold(5))
                                          .currentSubCount(0)
                                          .imageUrl("https://example.com/premium.jpg")
                                          .createdAt(LocalDateTime.now())
                                          .updatedAt(LocalDateTime.now())
                                          .build();

        List<PlanMeal> dummyPlanMeals = new ArrayList<>();
        dummyPlanMeals.add(dummyPlanMeal1);
        dummyPlanMeals.add(dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .skipDays(new ArrayList<>())
                        .providerUserId(userId)
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(new BigDecimal("2000.00")))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();
        PlanResponseDto planResponseDto = planManagementCommandService.addMealToPlan(dummyPlanId, validPlanMealRequestDto);
        assertEquals(PlanStatus.SUSPENDED, planResponseDto.getStatus());
    }

    @Test
    void testAddMealToPlan_negativePrice_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.addMealToPlan(dummyPlanId, negativePricePlanMealRequestDto));
        assertEquals(PlanManagementErrorCode.NEGATIVE_PLANMEAL_PRICE, planManagementDomainException.getErrorCode());
    }

    @Test
    void testAddMealToPlan_negativeMinSubCount_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.addMealToPlan(dummyPlanId, negativeMinSubCountPlanMealRequestDto));
        assertEquals(PlanManagementErrorCode.NEGATIVE_PLANMEAL_MINSUBCOUNT, planManagementDomainException.getErrorCode());
    }

    @Test
    void testRemoveMealFromPlan() {
        PlanResponseDto planResponseDto = planManagementCommandService.removeMealFromPlan(dummyPlanId, dummyPlanMealId2);
        assertEquals(1, planResponseDto.getPlanMealResponseDtos().size());
        assertEquals(INITIAL_DISPLAY_SUBFEE.subtract(REMOVED_PLANMEAL_PRICE), planResponseDto.getDisplaySubscriptionFee());
    }

    @Test
    void testRemoveMealFromPlan_invalidPlanMealId_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.removeMealFromPlan(dummyPlanId, new PlanMealId(UUID.randomUUID())));
        assertEquals(PlanManagementErrorCode.INVALID_PLANMEAL_ID, planManagementDomainException.getErrorCode());
    }

    @Test
    void testRemoveMealFromPlan_noPrimaryMeal_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.removeMealFromPlan(dummyPlanId, dummyPlanMealId1));
        assertEquals(PlanManagementErrorCode.NO_PRIMARY_MEAL, planManagementDomainException.getErrorCode());
    }

    @Test
    void testUpdateMealFromPlan_statusChanged() {
        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(new ArrayList<>())
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();

        PlanResponseDto planResponseDto = planManagementCommandService.updateMealFromPlan(dummyPlanId, dummyPlanMealId2, validPlanMealRequestDto);
        assertEquals(PlanStatus.SUSPENDED, planResponseDto.getStatus());
        assertEquals(ORIGINAL_PLANMEAL1_PRICE.add(NEW_PLANMEAL_PRICE), planResponseDto.getDisplaySubscriptionFee());
        // assert updated planmeal
        PlanMealResponseDto updatedPlanMealResponseDto = planResponseDto.getPlanMealResponseDtos().stream()
                                                                        .filter(planMealResponseDto -> "Valid Test Plan Meal".equals(
                                                                                planMealResponseDto.getName()))
                                                                        .findFirst()
                                                                        .orElse(null);
        assertNotNull(updatedPlanMealResponseDto);
        assertEquals(NEW_PLANMEAL_PRICE, updatedPlanMealResponseDto.getPricePerMonth());
        assertEquals(10, updatedPlanMealResponseDto.getMinSubCount());
        assertEquals("https://example.com/bento-standard.jpg", updatedPlanMealResponseDto.getImageUrl());
        assertEquals("It is for testing", updatedPlanMealResponseDto.getDescription());
    }

    @Test
    void testUpdateMealFromPlan_statusUnchanged() {
        dummyPlanMeal1 = PlanMeal.builder()
                                 .planMealId(dummyPlanMealId1)
                                 .planId(dummyPlanId)
                                 .name("Standard Bento")
                                 .description("Standard daily bento")
                                 .pricePerMonth(new Money(ORIGINAL_PLANMEAL1_PRICE))
                                 .isPrimary(true)
                                 .minSubCount(new Threshold(1))
                                 .currentSubCount(1)
                                 .imageUrl("https://example.com/standard.jpg")
                                 .createdAt(LocalDateTime.now())
                                 .updatedAt(LocalDateTime.now())
                                 .build();
        dummyPlanMeals = List.of(dummyPlanMeal1, dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(new ArrayList<>())
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .planMeals(dummyPlanMeals)
                        .build();

        validPlanMealRequestDto = PlanMealRequestDto.builder()
                                                    .name("Valid Test Plan Meal")
                                                    .pricePerMonth(NEW_PLANMEAL_PRICE)
                                                    .minSubCount(10)
                                                    .primary(false)
                                                    .imageUrl("https://example.com/bento-standard.jpg")
                                                    .description("It is for testing")
                                                    .build();
        PlanResponseDto planResponseDto = planManagementCommandService.updateMealFromPlan(dummyPlanId, dummyPlanMealId2, validPlanMealRequestDto);
        assertEquals(PlanStatus.ACTIVE, planResponseDto.getStatus());
        assertEquals(ORIGINAL_PLANMEAL1_PRICE.add(NEW_PLANMEAL_PRICE), planResponseDto.getDisplaySubscriptionFee());

        // assert updated planmeal
        PlanMealResponseDto updatedPlanMealResponseDto = planResponseDto.getPlanMealResponseDtos().stream()
                                                                  .filter(planMealResponseDto -> "Valid Test Plan Meal".equals(
                                                                          planMealResponseDto.getName()))
                                                                  .findFirst()
                                                                  .orElse(null);
        assertNotNull(updatedPlanMealResponseDto);
        assertEquals(NEW_PLANMEAL_PRICE, updatedPlanMealResponseDto.getPricePerMonth());
        assertEquals(10, updatedPlanMealResponseDto.getMinSubCount());
        assertEquals("https://example.com/bento-standard.jpg", updatedPlanMealResponseDto.getImageUrl());
        assertEquals("It is for testing", updatedPlanMealResponseDto.getDescription());
    }

    @Test
    void testGenerateSchedules_twoAvailableMeals() {
        // given : business days 5, 6, 7, skipDays : 5
        dummyPlanMeal1 = PlanMeal.builder()
                                 .planMealId(dummyPlanMealId1)
                                 .planId(dummyPlanId)
                                 .name("Standard Bento")
                                 .description("Standard daily bento")
                                 .pricePerMonth(new Money(ORIGINAL_PLANMEAL1_PRICE))
                                 .isPrimary(true)
                                 .minSubCount(new Threshold(1))
                                 .currentSubCount(1)
                                 .imageUrl("https://example.com/standard.jpg")
                                 .createdAt(LocalDateTime.now())
                                 .updatedAt(LocalDateTime.now())
                                 .build();

        dummyPlanMeal2 = PlanMeal.builder()
                                 .planMealId(dummyPlanMealId2)
                                 .planId(dummyPlanId)
                                 .name("Premium Bento")
                                 .description("Premium ingredients bento")
                                 .pricePerMonth(new Money(REMOVED_PLANMEAL_PRICE))
                                 .isPrimary(false)
                                 .minSubCount(new Threshold(5))
                                 .currentSubCount(5)
                                 .imageUrl("https://example.com/premium.jpg")
                                 .createdAt(LocalDateTime.now())
                                 .updatedAt(LocalDateTime.now())
                                 .build();

        dummyPlanMeals = List.of(dummyPlanMeal1, dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(List.of(LocalDate.of(2026, 1, 5)))
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.of(2025, 1, 5, 1, 1))
                        .updatedAt(LocalDateTime.of(2025, 1, 6, 1, 1))
                        .planMeals(dummyPlanMeals)
                        .build();

        List<DeliverySchedule> deliverySchedules = planManagementCommandService.generateSchedules();

        assertEquals(1, deliverySchedules.size());
        assertEquals(dummyPlan.getId(), deliverySchedules.getFirst().getPlanId());
        assertEquals(startDate, deliverySchedules.getFirst().getPeriodStart());
        assertEquals(endDate, deliverySchedules.getFirst().getPeriodEnd());

        // assert delivery schedule details are 6, 7
        assertEquals(2, deliverySchedules.getFirst().getDeliveryScheduleDetails().size());
        assertEquals(LocalDate.of(2026, 1, 6), deliverySchedules.getFirst().getDeliveryScheduleDetails().getFirst().getDeliveryDate());
        assertEquals(LocalDate.of(2026, 1, 7), deliverySchedules.getFirst().getDeliveryScheduleDetails().getLast().getDeliveryDate());
        // assert primary meal comes first
        assertEquals(dummyPlanMealId1, deliverySchedules.getFirst().getDeliveryScheduleDetails().getFirst().getPlanMealId());
        assertEquals(dummyPlanMealId2, deliverySchedules.getFirst().getDeliveryScheduleDetails().getLast().getPlanMealId());
    }

    @Test
    void testGenerateSchedules_oneAvailableMeal() {
        // given : business days 5, 6, 7, skipDays : 5
        dummyPlanMeal1 = PlanMeal.builder()
                                 .planMealId(dummyPlanMealId1)
                                 .planId(dummyPlanId)
                                 .name("Standard Bento")
                                 .description("Standard daily bento")
                                 .pricePerMonth(new Money(ORIGINAL_PLANMEAL1_PRICE))
                                 .isPrimary(true)
                                 .minSubCount(new Threshold(1))
                                 .currentSubCount(1)
                                 .imageUrl("https://example.com/standard.jpg")
                                 .createdAt(LocalDateTime.now())
                                 .updatedAt(LocalDateTime.now())
                                 .build();

        dummyPlanMeal2 = PlanMeal.builder()
                                 .planMealId(dummyPlanMealId2)
                                 .planId(dummyPlanId)
                                 .name("Premium Bento")
                                 .description("Premium ingredients bento")
                                 .pricePerMonth(new Money(REMOVED_PLANMEAL_PRICE))
                                 .isPrimary(false)
                                 .minSubCount(new Threshold(5))
                                 .currentSubCount(1)
                                 .imageUrl("https://example.com/premium.jpg")
                                 .createdAt(LocalDateTime.now())
                                 .updatedAt(LocalDateTime.now())
                                 .build();

        dummyPlanMeals = List.of(dummyPlanMeal1, dummyPlanMeal2);

        dummyPlan = Plan.builder()
                        .planId(dummyPlanId)
                        .code(Code.generate())
                        .title("Dummy Plan")
                        .description("Dummy plan for repository stubbing")
                        .status(PlanStatus.ACTIVE)
                        .categoryIds(Set.of(new CategoryId(DUMMY_CATEGORY_ID_UUID)))
                        .providerUserId(userId)
                        .skipDays(List.of(LocalDate.of(2026, 1, 5)))
                        .address(Address.builder()
                                        .prefecture("Osaka")
                                        .city("Osaka")
                                        .district("NishiKu,Honden")
                                        .chomeBanGo("3-2-1")
                                        .buildingNameRoomNo("UpdatedBuilding 1001")
                                        .postalCode("550-0022")
                                        .location(GeoPoint.of(65.658034, 109.701636))
                                        .build())
                        .displaySubscriptionFee(new Money(INITIAL_DISPLAY_SUBFEE))
                        .createdAt(LocalDateTime.of(2025, 1, 5, 1, 1))
                        .updatedAt(LocalDateTime.of(2025, 1, 6, 1, 1))
                        .planMeals(dummyPlanMeals)
                        .build();

        List<DeliverySchedule> deliverySchedules = planManagementCommandService.generateSchedules();
        assertEquals(1, deliverySchedules.size());
        assertEquals(dummyPlan.getId(), deliverySchedules.getFirst().getPlanId());
        assertEquals(startDate, deliverySchedules.getFirst().getPeriodStart());
        assertEquals(endDate, deliverySchedules.getFirst().getPeriodEnd());

        // assert delivery schedule details are 6, 7
        assertEquals(2, deliverySchedules.getFirst().getDeliveryScheduleDetails().size());
        assertEquals(LocalDate.of(2026, 1, 6), deliverySchedules.getFirst().getDeliveryScheduleDetails().getFirst().getDeliveryDate());
        assertEquals(LocalDate.of(2026, 1, 7), deliverySchedules.getFirst().getDeliveryScheduleDetails().getLast().getDeliveryDate());

        // assert only primary meal is present
        assertEquals(dummyPlanMealId1, deliverySchedules.getFirst().getDeliveryScheduleDetails().getFirst().getPlanMealId());
        assertEquals(dummyPlanMealId1, deliverySchedules.getFirst().getDeliveryScheduleDetails().getLast().getPlanMealId());
    }

    @Test
     void testGetActivePlans() {
        PlanResponseDto planResponseDto = planManagementQueryService.getActivePlans(0, 5).getFirst();
        assertNotNull(planResponseDto);
        assertEquals(dummyPlanId.getValue(), planResponseDto.getPlanId());
        assertEquals("Dummy Plan", planResponseDto.getTitle());
        assertEquals("Dummy plan for repository stubbing", planResponseDto.getDescription());
        assertEquals(PlanStatus.RECRUITING, planResponseDto.getStatus());
        assertEquals(DUMMY_CATEGORY_ID_UUID, planResponseDto.getCategoryIds().stream().toList().getFirst());
        assertEquals("Osaka", planResponseDto.getAddress().getPrefecture());
        assertEquals("Osaka", planResponseDto.getAddress().getCity());
        assertEquals("NishiKu,Honden", planResponseDto.getAddress().getDistrict());
        assertEquals("3-2-1", planResponseDto.getAddress().getChomeBanGo());
        assertEquals("UpdatedBuilding 1001", planResponseDto.getAddress().getBuildingNameRoomNo());
        assertEquals("550-0022", planResponseDto.getAddress().getPostalCode());
        assertEquals(65.658034, planResponseDto.getAddress().getLocation().getLatitude());
        assertEquals(109.701636, planResponseDto.getAddress().getLocation().getLongitude());
        assertEquals(new BigDecimal("2000.00"), planResponseDto.getDisplaySubscriptionFee());
        assertEquals(2, planResponseDto.getPlanMealResponseDtos().size());
    }

    @Test
    void testCreateCategory() {
        // when
        planManagementCommandService.createCategory(new CategoryDto("Japanese"));

        // then
        ArgumentCaptor<Category> planArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        verify(planManagementRepository).saveCategory(planArgumentCaptor.capture());

        Category savedCategory = planArgumentCaptor.getValue();
        assertNotNull(savedCategory.getId());
        assertEquals("JAPANESE", savedCategory.getName());
    }

    @Test
    void testCreateCategory_duplicatedCategory_shouldThrow() {
        // given: repository throws DB constraint violation for "Healthy"
        when(planManagementRepository.saveCategory(
                argThat(category -> "HEALTHY".equals(category.getName()))
        )).thenThrow(new DataIntegrityViolationException("duplicate key"));

        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.createCategory(new CategoryDto("Healthy")));
        assertEquals(PlanManagementErrorCode.DUPLICATED_CATEGORY_NAME, planManagementDomainException.getErrorCode());
    }

    @Test
    void testCreateCategory_nullCategory_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.createCategory(null));
        assertEquals(PlanManagementErrorCode.INVALID_CATEGORY_NAME, planManagementDomainException.getErrorCode());
    }

    @Test
    void testCreateCategory_emptyCategory_shouldThrow() {
        PlanManagementDomainException planManagementDomainException = assertThrows(PlanManagementDomainException.class,
                                                                                   () -> planManagementCommandService.createCategory(new CategoryDto("")));
        assertEquals(PlanManagementErrorCode.INVALID_CATEGORY_NAME, planManagementDomainException.getErrorCode());
    }

}
