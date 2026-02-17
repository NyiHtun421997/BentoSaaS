import { useContext, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import AuthContext from "../../state/createContext";
import { apiDelete, apiGet, isApiError } from "../../lib/api/http";
import PageLoading from "../../components/PageLoading";
import AlertModal from "../../components/AlertModal";
import type { PlanResponseDto, UUID } from "../../lib/api/types";
import "./ProviderPlansPage.css";

const ProviderPlansPage = () => {
  const navigate = useNavigate();
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId as UUID | undefined;

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMsg, setAlertMsg] = useState<string | null>(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  const {
    data: plans,
    isLoading,
    refetch,
    error,
  } = useQuery({
    queryKey: ["providerPlans", userId],
    enabled: !!userId,
    queryFn: async () => {
      return apiGet<PlanResponseDto[]>(
        `/plan-management/api/v1/plan/byuserid?userId=${encodeURIComponent(
          String(userId),
        )}&page=0&size=50`,
      );
    },
  });

  const sortedPlans = useMemo(() => {
    const arr = plans ?? [];
    return [...arr].sort((a, b) => (a.code ?? "").localeCompare(b.code ?? ""));
  }, [plans]);

  const onCancelPlan = async (planId: UUID) => {
    const ok = window.confirm("Cancel (soft delete) this plan?");
    if (!ok) return;

    setAlertMsg(null);
    try {
      await apiDelete(`/plan-management/api/v1/provider/plan/${planId}`);
      setAlertMsg("Plan cancelled.");
      setAlertSuccess(true);
      setAlertOpen(true);
      refetch();
    } catch (e: unknown) {
      setAlertMsg(isApiError(e) ? e.message : "Failed to cancel plan");
      setAlertSuccess(false);
      setAlertOpen(true);
    }
  };

  return (
    <div className="page-shell">
      <div className="provider-plans-header">
        <div>
          <h1 className="page-title">Provider Plans</h1>
          <p className="page-subtitle">
            Manage your plan catalog and meal offerings.
          </p>
        </div>

        <div className="provider-plans-actions">
          <button
            className="provider-plans-btn provider-plans-btn-primary"
            onClick={() => navigate("/provider/plans/new")}
            disabled={!userId}
          >
            + Create Plan
          </button>
        </div>
      </div>

      <div className="provider-plans-panel">
        {!userId ? (
          <div className="text-sm text-rose-600">
            Missing userId. Please login again.
          </div>
        ) : isLoading ? (
          <PageLoading text="Loading your plans..." />
        ) : error ? (
          <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
            {isApiError(error) ? error.message : "Failed to load plans"}
          </div>
        ) : sortedPlans.length === 0 ? (
          <div className="text-sm text-slate-500">
            No plans yet. Click <b>Create Plan</b> to add your first one.
          </div>
        ) : (
          <div className="provider-plans-grid">
            {sortedPlans.map((p) => (
              <div key={String(p.planId)} className="provider-plan-card">
                <div className="provider-plan-media">
                  {/* backend returns presigned GET already, so just use it */}
                  <img
                    src={p.image ?? ""}
                    alt={p.title ?? "Plan"}
                    className="provider-plan-img"
                    onError={(e) => {
                      (e.currentTarget as HTMLImageElement).src =
                        "/fallback.png";
                    }}
                  />
                </div>

                <div className="provider-plan-body">
                  <div className="provider-plan-top">
                    <div>
                      <div className="provider-plan-title">{p.title}</div>
                      <div className="provider-plan-sub">
                        <span className="font-mono">{p.code ?? "-"}</span> •{" "}
                        <span className="provider-plan-status">
                          {p.status ?? "-"}
                        </span>
                      </div>
                    </div>
                    <div className="provider-plan-fee">
                      ¥
                      {Math.round(
                        Number(p.displaySubscriptionFee ?? 0),
                      ).toLocaleString("ja-JP")}
                      <span className="provider-plan-fee-sub">/month</span>
                    </div>
                  </div>

                  <div className="provider-plan-desc">{p.description}</div>

                  <div className="provider-plan-meta">
                    <div className="text-xs text-slate-500">
                      Meals: {(p.planMealResponseDtos?.length ?? 0).toString()}
                    </div>
                    <div className="provider-plan-btns">
                      <button
                        className="provider-plans-btn"
                        onClick={() =>
                          navigate(`/provider/plans/${p.planId}/edit`)
                        }
                      >
                        Edit
                      </button>
                      <button
                        className="provider-plans-btn provider-plans-btn-danger"
                        onClick={() => onCancelPlan(p.planId as UUID)}
                      >
                        Cancel
                      </button>
                      <button
                        className="provider-plans-btn"
                        onClick={() =>
                          navigate(`/provider/insights/${p.planId}`)
                        }
                      >
                        Insights
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

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

export default ProviderPlansPage;
