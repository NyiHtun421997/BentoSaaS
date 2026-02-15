import "./SubscriptionsPage.css";

import { useContext, useEffect, useMemo, useState } from "react";
import AuthContext from "../../state/createContext";
import { apiDelete, apiGet } from "../../lib/api/http";
import { isApiError } from "../../lib/api/http";
import PageLoading from "../../components/PageLoading";
import AlertModal from "../../components/AlertModal";
import EditMealSelectionModal from "../../components/EditMealSelectionModal";
import DeliveryScheduleViewModal from "../../components/DeliveryScheduleViewModal";
import { RiCalendarScheduleLine } from "react-icons/ri";
import { FaEdit } from "react-icons/fa";
import { endOfMonth, startOfMonth, startOfToday } from "date-fns";
import fallbackImg from "../../assets/fallback.png";

import type {
  UUID,
  PlanResponseDto,
  SubscriptionResponseDto,
} from "../../lib/api/types";
import formatToJapaneseDateTime from "../../utilities/formatToJst";
import formatAddressJp from "../../utilities/formatAddressJp";
import formatYmd from "../../utilities/formatYmd";
import { Link } from "react-router-dom";

type SubView = {
  sub: SubscriptionResponseDto;
  plan?: PlanResponseDto;
};

const oneYearAgoYmd = () => {
  const d = new Date();
  d.setFullYear(d.getFullYear() - 1);
  return formatYmd(d);
};

const statusBadge = (status?: string) => {
  switch (status) {
    case "SUBSCRIBED":
      return "bg-emerald-50 text-emerald-700";
    case "APPLIED":
      return "bg-blue-50 text-blue-700";
    case "SUSPENDED":
      return "bg-amber-50 text-amber-700";
    case "CANCELLED":
      return "bg-rose-50 text-rose-700";
    default:
      return "bg-slate-100 text-slate-700";
  }
};

