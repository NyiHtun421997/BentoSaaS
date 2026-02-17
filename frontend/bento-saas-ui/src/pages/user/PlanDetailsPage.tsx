import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { apiGet, apiPost } from "../../lib/api/http";
import { useContext } from "react";
import AuthContext from "../../state/createContext";
import "./PlanDetailsPage.css";
import { FiHelpCircle } from "react-icons/fi";
import PageLoading from "../../components/PageLoading";
import { isApiError } from "../../lib/api/http";
import AlertModal from "../../components/AlertModal";
import type {
  PlanMealResponseDto,
  PlanResponseDto,
  SubscriptionRequestDto,
  SubscriptionResponseDto,
  UserResponseDTO,
} from "../../lib/api/types";
import ProviderUserInfoModal from "../../components/ProviderUserInfoModal";
import formatAddressJp from "../../utilities/formatAddressJp";
import fallbackImg from "../../assets/fallback.png";

const PlanDetailsPage = () => {
  const { planId } = useParams();
  const navigate = useNavigate();

  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId;

  const [plan, setPlan] = useState<PlanResponseDto | null>(null);
  const [selectedMealIds, setSelectedMealIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [openAlert, setOpenAlert] = useState(false);
  const [openProviderInfo, setOpenProviderInfo] = useState(false);

  const mealIdSet = useMemo(() => new Set(selectedMealIds), [selectedMealIds]);

  useEffect(() => {
    if (!planId) return;

    const load = async () => {
      setLoading(true);
      setError(null);
      setSuccess(null);
      try {
        const data = await apiGet<PlanResponseDto>(
          `/plan-management/api/v1/plan/${planId}`,
        );
        setPlan(data);
        setSelectedMealIds([]); // start empty; user chooses
      } catch (e: Error | unknown) {
        setError(isApiError(e) ? e.message : "Failed to load plan details");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [planId]);

  const toggleMeal = (id: string) => {
    setSelectedMealIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  };

  const applyToPlan = async () => {
    setError(null);
    setSuccess(null);

    if (!plan) {
      setError("Plan not loaded");
      setOpenAlert(true);
      return;
    }
    if (!userId) {
      setError("Missing userId in auth state (login headers issue)");
      setOpenAlert(true);
      return;
    }
    if (selectedMealIds.length === 0) {
      setError("Select at least 1 meal");
      setOpenAlert(true);
      return;
    }
    if (!plan.planId || !plan.providerUserId) {
      setError("Plan details are incomplete. Please refresh and try again.");
      setOpenAlert(true);
      return;
    }

    const req: SubscriptionRequestDto = {
      planId: plan.planId,
      planMealIds: selectedMealIds,
      providedUserId: plan.providerUserId,
    };

    setSubmitting(true);
    try {
      const res = await apiPost<SubscriptionResponseDto>(
        "/subscription/api/v1",
        req,
        { "X-USER-ID": userId },
      );

      setSuccess(`Applied! subscriptionId = ${res.subscriptionId}`);
      setOpenAlert(true);
    } catch (e: Error | unknown) {
      setError(isApiError(e) ? e.message : "Failed to apply");
      setOpenAlert(true);
    } finally {
      setSubmitting(false);
    }
  };

  const fetchProviderInfo = useCallback(
    () => apiGet<UserResponseDTO>(`/user/api/v1/${plan?.providerUserId}`),
    [plan?.providerUserId],
  );

  if (loading) {
    return (
      <div className="page-shell">
        <PageLoading text="Loading plan details…" />
      </div>
    );
  }

  if (error && !plan) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Plan Details</h1>
        <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
          {error}
        </p>
        <button
          onClick={() => navigate(-1)}
          className="mt-4 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700"
        >
          Back
        </button>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <button
        onClick={() => navigate(-1)}
        className="text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        ← Back
      </button>

      <h1 className="page-title mt-3">Plan Details</h1>

      {plan ? (
        <div className="plan-details-panel">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h2 className="text-xl font-semibold">{plan.title}</h2>
            </div>
            <div className="flex flex-col justify-center items-center gap-2">
              <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                {plan.status}
              </span>

              <button
                type="button"
                className="bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-100 rounded-full transition"
                onClick={() => setOpenProviderInfo(true)}
              >
                Provider Info
              </button>
            </div>
          </div>

          <p className="mt-2 text-sm text-slate-600">{plan.description}</p>

          <div className="mt-4 space-y-1 text-sm text-slate-700">
            <p>
              <span className="font-semibold">Code:</span> {plan.code}
            </p>
            <p>
              <span className="font-semibold">Display fee:</span> ¥
              {plan.displaySubscriptionFee}
            </p>
            <p>
              <span className="font-semibold">Address:</span>
              {formatAddressJp(plan.address)}
            </p>
          </div>

          <div className="mt-6 border-t border-slate-700 pt-4">
            <h3 className="text-sm font-semibold text-slate-700">
              Select meals
            </h3>

            <div className="mt-3 flex flex-col gap-3">
              {plan.planMealResponseDtos?.map((m: PlanMealResponseDto) => {
                const mealId = m.planMealId;
                if (!mealId) return null;

                return (
                  <label
                    key={mealId}
                    className={`meal-card ${mealIdSet.has(mealId) ? "meal-card-selected" : ""}`}
                  >
                    <div>
                      <div className="text-sm font-semibold text-slate-800">
                        {m.name} — ¥{m.pricePerMonth}
                        {m.primary ? " (Primary)" : ""}
                      </div>
                      <div className="text-sm text-slate-600">
                        {m.description}
                      </div>
                      {typeof m.currentSubCount === "number" &&
                      typeof m.minSubCount === "number" ? (
                        <>
                          <div className="mt-1 text-xs text-slate-500">
                            Subscribers: {m.currentSubCount}人
                          </div>
                          <div className="mt-1 flex items-end gap-1 text-xs text-slate-500">
                            <span>Minimum Required: {m.minSubCount}人</span>

                            <span className="relative group">
                              <FiHelpCircle
                                className="h-4 w-4 text-slate-400"
                                aria-hidden="true"
                              />

                              <span className="pointer-events-none absolute left-1/2 top-full z-10 mt-2 w-56 -translate-x-1/2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-[11px] text-slate-600 shadow-lg opacity-0 transition group-hover:opacity-100">
                                Minimum required is the threshold number of
                                subscribers needed for this meal plan to
                                activate.
                              </span>
                            </span>
                          </div>
                        </>
                      ) : null}
                    </div>

                    <img
                      src={m.image || fallbackImg}
                      alt={m.name}
                      className="mt-4 w-full h-90 rounded-xl object-cover shadow-md"
                    />

                    <div className="flex gap-2 items-center justify-around w-full">
                      <button
                        type="button"
                        onClick={() => toggleMeal(mealId)}
                        className={`mt-2 w-1/3 text-sm font-semibold ${mealIdSet.has(mealId) ? "bg-red-600 hover:bg-red-500" : "bg-blue-500 hover:bg-blue-400"} text-white px-3 py-1 rounded-lg`}
                      >
                        {mealIdSet.has(mealId) ? "Remove" : "Add"}
                      </button>
                    </div>
                  </label>
                );
              })}
            </div>
          </div>

          <div className="mt-5 flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={applyToPlan}
              disabled={submitting}
              className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:bg-emerald-400"
            >
              {submitting ? "Applying…" : "Apply to this plan"}
            </button>
            <button
              type="button"
              onClick={() => navigate("/app/subscriptions")}
              className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700"
            >
              Go to Subscriptions
            </button>
          </div>

          {error ? (
            <AlertModal
              open={openAlert}
              onClose={() => setOpenAlert(false)}
              msg={error}
              isSuccess={false}
            />
          ) : null}

          {success ? (
            <AlertModal
              open={openAlert}
              onClose={() => setOpenAlert(false)}
              msg={success}
              isSuccess={true}
            />
          ) : null}

          {openProviderInfo ? (
            <ProviderUserInfoModal
              open={openProviderInfo}
              fetchProviderInfo={fetchProviderInfo}
              onClose={() => setOpenProviderInfo(false)}
            />
          ) : null}
        </div>
      ) : null}
    </div>
  );
};

export default PlanDetailsPage;
