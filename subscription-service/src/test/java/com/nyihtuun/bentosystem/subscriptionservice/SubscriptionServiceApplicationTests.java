package com.nyihtuun.bentosystem.subscriptionservice;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class SubscriptionServiceApplicationTests {

    private static final UUID USER_ID_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID PLAN_ID_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID INVALID_PLAN_ID_UUID =
            UUID.fromString("43333333-3333-3333-3333-333333333334");

    private static final UUID API_FAILURE_PLAN_ID_UUID =
            UUID.fromString("53333333-3333-3333-3333-333333333335");

    private static final UUID PLAN_MEAL_ID_UUID1 =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID PLAN_MEAL_ID_UUID2 =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID PLAN_MEAL_ID_UUID3 =
            UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final UUID PLAN_MEAL_ID_UUID4 =
            UUID.fromString("77777777-7777-7777-7777-777777777777");

    private static final UUID SUBSCRIPTION_ID_UUID =
            UUID.fromString("88888888-8888-8888-8888-888888888888");

    private static final UUID INVALID_SUBSCRIPTION_ID_UUID =
            UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Autowired
    private SubscriptionCommandService subscriptionCommandService;

    @Autowired
    private SubscriptionQueryService subscriptionQueryService;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private PlanManagementServiceClient planManagementServiceClient;

    private UserId userId;
    private PlanId planId;
    private PlanMealId planMealId1;
    private PlanMealId planMealId2;
    private List<PlanMealId> planMealIds;
    private SubscriptionRequestDto cannonSubscriptionRequestDto;
    private SubscriptionRequestDto subscriptionRequestDto;
    private SubscriptionRequestDto updateSubscriptionRequestDto;
    private SubscriptionRequestDto invalidPlanIdSubscriptionRequestDto;
    private SubscriptionRequestDto apiFailureSubscriptionRequestDto;
    private Subscription dummySubscription;
    private MealSelection dummyMealSelection1;
    private MealSelection dummyMealSelection2;
    private List<MealSelection> dummyMealSelections;
    private SubscriptionId subscriptionId;
    private SubscriptionId invalidSubscriptionId;

    @BeforeEach
    void setUp() {
        userId = new UserId(USER_ID_UUID);
        planId = new PlanId(PLAN_ID_UUID);
        planMealId1 = new PlanMealId(PLAN_MEAL_ID_UUID1);
        planMealId2 = new PlanMealId(PLAN_MEAL_ID_UUID2);


        cannonSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                            .planId(PLAN_ID_UUID)
                                                            .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2, PLAN_MEAL_ID_UUID3, PLAN_MEAL_ID_UUID4))
                                                            .build();

        subscriptionRequestDto = SubscriptionRequestDto.builder()
                                                       .planId(PLAN_ID_UUID)
                                                       .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                       .build();

        updateSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                       .planId(PLAN_ID_UUID)
                                                       .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID3, PLAN_MEAL_ID_UUID4))
                                                       .build();

        invalidPlanIdSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                                    .planId(INVALID_PLAN_ID_UUID)
                                                                    .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                                    .build();

        apiFailureSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                                 .planId(API_FAILURE_PLAN_ID_UUID)
                                                                 .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                                 .build();

        subscriptionId = new SubscriptionId(SUBSCRIPTION_ID_UUID);
        invalidSubscriptionId = new SubscriptionId(INVALID_SUBSCRIPTION_ID_UUID);

        dummyMealSelection1 = MealSelection.builder()
                                           .subscriptionId(subscriptionId)
                                           .planMealId(planMealId1)
                                           .build();

        dummyMealSelection2 = MealSelection.builder()
                                           .subscriptionId(subscriptionId)
                                           .planMealId(planMealId2)
                                           .build();

        dummyMealSelections = List.of(dummyMealSelection1, dummyMealSelection2);

        dummySubscription = Subscription.builder()
                                        .subscriptionId(subscriptionId)
                                        .planId(planId)
                                        .userId(userId)
                                        .subscriptionStatus(SubscriptionStatus.APPLIED)
                                        .mealSelections(dummyMealSelections)
                                        .build();

        when(planManagementServiceClient.validateAndFetchLegitPlanAndPlanMeals(subscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                                                     cannonSubscriptionRequestDto));

        when(planManagementServiceClient.validateAndFetchLegitPlanAndPlanMeals(updateSubscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                                                     cannonSubscriptionRequestDto));

        when(planManagementServiceClient.validateAndFetchLegitPlanAndPlanMeals(invalidPlanIdSubscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.INVALID_PLAN,
                                                                     null));

        when(planManagementServiceClient.validateAndFetchLegitPlanAndPlanMeals(apiFailureSubscriptionRequestDto))
                .thenAnswer(invocationOnMock -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.API_FAILURE,
                                                                           null));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(subscriptionRepository.findBySubscriptionId(SUBSCRIPTION_ID_UUID))
                .thenAnswer(invocation -> Optional.ofNullable(dummySubscription));

        when(subscriptionRepository.findAllSubscriptionsByUserIdAndDate(any(UUID.class), any(LocalDate.class)))
                .thenAnswer(invocation -> List.of(dummySubscription));

        when(subscriptionRepository.findBySubscriptionId(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(dummySubscription));
    }

    @Test
    void testValidateAndInitiateSubscription() {
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.validateAndInitiateSubscription(
                subscriptionRequestDto);

        assertNotNull(subscriptionResponseDto);
        assertNotNull(subscriptionResponseDto.getSubscriptionId());

        assertEquals(PLAN_ID_UUID, subscriptionResponseDto.getPlanId());
        assertEquals(USER_ID_UUID, subscriptionResponseDto.getUserId());
        assertEquals(SubscriptionStatus.APPLIED, subscriptionResponseDto.getSubscriptionStatus());

        assertEquals(2, subscriptionResponseDto.getMealSelectionResponseDtos().size());
        assertEquals(PLAN_MEAL_ID_UUID1, subscriptionResponseDto.getMealSelectionResponseDtos().get(0).getPlanMealId());
        assertEquals(PLAN_MEAL_ID_UUID2, subscriptionResponseDto.getMealSelectionResponseDtos().get(1).getPlanMealId());
        assertEquals(subscriptionResponseDto.getSubscriptionId(),
                     subscriptionResponseDto.getMealSelectionResponseDtos().get(0).getSubscriptionId());
        assertEquals(subscriptionResponseDto.getSubscriptionId(),
                     subscriptionResponseDto.getMealSelectionResponseDtos().get(1).getSubscriptionId());
    }

    @Test
    void testValidateAndInitiateSubscription_invalidPlanId_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndInitiateSubscription(
                                                                                       invalidPlanIdSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.INVALID_PLAN, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testValidateAndInitiateSubscription_apiFailure_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndInitiateSubscription(
                                                                                       apiFailureSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.VALIDATION_FAILURE, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testValidateAndUpdateSubscription() {
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.validateAndUpdateSubscription(subscriptionId,
                                                                                                                   subscriptionRequestDto);

        assertNotNull(subscriptionResponseDto);
        assertNotNull(subscriptionResponseDto.getSubscriptionId());

        assertEquals(PLAN_ID_UUID, subscriptionResponseDto.getPlanId());

        assertEquals(3, subscriptionResponseDto.getMealSelectionResponseDtos().size());
        assertEquals(PLAN_MEAL_ID_UUID1, subscriptionResponseDto.getMealSelectionResponseDtos().get(0).getPlanMealId());
        assertEquals(PLAN_MEAL_ID_UUID3, subscriptionResponseDto.getMealSelectionResponseDtos().get(1).getPlanMealId());
        assertEquals(PLAN_MEAL_ID_UUID4, subscriptionResponseDto.getMealSelectionResponseDtos().get(2).getPlanMealId());

        assertEquals(subscriptionResponseDto.getSubscriptionId(),
                     subscriptionResponseDto.getMealSelectionResponseDtos().get(0).getSubscriptionId());
        assertEquals(subscriptionResponseDto.getSubscriptionId(),
                     subscriptionResponseDto.getMealSelectionResponseDtos().get(1).getSubscriptionId());
        assertEquals(subscriptionResponseDto.getSubscriptionId(),
                     subscriptionResponseDto.getMealSelectionResponseDtos().get(2).getSubscriptionId());
    }

    @Test
    void testValidateAndUpdateSubscription_invalidPlanId_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(subscriptionId,
                                                                                       invalidPlanIdSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.INVALID_PLAN, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testValidateAndUpdateSubscription_apiFailure_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(subscriptionId,
                                                                                       apiFailureSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.VALIDATION_FAILURE, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }
    @Test
    void testValidateAndUpdateSubscription_invalidSubscription_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(invalidSubscriptionId,
                                                                                                                                              invalidPlanIdSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }


    @Test
    void testCancelSubscription() {
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.cancelSubscription(subscriptionId);
        assertEquals(SubscriptionStatus.CANCELLED, subscriptionResponseDto.getSubscriptionStatus());
        assertEquals(subscriptionId.getValue(), subscriptionResponseDto.getSubscriptionId());
    }

    @Test
    void testCancelSubscription_invalidSubscription_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.cancelSubscription(invalidSubscriptionId));
        assertEquals(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testReflectPlanChanged() {

    }

    @Test
    void testGetMySubscriptions() {
        List<SubscriptionResponseDto> mySubscriptions = subscriptionQueryService.getMySubscriptions(userId.getValue(),
                                                                                                    LocalDate.now());

        assertEquals(1, mySubscriptions.size());
        assertEquals(SUBSCRIPTION_ID_UUID, mySubscriptions.getFirst().getSubscriptionId());
        assertEquals(PLAN_ID_UUID, mySubscriptions.getFirst().getPlanId());
        assertEquals(USER_ID_UUID, mySubscriptions.getFirst().getUserId());
        assertEquals(SubscriptionStatus.APPLIED, mySubscriptions.getFirst().getSubscriptionStatus());

        assertEquals(2, mySubscriptions.getFirst().getMealSelectionResponseDtos().size());
        assertEquals(PLAN_MEAL_ID_UUID1, mySubscriptions.getFirst().getMealSelectionResponseDtos().get(0).getPlanMealId());
        assertEquals(PLAN_MEAL_ID_UUID2, mySubscriptions.getFirst().getMealSelectionResponseDtos().get(1).getPlanMealId());
        assertEquals(SUBSCRIPTION_ID_UUID, mySubscriptions.getFirst().getMealSelectionResponseDtos().get(0).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_UUID, mySubscriptions.getFirst().getMealSelectionResponseDtos().get(1).getSubscriptionId());
    }

    @Test
    void testGetSubscriptionById() {
        SubscriptionResponseDto subscription = subscriptionQueryService.getSubscriptionById(SUBSCRIPTION_ID_UUID);

        assertEquals(SUBSCRIPTION_ID_UUID, subscription.getSubscriptionId());
        assertEquals(PLAN_ID_UUID, subscription.getPlanId());
        assertEquals(USER_ID_UUID, subscription.getUserId());
        assertEquals(SubscriptionStatus.APPLIED, subscription.getSubscriptionStatus());

        assertEquals(2, subscription.getMealSelectionResponseDtos().size());
        assertEquals(PLAN_MEAL_ID_UUID1, subscription.getMealSelectionResponseDtos().get(0).getPlanMealId());
        assertEquals(PLAN_MEAL_ID_UUID2, subscription.getMealSelectionResponseDtos().get(1).getPlanMealId());
        assertEquals(SUBSCRIPTION_ID_UUID, subscription.getMealSelectionResponseDtos().get(0).getSubscriptionId());
        assertEquals(SUBSCRIPTION_ID_UUID, subscription.getMealSelectionResponseDtos().get(1).getSubscriptionId());
    }
}
