import { apiGet } from "./http";
import type { InvoiceResponseDto, UUID } from "./types";

const BASE = "/invoice";

export async function findInvoicesByUserIdAndDate(
  userId: UUID,
  date: string,
): Promise<InvoiceResponseDto[]> {
  return apiGet(`${BASE}/v1/byuseridanddate?userId=${userId}&date=${date}`);
}

export async function getInvoiceById(
  invoiceId: UUID,
): Promise<InvoiceResponseDto> {
  return apiGet(`${BASE}/v1/${invoiceId}`);
}
