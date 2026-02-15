import "./InvoicesPage.css";

import { useContext, useEffect, useState } from "react";
import AuthContext from "../../state/createContext";
import { apiGet } from "../../lib/api/http";
import { isApiError } from "../../lib/api/http";
import PageLoading from "../../components/PageLoading";
import AlertModal from "../../components/AlertModal";
import InvoiceDetailsModal from "../../components/InvoiceDetailsModal";
import formatToJapaneseDateTime from "../../utilities/formatToJst";

import type { UUID, InvoiceResponseDto } from "../../lib/api/types";
import formatYmdJp from "../../utilities/formatYmdJp";

const toYmd = (d: Date) => d.toISOString().slice(0, 10);

const oneYearAgoYmd = () => {
  const d = new Date();
  d.setFullYear(d.getFullYear() - 1);
  return toYmd(d);
};

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

const yen = (n?: number) =>
  typeof n === "number" ? `¥${Math.round(n).toLocaleString("ja-JP")}` : "-";

export default function InvoicesPage() {
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId as UUID | undefined;

  const [since, setSince] = useState(() => oneYearAgoYmd());
  const [loading, setLoading] = useState(false);
  const [invoices, setInvoices] = useState<InvoiceResponseDto[]>([]);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openAlert, setOpenAlert] = useState(false);

  const [openDetails, setOpenDetails] = useState(false);
  const [targetInvoiceId, setTargetInvoiceId] = useState<UUID | null>(null);

  useEffect(() => {
    if (error || success) setOpenAlert(true);
  }, [error, success]);

  const fetchAll = async () => {
    if (!userId) {
      setError("Please login again (missing userId in auth state).");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // NOTE: backend param is "date" but UI treats it as "since"
      const data = await apiGet<InvoiceResponseDto[]>(
        `/invoice/api/v1/byuseridanddate?userId=${encodeURIComponent(
          userId,
        )}&date=${encodeURIComponent(since)}`,
      );

      // sort latest first (issuedAt desc if present)
      const sorted = (data ?? []).slice().sort((a, b) => {
        const ax = a.issuedAt ?? "";
        const bx = b.issuedAt ?? "";
        return bx.localeCompare(ax);
      });

      setInvoices(sorted);
    } catch (e: unknown) {
      setError(isApiError(e) ? e.message : "Failed to load invoices");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!userId) return;
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, since]);

  const openInvoice = (invoiceId?: UUID) => {
    if (!invoiceId) return;
    setTargetInvoiceId(invoiceId);
    setOpenDetails(true);
  };

  if (!userId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Invoices</h1>
        <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Please login again (missing userId in auth state).
        </p>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <h1 className="page-title">Invoices</h1>
      <p className="page-subtitle">
        View your billing history and invoice details.
      </p>

      {/* Filter */}
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="flex flex-col">
          <label className="text-xs font-semibold text-slate-600">Since</label>
          <input
            type="date"
            value={since}
            onChange={(e) => setSince(e.target.value)}
            className="mt-1 rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-800"
          />
        </div>

        <button
          type="button"
          onClick={fetchAll}
          className="h-10 rounded-xl bg-slate-900 px-4 text-sm font-semibold text-white hover:bg-slate-800"
        >
          Refresh
        </button>

        <button
          type="button"
          onClick={() => setSince(oneYearAgoYmd())}
          className="h-10 rounded-xl border border-slate-200 px-4 text-sm font-semibold text-slate-700 hover:bg-slate-50"
        >
          Default (1 year)
        </button>
      </div>

      <div className="subscriptions-panel">
        {loading ? (
          <PageLoading text="Loading invoices…" />
        ) : invoices.length === 0 ? (
          <p className="text-sm text-slate-500">No invoices found.</p>
        ) : (
          <div className="flex flex-col gap-3">
            {invoices.map((inv) => {
              const invoiceId = inv.invoiceId as UUID | undefined;

              return (
                <button
                  key={String(invoiceId)}
                  type="button"
                  onClick={() => openInvoice(invoiceId)}
                  className="text-left rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:bg-slate-50"
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <div className="text-sm font-semibold text-slate-900">
                          {yen(inv.amount)}
                        </div>
                        <span
                          className={[
                            "rounded-full px-3 py-1 text-xs font-semibold",
                            statusBadge(inv.invoiceStatus),
                          ].join(" ")}
                        >
                          {inv.invoiceStatus ?? "-"}
                        </span>
                      </div>

                      <div className="mt-1 text-xs text-slate-600">
                        Period: {formatYmdJp(inv.periodStart) ?? "-"} ~{" "}
                        {formatYmdJp(inv.periodEnd) ?? "-"}
                      </div>

                      {inv.issuedAt ? (
                        <div className="mt-1 text-xs text-slate-500">
                          Issued: {formatToJapaneseDateTime(inv.issuedAt)}
                        </div>
                      ) : null}

                      {inv.paidAt ? (
                        <div className="mt-1 text-xs text-slate-500">
                          Paid: {formatToJapaneseDateTime(inv.paidAt)}
                        </div>
                      ) : null}
                    </div>

                    <div className="text-xs text-slate-500">
                      <div>Invoice ID</div>
                      <div className="mt-1 font-mono text-slate-700">
                        {invoiceId}
                      </div>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* Alert */}
      {error || success ? (
        <AlertModal
          open={openAlert}
          onClose={() => {
            setOpenAlert(false);
            setError(null);
            setSuccess(null);
          }}
          msg={error ?? success ?? ""}
          isSuccess={!!success && !error}
        />
      ) : null}

      {/* Details modal */}
      <InvoiceDetailsModal
        open={openDetails}
        invoiceId={targetInvoiceId}
        onClose={() => {
          setOpenDetails(false);
          setTargetInvoiceId(null);
        }}
      />
    </div>
  );
}
