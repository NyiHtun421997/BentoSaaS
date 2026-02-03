package com.nyihtuun.bentosystem.subscriptionservice;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.UserPlanSubscriptionEventOutboxRepository;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class SubscriptionServiceApplicationTests {

    private static final UUID USER_ID_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID PROVIDED_USER_ID_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

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

    @MockitoBean
    private UserPlanSubscriptionEventOutboxRepository userPlanSubscriptionEventOutboxRepository;

    private UserId userId;
    private UserId providedUserId;
    private PlanId planId;
    private PlanMealId planMealId1;
    private PlanMealId planMealId2;
    private List<PlanMealId> planMealIds;
    private PlanData cannonPlanData;
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
        SecurityContextHolder.clearContext();
        userId = new UserId(USER_ID_UUID);
        providedUserId = new UserId(PROVIDED_USER_ID_UUID);
        planId = new PlanId(PLAN_ID_UUID);
        planMealId1 = new PlanMealId(PLAN_MEAL_ID_UUID1);
        planMealId2 = new PlanMealId(PLAN_MEAL_ID_UUID2);


        cannonPlanData = new PlanData(PLAN_ID_UUID,
                                      List.of(PLAN_MEAL_ID_UUID1,
                                              PLAN_MEAL_ID_UUID2,
                                              PLAN_MEAL_ID_UUID3,
                                              PLAN_MEAL_ID_UUID4));

        subscriptionRequestDto = SubscriptionRequestDto.builder()
                                                       .planId(PLAN_ID_UUID)
                                                       .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                       .providedUserId(PROVIDED_USER_ID_UUID)
                                                       .build();

        updateSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                             .planId(PLAN_ID_UUID)
                                                             .planMealIds(List.of(PLAN_MEAL_ID_UUID1,
                                                                                  PLAN_MEAL_ID_UUID3,
                                                                                  PLAN_MEAL_ID_UUID4))
                                                             .providedUserId(PROVIDED_USER_ID_UUID)
                                                             .build();

        invalidPlanIdSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                                    .planId(INVALID_PLAN_ID_UUID)
                                                                    .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                                    .providedUserId(PROVIDED_USER_ID_UUID)
                                                                    .build();

        apiFailureSubscriptionRequestDto = SubscriptionRequestDto.builder()
                                                                 .planId(API_FAILURE_PLAN_ID_UUID)
                                                                 .planMealIds(List.of(PLAN_MEAL_ID_UUID1, PLAN_MEAL_ID_UUID2))
                                                                 .providedUserId(PROVIDED_USER_ID_UUID)
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

        dummyMealSelections = new ArrayList<>(Arrays.asList(dummyMealSelection1, dummyMealSelection2));

        dummySubscription = Subscription.builder()
                                        .subscriptionId(subscriptionId)
                                        .planId(planId)
                                        .userId(userId)
                                        .subscriptionStatus(SubscriptionStatus.APPLIED)
                                        .mealSelections(dummyMealSelections)
                                        .providedUserId(providedUserId)
                                        .build();

        when(planManagementServiceClient.validateAndFetchExistingPlanAndPlanMeals(subscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                                                     cannonPlanData));

        when(planManagementServiceClient.validateAndFetchExistingPlanAndPlanMeals(updateSubscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                                                     cannonPlanData));

        when(planManagementServiceClient.validateAndFetchExistingPlanAndPlanMeals(invalidPlanIdSubscriptionRequestDto))
                .thenAnswer(invocation -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.INVALID_PLAN,
                                                                     null));

        when(planManagementServiceClient.validateAndFetchExistingPlanAndPlanMeals(apiFailureSubscriptionRequestDto))
                .thenAnswer(invocationOnMock -> new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.API_FAILURE,
                                                                           null));

        when(subscriptionRepository.save(any(Subscription.class), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(subscriptionRepository.findBySubscriptionId(SUBSCRIPTION_ID_UUID))
                .thenAnswer(invocation -> Optional.ofNullable(dummySubscription));

        when(subscriptionRepository.findAllSubscriptionsByUserIdAndDate(any(UUID.class), any(LocalDate.class)))
                .thenAnswer(invocation -> List.of(dummySubscription));

        when(subscriptionRepository.findByPlanId(any(UUID.class)))
                .thenAnswer(invocation -> List.of(dummySubscription));

        when(subscriptionRepository.findBySubscriptionId(INVALID_SUBSCRIPTION_ID_UUID))
                .thenAnswer(invocation -> Optional.empty());

        doNothing().when(userPlanSubscriptionEventOutboxRepository).save(any(UserPlanSubscriptionEventOutboxMessage.class));
    }

    @Test
    void testValidateAndInitiateSubscription() {
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.validateAndInitiateSubscription(
                subscriptionRequestDto, userId);

        assertNotNull(subscriptionResponseDto);
        assertNotNull(subscriptionResponseDto.getSubscriptionId());

        assertEquals(PLAN_ID_UUID, subscriptionResponseDto.getPlanId());
        assertEquals(USER_ID_UUID, subscriptionResponseDto.getUserId());
        assertEquals(PROVIDED_USER_ID_UUID, subscriptionResponseDto.getProvidedUserId());
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
                                                                                       invalidPlanIdSubscriptionRequestDto, userId));
        assertEquals(SubscriptionErrorCode.INVALID_PLAN, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }

    @Test
    void testValidateAndInitiateSubscription_apiFailure_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndInitiateSubscription(
                                                                                       apiFailureSubscriptionRequestDto, userId));
        assertEquals(SubscriptionErrorCode.VALIDATION_FAILURE, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }

    @Test
    void testValidateAndUpdateSubscription() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.validateAndUpdateSubscription(subscriptionId,
                                                                                                                    updateSubscriptionRequestDto);

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
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(
                                                                                       subscriptionId,
                                                                                       invalidPlanIdSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.INVALID_PLAN, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }

    @Test
    void testValidateAndUpdateSubscription_apiFailure_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(
                                                                                       subscriptionId,
                                                                                       apiFailureSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.VALIDATION_FAILURE, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }

    @Test
    void testValidateAndUpdateSubscription_invalidSubscription_shouldThrow() {
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.validateAndUpdateSubscription(
                                                                                       invalidSubscriptionId,
                                                                                       invalidPlanIdSubscriptionRequestDto));
        assertEquals(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }


    @Test
    void testCancelSubscription() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        SubscriptionResponseDto subscriptionResponseDto = subscriptionCommandService.cancelSubscription(subscriptionId);
        assertEquals(SubscriptionStatus.CANCELLED, subscriptionResponseDto.getSubscriptionStatus());
    }

    @Test
    void testCancelSubscription_invalidSubscription_shouldThrow() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        SubscriptionDomainException subscriptionDomainException = assertThrows(SubscriptionDomainException.class,
                                                                               () -> subscriptionCommandService.cancelSubscription(
                                                                                       invalidSubscriptionId));
        assertEquals(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID, subscriptionDomainException.getErrorCode());
        verify(subscriptionRepository, never()).save(any(Subscription.class), anyBoolean());
    }

    @Test
    void testReflectPlanChanged_activated() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        List<SubscriptionResponseDto> subscriptionResponseDtos = subscriptionCommandService.reflectPlanChanged(planId,
                                                                                                               PlanStatus.ACTIVE);
        assertEquals(SubscriptionStatus.SUBSCRIBED, subscriptionResponseDtos.getFirst().getSubscriptionStatus());
        assertEquals(PLAN_ID_UUID, subscriptionResponseDtos.getFirst().getPlanId());
    }

    @Test
    void testReflectPlanChanged_suspended() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        dummySubscription = Subscription.builder()
                                        .subscriptionId(subscriptionId)
                                        .planId(planId)
                                        .userId(userId)
                                        .subscriptionStatus(SubscriptionStatus.SUBSCRIBED)
                                        .mealSelections(dummyMealSelections)
                                        .providedUserId(providedUserId)
                                        .build();

        List<SubscriptionResponseDto> subscriptionResponseDtos = subscriptionCommandService.reflectPlanChanged(planId, PlanStatus.SUSPENDED);
        assertEquals(SubscriptionStatus.SUSPENDED, subscriptionResponseDtos.getFirst().getSubscriptionStatus());
        assertEquals(PLAN_ID_UUID, subscriptionResponseDtos.getFirst().getPlanId());
    }

    @Test
    void testReflectPlanChanged_cancelled() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        List<SubscriptionResponseDto> subscriptionResponseDtos = subscriptionCommandService.reflectPlanChanged(planId, PlanStatus.CANCELLED);
        assertEquals(SubscriptionStatus.CANCELLED, subscriptionResponseDtos.getFirst().getSubscriptionStatus());
        assertEquals(PLAN_ID_UUID, subscriptionResponseDtos.getFirst().getPlanId());
    }

    @Test
    void testReflectPlanMealsRemoved_statusUnchanged() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        List<SubscriptionResponseDto> subscriptionResponseDtos = subscriptionCommandService.reflectPlanMealsRemoved(planId, List.of(planMealId2));
        assertEquals(1, subscriptionResponseDtos.getFirst().getMealSelectionResponseDtos().size());
        assertEquals(PLAN_MEAL_ID_UUID1, subscriptionResponseDtos.getFirst().getMealSelectionResponseDtos().getFirst().getPlanMealId());
        assertEquals(SubscriptionStatus.APPLIED, subscriptionResponseDtos.getFirst().getSubscriptionStatus());
    }

    @Test
    void testReflectPlanMealsRemoved_statusChanged() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
        List<SubscriptionResponseDto> subscriptionResponseDtos = subscriptionCommandService.reflectPlanMealsRemoved(planId, List.of(planMealId1, planMealId2));
        assertEquals(0, subscriptionResponseDtos.getFirst().getMealSelectionResponseDtos().size());
        assertEquals(SubscriptionStatus.CANCELLED, subscriptionResponseDtos.getFirst().getSubscriptionStatus());
    }

    @Test
    void testGetMySubscriptions() {
        setupSecurityContext("USER", USER_ID_UUID.toString());
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
        setupSecurityContext("USER", USER_ID_UUID.toString());
        SubscriptionResponseDto subscription = subscriptionQueryService.getSubscriptionById(SUBSCRIPTION_ID_UUID).get();

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

    private void setupSecurityContext(String role, String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
