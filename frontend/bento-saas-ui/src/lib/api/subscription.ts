import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { SubscriptionRequestDto, SubscriptionResponseDto, UUID } from "./types";

const BASE = "/subscription";

export async function createSubscription(
  body: SubscriptionRequestDto,
  userIdHeader: UUID,
): Promise<SubscriptionResponseDto> {
  return apiPost(`${BASE}/v1`, body, { "X-USER-ID": userIdHeader });
}

export async function getSubscriptionDetails(
  subscriptionId: UUID,
): Promise<SubscriptionResponseDto> {
  return apiGet(`${BASE}/v1/${subscriptionId}`);
}

export async function updateSubscription(
  subscriptionId: UUID,
  body: SubscriptionRequestDto,
): Promise<SubscriptionResponseDto> {
  return apiPut(`${BASE}/v1/${subscriptionId}`, body);
}

export async function cancelSubscription(
  subscriptionId: UUID,
): Promise<SubscriptionResponseDto> {
  return apiDelete(`${BASE}/v1/${subscriptionId}`);
}

export async function findMySubscriptions(
  userId: UUID,
  since: string,
): Promise<SubscriptionResponseDto[]> {
  return apiGet(`${BASE}/v1/byuseridanddate?userId=${userId}&since=${since}`);
}
