import { useMemo } from "react";
import BaseModal from "./BaseModal";
import { apiGet } from "../../src/lib/api/http";
import { isApiError } from "../../src/lib/api/http";

import type {
  UUID,
  InvoiceResponseDto,
  SubscriptionResponseDto,
  PlanResponseDto,
  UserResponseDTO,
} from "../../src/lib/api/types";

import formatToJapaneseDateTime from "../utilities/formatToJst";
import formatAddressJp from "../utilities/formatAddressJp";
import { useQuery } from "@tanstack/react-query";
import PageLoading from "./PageLoading";
import formatYmdJp from "../utilities/formatYmdJp";

type Props = {
  open: boolean;
  onClose: () => void;
  invoiceId: UUID | null;
};

const TAX_RATE = 0.1; // Japan consumption tax 10%

const yen = (n?: number) =>
  typeof n === "number" ? `¥${Math.round(n).toLocaleString("ja-JP")}` : "-";

const safeName = (u?: UserResponseDTO) => {
  const first = u?.firstName ?? "";
  const last = u?.lastName ?? "";
  const full = `${last} ${first}`.trim();
  return full || u?.email || "—";
};

const InvoiceDetailsModal = ({ open, onClose, invoiceId }: Props) => {
  const { data, isLoading, error } = useQuery({
    queryKey: ["invoiceDetails", invoiceId],
    enabled: open && !!invoiceId,
    queryFn: async () => {
      if (!invoiceId) throw new Error("Missing invoiceId");

      const invoice = await apiGet<InvoiceResponseDto>(
        `/invoice/api/v1/${invoiceId}`,
      );

      const subscription = await apiGet<SubscriptionResponseDto>(
        `/subscription/api/v1/${invoice.subscriptionId}`,
      );

      const [issuer, billTo] = await Promise.all([
        apiGet<UserResponseDTO>(`/user/api/v1/${invoice.providedUserId}`),
        apiGet<UserResponseDTO>(`/user/api/v1/${invoice.userId}`),
      ]);

      const plan = await apiGet<PlanResponseDto>(
        `/plan-management/api/v1/plan/${subscription.planId}`,
      );

      return {
        invoice,
        subscription,
        issuer,
        billTo,
        plan,
      };
    },
  });

  const selectedMeals = useMemo(() => {
    const raw = data?.plan?.planMealResponseDtos ?? [];
    const mealIds = data?.invoice?.subscribedMealIds ?? [];
    return mealIds
      .map((mid) => raw.find((m) => m.planMealId === mid))
      .filter(Boolean);
  }, [data?.invoice?.subscribedMealIds, data?.plan]);

  // If we have meal prices, we can compute subtotal from them
  const computedSubtotal = useMemo(() => {
    const sum = selectedMeals.reduce(
      (acc, m) => acc + (m?.pricePerMonth ?? 0),
      0,
    );
    return sum > 0 ? sum : undefined;
  }, [selectedMeals]);

  const subtotal = computedSubtotal ?? data?.invoice?.amount ?? 0;
  const tax = Math.round(subtotal * TAX_RATE);
  const total = subtotal + tax;

  const statusBadge = (status?: string) => {
    switch (status) {
      case "PAID":
        return "bg-emerald-50 text-emerald-700";
      case "ISSUED":
        return "bg-blue-50 text-blue-700";
      case "FAILED":
        return "bg-amber-50 text-amber-700";
      case "CANCELLED":
        return "bg-rose-50 text-rose-700";
      default:
        return "bg-slate-100 text-slate-700";
    }
  };

  return (
    <BaseModal
      open={open}
      onClose={onClose}
      panelClassName="w-[min(920px,96vw)] max-h-[88vh] overflow-auto px-8 py-6"
    >
      {({ close }) => (
        <div className="flex flex-col gap-5">
          {/* Header */}
          <div className="text-lg font-semibold bg-gradient-to-r from-blue-500 to-blue-300 text-white text-center px-3 py-1 rounded">
            請求書
          </div>
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 className="text-xl font-semibold text-slate-900">
                {data?.plan?.title ?? "Invoice Details"}
              </h2>
              {data?.invoice?.periodStart && data?.invoice?.periodEnd ? (
                <div className="mt-1 text-xs text-slate-500">
                  Billing period: {formatYmdJp(data.invoice.periodStart)} ~{" "}
                  {formatYmdJp(data.invoice.periodEnd)}
                </div>
              ) : null}
            </div>

            <div className="flex items-center gap-3">
              <span
                className={[
                  "rounded-full px-3 py-1 text-xs font-semibold",
                  statusBadge(data?.invoice?.invoiceStatus),
                ].join(" ")}
              >
                {data?.invoice?.invoiceStatus ?? "-"}
              </span>

              <button
                className="text-slate-500 hover:text-slate-900"
                onClick={close}
                aria-label="Close"
              >
                ✕
              </button>
            </div>
          </div>

          {isLoading ? (
            <div className="text-sm text-slate-600">
              <PageLoading text="Loading details…" />
            </div>
          ) : error ? (
            <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
              {isApiError(error) ? error.message : null}
            </div>
          ) : !data?.invoice ? (
            <div className="text-sm text-slate-600">No invoice found.</div>
          ) : (
            <>
              {/* Parties + Meta */}
              <div className="grid grid-cols-1 border-b border-slate-200 gap-4 md:grid-cols-2">
                {/* Bill to */}
                <div className="rounded-2xl bg-white p-4">
                  <div className="text-lg font-semibold text-slate-900">
                    {`${safeName(data.billTo ?? undefined)} 御中`}
                  </div>
                  {data.billTo?.address ? (
                    <>
                      <div className="mt-2 text-xs text-slate-600">
                        {`〒 ${data.billTo.address.postalCode ?? ""}`}
                      </div>
                      <div className="mt-2 text-xs text-slate-600">
                        {formatAddressJp(data.billTo.address, false)}
                      </div>
                    </>
                  ) : null}
                  {data.billTo?.email ? (
                    <div className="mt-1 text-xs text-slate-600">
                      {`メール: ${data.billTo.email}`}
                    </div>
                  ) : null}
                  {data.invoice?.userId ? (
                    <div className="mt-2 text-[11px] text-slate-500 font-mono">
                      User ID: {String(data.invoice.userId)}
                    </div>
                  ) : null}
                </div>

                {/* Invoice Meta */}
                <div className="rounded-2xl bg-white p-4">
                  <div className="mt-2 grid grid-cols-[90px_1fr] gap-2">
                    <div className="text-xs text-slate-600 font-semibold">
                      発行日
                    </div>
                    <div className="text-xs font-mono">
                      {data.invoice?.issuedAt
                        ? formatToJapaneseDateTime(data.invoice.issuedAt, false)
                        : "-"}
                    </div>

                    <div className="text-xs text-slate-600 font-semibold">
                      請求書番号
                    </div>
                    <div className="text-xs font-mono">
                      {String(data.invoice?.invoiceId ?? "")}
                    </div>

                    <div className="text-xs text-slate-600 font-semibold">
                      登録番号
                    </div>
                    <div className="text-xs font-mono">
                      {String(data.invoice?.subscriptionId ?? "")}
                    </div>

                    <div className="text-xs text-slate-600 font-semibold">
                      更新日
                    </div>
                    <div className="text-xs font-mono">
                      {data.invoice?.updatedAt
                        ? formatToJapaneseDateTime(
                            data.invoice.updatedAt,
                            false,
                          )
                        : "-"}
                    </div>

                    <div className="text-xs text-slate-600 font-semibold">
                      支払日
                    </div>
                    <div className="text-xs font-mono">
                      {data.invoice?.paidAt
                        ? formatToJapaneseDateTime(data.invoice.paidAt, false)
                        : "-"}
                    </div>
                  </div>
                </div>
              </div>

              {/* Issuer */}
              <div className="flex justify-end border-b border-slate-200 pb-4">
                <div className="rounded-2xl bg-white p-4">
                  <div className="text-xs font-semibold text-slate-500">
                    ISSUER (Provider)
                  </div>
                  <div className="mt-2 text-sm font-semibold text-slate-900">
                    {safeName(data.issuer ?? undefined)}
                  </div>
                  {data.issuer?.address ? (
                    <>
                      <div className="mt-2 text-xs text-slate-600">
                        {`〒 ${data.issuer.address.postalCode ?? ""}`}
                      </div>
                      <div className="mt-2 text-xs text-slate-600">
                        {`住所: ${formatAddressJp(data.issuer.address, false)}`}
                      </div>
                    </>
                  ) : null}
                  {data.issuer?.email ? (
                    <div className="mt-1 text-xs text-slate-600">
                      {`メール: ${data.issuer.email}`}
                    </div>
                  ) : null}
                  {data.issuer?.phNo ? (
                    <div className="mt-1 text-xs text-slate-600">
                      {`電話: ${data.issuer.phNo}`}
                    </div>
                  ) : null}
                  {data.invoice?.providedUserId ? (
                    <div className="mt-2 text-[11px] text-slate-500 font-mono">
                      Provider ID: {String(data.invoice.providedUserId)}
                    </div>
                  ) : null}
                </div>
              </div>

              {/* Service Info (Plan + Meals) */}
              <div className="rounded-2xl border border-slate-200 bg-white p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <div className="text-xs font-semibold text-slate-500">
                      SERVICE
                    </div>
                    <div className="mt-1 text-sm font-semibold text-slate-900">
                      {data.plan?.title ?? "Plan"}
                    </div>
                    {data.plan?.code ? (
                      <div className="mt-1 text-xs text-slate-600">
                        Code: {data.plan.code}
                      </div>
                    ) : null}
                    {data.plan?.address ? (
                      <div className="mt-1 text-xs text-slate-600">
                        Service location: {formatAddressJp(data.plan.address)}
                      </div>
                    ) : null}
                  </div>

                  <div className="text-right">
                    <div className="text-xs text-slate-500">金額（税抜）</div>
                    <div className="text-lg font-semibold text-slate-900">
                      {yen(data.invoice?.amount)}
                    </div>
                  </div>
                </div>

                <div className="mt-4 overflow-auto rounded-xl border border-slate-200">
                  <table className="min-w-full text-sm">
                    <thead className="bg-slate-50 text-xs text-slate-600">
                      <tr>
                        <th className="px-4 py-2 text-left font-semibold">
                          説明
                        </th>
                        <th className="px-4 py-2 text-left font-semibold">
                          期間
                        </th>
                        <th className="px-4 py-2 text-right font-semibold">
                          単価（税抜）
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedMeals.length === 0 ? (
                        <tr>
                          <td className="px-4 py-3 text-slate-600" colSpan={3}>
                            No meal selections found.
                          </td>
                        </tr>
                      ) : (
                        selectedMeals.map((meal, idx) => (
                          <tr
                            key={String(meal?.planMealId ?? idx)}
                            className="border-t"
                          >
                            <td className="px-4 py-3 text-slate-800">
                              {meal?.name ?? "Meal"}
                              {meal?.primary ? (
                                <span className="ml-2 text-xs font-semibold text-emerald-700">
                                  (Primary)
                                </span>
                              ) : null}
                            </td>
                            <td className="px-4 py-3 text-slate-600">
                              {formatYmdJp(data.invoice?.periodStart)} ~{" "}
                              {formatYmdJp(data.invoice?.periodEnd)}
                            </td>
                            <td className="px-4 py-3 text-right text-slate-800">
                              {yen(meal?.pricePerMonth)}
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                {/* Totals */}
                <div className="mt-4 flex justify-end">
                  <div className="w-full max-w-sm rounded-xl border border-slate-200 bg-slate-50 p-4">
                    <div className="flex items-center justify-between text-sm">
                      <div className="text-slate-600">小計</div>
                      <div className="font-semibold text-slate-900">
                        {yen(subtotal)}
                      </div>
                    </div>
                    <div className="mt-2 flex items-center justify-between text-sm">
                      <div className="text-slate-600">
                        消費税 ({Math.round(TAX_RATE * 100)}%)
                      </div>
                      <div className="font-semibold text-slate-900">
                        {yen(tax)}
                      </div>
                    </div>
                    <div className="mt-3 border-t border-slate-200 pt-3 flex items-center justify-between">
                      <div className="text-sm font-semibold text-slate-700">
                        合計
                      </div>
                      <div className="text-lg font-semibold text-slate-900">
                        {yen(total)}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="flex justify-end pt-1">
                <button
                  className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  onClick={close}
                >
                  Close
                </button>
              </div>
            </>
          )}
        </div>
      )}
    </BaseModal>
  );
};

export default InvoiceDetailsModal;
