export type UUID = string;

export type GeoPoint = { latitude: number; longitude: number };

export type AddressDto = {
  buildingNameRoomNo?: string;
  chomeBanGo: string;
  district: string;
  postalCode: string;
  city: string;
  prefecture: string;
  location: GeoPoint;
};

export type CategoryDto = { id?: UUID; name: string };

export type PlanMealRequestDto = {
  name: string;
  description: string;
  pricePerMonth: number;
  primary?: boolean;
  minSubCount?: number;
  imageUrl?: string;
};

export type PlanRequestDto = {
  title: string;
  description: string;
  categoryIds: UUID[];
  address: AddressDto;
  displaySubscriptionFee: number;
  skipDays?: string[];
  imageUrl?: string;
  planMealRequestDtos?: PlanMealRequestDto[];
};

export type PlanMealResponseDto = PlanMealRequestDto & {
  planMealId?: UUID;
  planId?: UUID;
  currentSubCount?: number;
};

export type PlanStatus = "RECRUITING" | "ACTIVE" | "SUSPENDED" | "CANCELLED";

export type PlanResponseDto = Omit<PlanRequestDto, "planMealRequestDtos"> & {
  planId?: UUID;
  code?: string;
  status?: PlanStatus;
  providerUserId?: UUID;
  planMealResponseDtos?: PlanMealResponseDto[];
};

export type IdWrapper = {
  value: UUID;
};

export type DeliveryScheduleDetail = {
  deliveryDate: string;
  planMealId?: IdWrapper;
};

export type DeliverySchedule = {
  periodStart?: string;
  periodEnd?: string;
  createdAt?: string;
  deliveryScheduleDetails?: DeliveryScheduleDetail[];
};

export type SubscriptionStatus =
  | "APPLIED"
  | "SUBSCRIBED"
  | "CANCELLED"
  | "SUSPENDED";

export type SubscriptionRequestDto = {
  planId?: UUID;
  planMealIds: UUID[];
  providedUserId: UUID;
};

export type MealSelectionResponseDto = {
  subscriptionId?: UUID;
  planMealId?: UUID;
};

export type SubscriptionResponseDto = {
  subscriptionId?: UUID;
  userId?: UUID;
  planId?: UUID;
  subscriptionStatus?: SubscriptionStatus;
  mealSelectionResponseDtos?: MealSelectionResponseDto[];
  providedUserId?: UUID;
};

export type InvoiceStatus = "ISSUED" | "PAID" | "CANCELLED" | "FAILED";

export type InvoiceResponseDto = {
  invoiceId?: UUID;
  subscriptionId?: UUID;
  userId?: UUID;
  providedUserId?: UUID;
  invoiceStatus?: InvoiceStatus;
  amount?: number;
  subscribedMealIds?: UUID[];
  issuedAt?: string;
  updatedAt?: string;
  paidAt?: string;
  periodStart?: string;
  periodEnd?: string;
};

export type AddressRequestDTO = {
  buildingNameRoomNo?: string;
  chomeBanGo: string;
  district: string;
  postalCode: string;
  city: string;
  prefecture: string;
};

export type UserRequestDTO = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phNo: string;
  description?: string;
  imageUrl?: string;
  address?: AddressRequestDTO;
};

export type UserResponseDTO = {
  userId?: UUID;
  email?: string;
  firstName?: string;
  lastName?: string;
  phNo?: string;
  description?: string;
  joinedOn?: string;
  updatedAt?: string;
  imageUrl?: string;
  address?: AddressDto;
};

export type Role = "USER" | "PROVIDER" | "ADMIN";

export type AuthenticatedUser = {
  token: string;
  userId: string;
  authorities: string[];
  roles: Role[];
  effectiveRole: Role;
  email: string;
};
