import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type {
  CategoryDto,
  DeliverySchedule,
  PlanMealRequestDto,
  PlanRequestDto,
  PlanResponseDto,
  UUID,
} from "./types";

const BASE = "/plan-management";

export async function getCategories(): Promise<CategoryDto[]> {
  return apiGet(`${BASE}/v1/category`);
}

export async function searchActivePlans(
  page = 0,
  size = 10,
): Promise<PlanResponseDto[]> {
  return apiGet(`${BASE}/v1/plan?page=${page}&size=${size}`);
}

export async function getPlanDetails(planId: UUID): Promise<PlanResponseDto> {
  return apiGet(`${BASE}/v1/plan/${planId}`);
}

export async function findActivePlansNearMe(
  latitude: number,
  longitude: number,
  page = 0,
  size = 5,
): Promise<PlanResponseDto[]> {
  return apiGet(
    `${BASE}/v1/plan/nearby?latitude=${encodeURIComponent(latitude)}&longitude=${encodeURIComponent(longitude)}&page=${page}&size=${size}`,
  );
}

export async function getDeliverySchedule(
  planId: UUID,
  start: string,
  end: string,
): Promise<DeliverySchedule> {
  return apiGet(
    `${BASE}/v1/plan/delivery-schedule?planId=${planId}&start=${start}&end=${end}`,
  );
}

export async function findMyPlans(
  userId: UUID,
  page = 0,
  size = 5,
): Promise<PlanResponseDto[]> {
  return apiGet(`${BASE}/v1/plan/byuserid?userId=${userId}&page=${page}&size=${size}`);
}

export async function findPlanByTitleAndCode(
  title: string,
  code: string,
): Promise<PlanResponseDto> {
  return apiGet(
    `${BASE}/v1/plan/bytitleandcode?title=${encodeURIComponent(title)}&code=${encodeURIComponent(code)}`,
  );
}

export async function findActivePlansByCategory(
  categoryId: UUID,
  page = 0,
  size = 5,
): Promise<PlanResponseDto[]> {
  return apiGet(
    `${BASE}/v1/plan/bycategory?categoryId=${categoryId}&page=${page}&size=${size}`,
  );
}

export async function createPlan(
  body: PlanRequestDto,
  userIdHeader: UUID,
): Promise<PlanResponseDto> {
  return apiPost(`${BASE}/v1/provider/plan`, body, { "X-USER-ID": userIdHeader });
}

export async function updatePlan(
  planId: UUID,
  body: PlanRequestDto,
): Promise<PlanResponseDto> {
  return apiPut(`${BASE}/v1/provider/plan/${planId}`, body);
}

export async function deletePlan(planId: UUID): Promise<PlanResponseDto> {
  return apiDelete(`${BASE}/v1/provider/plan/${planId}`);
}

export async function addMealToPlan(
  planId: UUID,
  body: PlanMealRequestDto,
): Promise<PlanResponseDto> {
  return apiPost(`${BASE}/v1/provider/plan/meal/${planId}`, body);
}

export async function updateMeal(
  planId: UUID,
  mealId: UUID,
  body: PlanMealRequestDto,
): Promise<PlanResponseDto> {
  return apiPut(`${BASE}/v1/provider/plan/meal?planId=${planId}&mealId=${mealId}`, body);
}

export async function deleteMeal(planId: UUID, mealId: UUID): Promise<void> {
  await apiDelete(`${BASE}/v1/provider/plan/meal?planId=${planId}&mealId=${mealId}`);
}
