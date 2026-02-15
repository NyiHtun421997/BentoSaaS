import { apiGet, apiPost } from "./http";
import type { UserRequestDTO, UserResponseDTO, UUID } from "./types";

const BASE = "/user";

export async function signupRegular(
  body: UserRequestDTO,
): Promise<UserResponseDTO> {
  return apiPost(`${BASE}/v1/signup/regular`, body);
}

export async function signupProvider(
  body: UserRequestDTO,
): Promise<UserResponseDTO> {
  return apiPost(`${BASE}/v1/signup/provider`, body);
}

export async function getUserById(userId: UUID): Promise<UserResponseDTO> {
  return apiGet(`${BASE}/v1/${userId}`);
}