const SubscriptionsPage = () => {
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId;

  // filter
  const [since, setSince] = useState<string>(() => oneYearAgoYmd());

  // list state
  const [items, setItems] = useState<SubView[]>([]);
  const [loading, setLoading] = useState(false);

  // alerts
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openAlert, setOpenAlert] = useState(false);

  // edit modal
  const [openEdit, setOpenEdit] = useState(false);
  const [editTarget, setEditTarget] = useState<SubView | null>(null);

  // schedule modal
  const [openSchedule, setOpenSchedule] = useState(false);
  const [scheduleTarget, setScheduleTarget] = useState<SubView | null>(null);

  const { start: monthStart, end: monthEnd } = useMemo(
    () => ({
      start: startOfMonth(startOfToday()),
      end: endOfMonth(startOfToday()),
    }),
    [],
  );

  useEffect(() => {
    if (error || success) setOpenAlert(true);
  }, [error, success]);

  const fetchAll = async () => {
    if (!userId) {
      setError("Missing userId in auth state (login headers issue)");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // 1) subs
      const subs = await apiGet<SubscriptionResponseDto[]>(
        `/subscription/api/v1/byuseridanddate?userId=${encodeURIComponent(
          userId,
        )}&since=${encodeURIComponent(since)}`,
      );

      // 2) plans (unique)
      const planIds = Array.from(
        new Set(subs.map((s) => s.planId).filter(Boolean) as UUID[]),
      );

      const planEntries = await Promise.all(
        planIds.map(async (pid) => {
          try {
            const plan = await apiGet<PlanResponseDto>(
              `/plan-management/api/v1/plan/${pid}`,
            );
            return [pid, plan] as const;
          } catch {
            return [pid, undefined] as const;
          }
        }),
      );

      const planMap = new Map<UUID, PlanResponseDto | undefined>(planEntries);

      setItems(
        subs.map((sub) => ({
          sub,
          plan: sub.planId ? planMap.get(sub.planId as UUID) : undefined,
        })),
      );
    } catch (e: unknown) {
      setError(isApiError(e) ? e.message : "Failed to load subscriptions");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!userId) return;
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, since]);

  const onCancel = async (subscriptionId?: UUID) => {
    if (!subscriptionId) return;

    setError(null);
    setSuccess(null);

    try {
      await apiDelete(`/subscription/api/v1/${subscriptionId}`);
      setSuccess("Subscription cancelled.");
      await fetchAll(); // ✅ refetch all to reflect updated status
    } catch (e: unknown) {
      setError(isApiError(e) ? e.message : "Failed to cancel subscription");
    }
  };

  const openEditModal = (item: SubView) => {
    setEditTarget(item);
    setOpenEdit(true);
  };

  const openScheduleModal = (item: SubView) => {
    setScheduleTarget(item);
    setOpenSchedule(true);
  };

  if (!userId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Subscriptions</h1>
        <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Please login again (missing userId in auth state).
        </p>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <h1 className="page-title">Subscriptions</h1>
      <p className="page-subtitle">
        Track your active plans and upcoming deliveries.
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
          <PageLoading text="Loading subscriptions…" />
        ) : items.length === 0 ? (
          <p className="text-sm text-slate-500">No subscriptions found.</p>
        ) : (
          <div className="flex flex-col gap-4">
            {items.map((item) => {
              const { sub, plan } = item;
              const subscriptionId = sub.subscriptionId as UUID | undefined;

              return (
                <div
                  key={String(subscriptionId)}
                  className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
                >
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <div className="min-w-0 lg:max-h-[900px] flex-1 p-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <Link
                            to={`/app/plans/${sub.planId}`}
                            className="truncate underline gap-2 text-sm font-semibold text-emerald-700 hover:text-emerald-600"
                          >
                            {plan?.title ?? `Plan ${sub.planId ?? ""}`}
                          </Link>

                          <span
                            className={[
                              "rounded-full px-3 py-1 text-xs font-semibold",
                              statusBadge(sub.subscriptionStatus),
                            ].join(" ")}
                          >
                            {sub.subscriptionStatus ?? "-"}
                          </span>
                        </div>

                        <div className="mt-1 text-xs text-slate-500">
                          Subscription ID:{" "}
                          <span className="font-mono text-slate-700">
                            {subscriptionId}
                          </span>
                        </div>

                        {plan?.address ? (
                          <div className="mt-1 text-xs text-slate-600">
                            {formatAddressJp(plan.address)}
                          </div>
                        ) : null}

                        {typeof plan?.displaySubscriptionFee === "number" ? (
                          <div className="mt-1 text-xs text-slate-600">
                            Display fee: ¥{plan.displaySubscriptionFee}
                          </div>
                        ) : null}
                      </div>

                      {/* Selected meals */}
                      <div className="border-t border-slate-200 pt-4 mt-5">
                        <div className="text-xs font-semibold text-slate-700">
                          Your selected meals
                          <span className="ml-2 font-normal text-slate-500">
                            (Changes apply next month)
                          </span>
                        </div>

                        <div className="mt-2 flex flex-wrap gap-2">
                          {(sub.mealSelectionResponseDtos ?? []).length ===
                          0 ? (
                            <span className="text-xs text-slate-500">
                              No meal selections.
                            </span>
                          ) : (
                            (sub.mealSelectionResponseDtos ?? [])
                              .map((x) => x.planMealId)
                              .filter(Boolean)
                              .map((mid) => {
                                const planMeal =
                                  plan?.planMealResponseDtos?.find(
                                    (m) => m.planMealId === mid,
                                  ) ?? null;

                                const name = planMeal
                                  ? planMeal.name
                                  : `Meal ${String(mid)}`;

                                const imageUrl =
                                  planMeal && planMeal.imageUrl
                                    ? planMeal.imageUrl
                                    : (plan?.imageUrl ?? "");

                                return (
                                  <div
                                    key={String(mid)}
                                    className="rounded-lg bg-slate-100 px-1 py-1 text-xs font-semibold text-slate-700 text-center"
                                  >
                                    <img
                                      src={imageUrl || fallbackImg}
                                      className="h-30 w-40 object-cover rounded-md mb-1"
                                      alt="Meal image"
                                    />
                                    {name}
                                  </div>
                                );
                              })
                          )}
                        </div>

                        <div className="flex shrink-0 flex-col gap-2">
                          <div className="mt-3 flex flex-wrap gap-2">
                            <button
                              type="button"
                              className="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                              onClick={() => openScheduleModal(item)}
                            >
                              <RiCalendarScheduleLine className="inline mr-1" />
                              View schedule
                            </button>

                            <button
                              type="button"
                              className="rounded-xl bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-500 disabled:opacity-60"
                              onClick={() => openEditModal(item)}
                              disabled={sub.subscriptionStatus === "CANCELLED"}
                            >
                              <FaEdit className="inline mr-1" />
                              Edit meals
                            </button>

                            <button
                              type="button"
                              className="rounded-xl bg-rose-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-rose-500 disabled:opacity-60"
                              onClick={() => onCancel(subscriptionId)}
                              disabled={sub.subscriptionStatus === "CANCELLED"}
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div
                      className="relative lg:w-3/5 lg:max-h-96 md:max-h-[500px] overflow-hidden rounded-lg border border-slate-200 shadow-md
                    flex items-center justify-center"
                    >
                      <img
                        src={plan?.imageUrl || fallbackImg}
                        alt="Plan image"
                        className="h-full w-full object-cover"
                      />
                    </div>
                  </div>
                </div>
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

      {/* Edit meals modal */}
      <EditMealSelectionModal
        open={openEdit}
        onClose={async () => {
          setOpenEdit(false);
          setEditTarget(null);
          await fetchAll();
        }}
        target={editTarget}
      />

      {/* Schedule modal */}
      <DeliveryScheduleViewModal
        open={openSchedule}
        onClose={() => {
          setOpenSchedule(false);
          setScheduleTarget(null);
        }}
        target={scheduleTarget}
        monthStart={monthStart}
        monthEnd={monthEnd}
        formatToJapaneseDateTime={formatToJapaneseDateTime}
      />
    </div>
  );
};

export default SubscriptionsPage;
