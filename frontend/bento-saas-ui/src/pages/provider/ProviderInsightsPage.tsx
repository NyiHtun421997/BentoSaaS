import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  FiArrowLeft,
  FiActivity,
  FiCalendar,
  FiMapPin,
  FiPackage,
  FiUsers,
  FiCheckCircle,
  FiPauseCircle,
  FiXCircle,
  FiClock,
} from "react-icons/fi";

import { apiGet, isApiError } from "../../lib/api/http";
import AlertModal from "../../components/AlertModal";
import PageLoading from "../../components/PageLoading";
import DeliveryScheduleViewModal from "../../components/DeliveryScheduleViewModal";
import type {
  PlanResponseDto,
  SubscriptionResponseDto,
} from "../../lib/api/types";
import "./ProviderInsightsPage.css";
import { endOfMonth, startOfMonth, startOfToday, addDays } from "date-fns";
import formatYmdJp from "../../utilities/formatYmdJp";
import formatToJapaneseDateTime from "../../utilities/formatToJst";

const statusBadge = (status?: string) => {
  const base =
    "inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-extrabold";
  switch (status) {
    case "ACTIVE":
      return (
        <span className={`${base} bg-emerald-50 text-emerald-700`}>
          <FiCheckCircle /> ACTIVE
        </span>
      );
    case "RECRUITING":
      return (
        <span className={`${base} bg-indigo-50 text-indigo-700`}>
          <FiClock /> RECRUITING
        </span>
      );
    case "SUSPENDED":
      return (
        <span className={`${base} bg-amber-50 text-amber-700`}>
          <FiPauseCircle /> SUSPENDED
        </span>
      );
    case "CANCELLED":
      return (
        <span className={`${base} bg-rose-50 text-rose-700`}>
          <FiXCircle /> CANCELLED
        </span>
      );
    default:
      return (
        <span className={`${base} bg-slate-100 text-slate-700`}>
          {status ?? "—"}
        </span>
      );
  }
};

const pct = (a: number, b: number) => {
  if (b <= 0) return 0;
  const v = Math.floor((a / b) * 100);
  return Math.max(0, Math.min(100, v));
};

