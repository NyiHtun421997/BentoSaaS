import type { NotificationDto } from "../api/types";

type IdWrapper = { value: string };

type PlanPayload = {
  id?: IdWrapper;
  code?: IdWrapper;
  title?: string;
  status?: string;
};

type InvoicePayload = {
  id?: IdWrapper;
  amount?: { amount?: number };
  periodStart?: string;
  periodEnd?: string;
};

const formatYen = (n?: number) =>
  typeof n === "number" ? `¥${n.toLocaleString("ja-JP")}` : "";

export const buildNotificationView = (
  noti: NotificationDto,
  isProvider: boolean | undefined,
) => {
  if (
    noti.notificationType === "PLAN_UPDATED_EVENT" ||
    noti.notificationType === "PLAN_DELETED_EVENT"
  ) {
    const p = noti.payload as PlanPayload | undefined;

    const title =
      noti.notificationType === "PLAN_UPDATED_EVENT" && isProvider
        ? "Your plan has been updated"
        : noti.notificationType === "PLAN_UPDATED_EVENT"
          ? "A plan you subscribed to has been updated"
          : "A plan you subscribed to has been deleted";

    const subtitle = `${p?.title ?? "Plan"}'s status has just been updated to ${p?.status ?? "unknown"}`;

    const to =
      noti.notificationType === "PLAN_UPDATED_EVENT" && isProvider
        ? "/provider/plans"
        : "/app/subscriptions";

    return {
      title,
      subtitle,
      to,
    };
  }

  if (noti.notificationType === "INVOICE_ISSUED_EVENT") {
    const p = noti.payload as InvoicePayload | undefined;
    const invoiceId = p?.id?.value;

    const amount = formatYen(p?.amount?.amount);
    const period =
      p?.periodStart && p?.periodEnd ? `${p.periodStart} → ${p.periodEnd}` : "";

    return {
      title: "Invoice issued",
      subtitle: period ? `${amount} · ${period}` : amount,
      to: invoiceId ? `/app/invoices?invoiceId=${invoiceId}` : "/app/invoices",
    };
  }

  return {
    title: "Notification",
    subtitle: `#${noti.id}`,
    to: "/app/browse",
  };
};