const ProviderInsightsPage = () => {
  const { planId } = useParams();

  const [loading, setLoading] = useState(false);
  const [plan, setPlan] = useState<PlanResponseDto | null>(null);
  const meals = useMemo(
    () =>
      (
        plan as PlanResponseDto & {
          planMealResponseDtos?: Array<{
            planMealId: string | number;
            name: string;
            description: string;
            pricePerMonth: number;
            primary: boolean;
            minSubCount: number;
            image?: string;
          }>;
        }
      )?.planMealResponseDtos ?? [],
    [plan],
  );

  const [start, setStart] = useState<string>(() => {
    return addDays(startOfMonth(startOfToday()), 1).toISOString().slice(0, 10);
  });
  const [end, setEnd] = useState<string>(() => {
    return endOfMonth(startOfToday()).toISOString().slice(0, 10);
  });

  const [scheduleOpen, setScheduleOpen] = useState(false);

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMsg, setAlertMsg] = useState<string | null>(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  const showAlert = (msg: string, ok: boolean) => {
    setAlertMsg(msg);
    setAlertSuccess(ok);
    setAlertOpen(true);
  };

  const totalApplicants = useMemo(() => {
    return meals.reduce((acc, m) => acc + Number(m.currentSubCount ?? 0), 0);
  }, [meals]);

  useEffect(() => {
    if (!planId) return;

    let mounted = true;

    const load = async () => {
      setLoading(true);
      try {
        const data = await apiGet<PlanResponseDto>(
          `/plan-management/api/v1/plan/${planId}`,
        );
        if (!mounted) return;
        setPlan(data);
      } catch (e: unknown) {
        showAlert(isApiError(e) ? e.message : "Failed to load plan", false);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void load();
    return () => {
      mounted = false;
    };
  }, [planId]);

  const scheduleTarget = useMemo(() => {
    if (!planId) return null;
    const sub: SubscriptionResponseDto = { planId };
    return { sub, plan: plan ?? undefined };
  }, [planId, plan]);

  if (!planId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Insights</h1>
        <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Missing planId in route.
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page-shell">
        <PageLoading text="Loading insights..." />
      </div>
    );
  }

  return (
    <div className="page-shell">
      <div className="provider-form-header">
        <div>
          <h1 className="page-title">Insights</h1>
          <p className="page-subtitle">
            Plan status, applicant counts, threshold progress, and delivery
            schedules.
          </p>

          <div className="mt-2 flex flex-wrap items-center gap-2">
            {statusBadge(plan?.status)}
            <span className="text-xs text-slate-500">
              Plan:{" "}
              <span className="font-semibold text-slate-900">
                {plan?.title ?? "—"}
              </span>
            </span>
          </div>
        </div>

        <div className="flex gap-2">
          <Link
            to="/provider/plans"
            className="provider-btn"
            style={{ textDecoration: "none" }}
          >
            <FiArrowLeft className="inline" /> Back
          </Link>
          <Link
            to={`/provider/plans/${planId}/edit`}
            className="provider-btn provider-btn-primary"
            style={{ textDecoration: "none" }}
          >
            Edit Plan
          </Link>
        </div>
      </div>

      {/* Summary */}
      <div className="provider-panel">
        <div className="provider-panel-title">
          <FiActivity className="provider-icon" /> Summary
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <div className="text-xs text-slate-500">
              Number of Applicants Per Meal
            </div>
            <div className="mt-1 text-2xl font-extrabold text-slate-900">
              <FiUsers className="inline -mt-1" />{" "}
              {totalApplicants / (meals.length || 1)}
            </div>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <div className="text-xs text-slate-500">Meals</div>
            <div className="mt-1 text-2xl font-extrabold text-slate-900">
              <FiPackage className="inline -mt-1" /> {meals.length}
            </div>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-4">
            <div className="text-xs text-slate-500">Location</div>
            <div className="mt-1 text-sm font-semibold text-slate-900">
              <FiMapPin className="inline -mt-0.5" />{" "}
              {plan?.address?.prefecture ?? "—"} / {plan?.address?.city ?? "—"}
            </div>
            <div className="mt-1 text-xs text-slate-500 font-mono">
              {plan?.address?.location?.latitude ?? "—"},{" "}
              {plan?.address?.location?.longitude ?? "—"}
            </div>
          </div>
        </div>
      </div>

      {/* Meal thresholds */}
      <div className="provider-panel">
        <div className="provider-panel-title">
          <FiPackage className="provider-icon" /> Meal Threshold Progress
        </div>

        {meals.length === 0 ? (
          <div className="text-sm text-slate-500">No meals.</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {meals.map((m) => {
              const cur = Number(m.currentSubCount ?? 0);
              const min = Number(m.minSubCount ?? 0);
              const p = pct(cur, min);
              const reached = min > 0 ? cur >= min : cur > 0;

              return (
                <div
                  key={String(m.planMealId)}
                  className="rounded-2xl border border-slate-200 bg-white p-4"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="font-extrabold text-slate-900">
                      {m.name}
                    </div>
                    <span
                      className={[
                        "rounded-full px-2 py-0.5 text-xs font-semibold",
                        m.primary
                          ? "bg-indigo-50 text-indigo-700"
                          : "bg-slate-100 text-slate-700",
                      ].join(" ")}
                    >
                      {m.primary ? "Primary" : "Optional"}
                    </span>
                  </div>

                  <div className="mt-1 text-xs text-slate-500 line-clamp-2">
                    {m.description}
                  </div>

                  <div className="mt-3 flex items-center justify-between text-xs">
                    <div className="text-slate-600">
                      Current: <b className="text-slate-900">{cur}</b>
                    </div>
                    <div className="text-slate-600">
                      Min: <b className="text-slate-900">{min}</b>
                    </div>
                    <div
                      className={
                        reached
                          ? "text-emerald-700 font-bold"
                          : "text-amber-700 font-bold"
                      }
                    >
                      {min === 0
                        ? "No threshold"
                        : reached
                          ? "Reached"
                          : "Not yet"}
                    </div>
                  </div>

                  {min > 0 ? (
                    <div className="mt-2">
                      <div className="h-2 w-full rounded-full bg-slate-100 overflow-hidden">
                        <div
                          className="h-2 rounded-full bg-slate-900"
                          style={{ width: `${p}%` }}
                        />
                      </div>
                      <div className="mt-1 text-xs text-slate-500">{p}%</div>
                    </div>
                  ) : null}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Delivery schedules */}
      <div className="provider-panel">
        <div className="provider-panel-title">
          <FiCalendar className="provider-icon" /> Delivery Schedules
        </div>

        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="provider-label">Start</label>
            <input
              className="provider-input w-[180px]"
              type="date"
              value={start}
              onChange={(e) => setStart(e.target.value)}
            />
          </div>
          <div>
            <label className="provider-label">End</label>
            <input
              className="provider-input w-[180px]"
              type="date"
              value={end}
              onChange={(e) => setEnd(e.target.value)}
            />
          </div>
          <button
            type="button"
            className="provider-btn provider-btn-primary"
            onClick={() => setScheduleOpen(true)}
            disabled={!planId}
          >
            Open Calendar
          </button>
        </div>
        <div className="mt-2 text-xs text-slate-500">
          {planId
            ? `Period: ${formatYmdJp(start)} ~ ${formatYmdJp(end)}`
            : "Plan ID missing."}
        </div>
      </div>

      <DeliveryScheduleViewModal
        open={scheduleOpen}
        onClose={() => setScheduleOpen(false)}
        target={scheduleTarget}
        monthStart={start}
        monthEnd={end}
        formatToJapaneseDateTime={formatToJapaneseDateTime}
      />

      {alertMsg ? (
        <AlertModal
          open={alertOpen}
          onClose={() => setAlertOpen(false)}
          msg={alertMsg}
          isSuccess={alertSuccess}
        />
      ) : null}
    </div>
  );
};

export default ProviderInsightsPage;
