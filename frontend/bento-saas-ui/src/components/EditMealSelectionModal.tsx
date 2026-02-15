import BaseModal from "./BaseModal";
import { useEffect, useState } from "react";
import type {
  UUID,
  SubscriptionRequestDto,
  SubscriptionResponseDto,
  PlanResponseDto,
} from "../lib/api/types";
import { apiPut, isApiError } from "../lib/api/http";
import { AiOutlineUpload } from "react-icons/ai";
import PageLoading from "./PageLoading";

type SubView = {
  sub: SubscriptionResponseDto;
  plan?: PlanResponseDto;
};

type Props = {
  open: boolean;
  onClose: () => void;
  target: SubView | null;
};

const EditMealSelectionModal = ({ open, onClose, target }: Props) => {
  const plan = target?.plan;
  const sub = target?.sub;

  const initialSelected = (sub?.mealSelectionResponseDtos ?? [])
    .map((x) => x.planMealId)
    .filter(Boolean) as UUID[];

  // local state inside modal
  const [selected, setSelected] = useState<UUID[]>(initialSelected);
  const [submitting, setSubmitting] = useState<boolean>(false);

  useEffect(() => {
    setSelected(initialSelected);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [target?.sub?.subscriptionId]);

  const toggle = (id: UUID) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  };

  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const submitEdit = async () => {
    if (!sub?.subscriptionId) return;
    if (!sub.planId || !sub.providedUserId) return;
    if (selected.length === 0) return;

    const req: SubscriptionRequestDto = {
      planId: sub.planId as UUID,
      planMealIds: selected,
      providedUserId: sub.providedUserId as UUID,
    };

    setError(null);
    setSuccess(null);
    setSubmitting(true);

    try {
      await apiPut<SubscriptionResponseDto>(
        `/subscription/api/v1/${sub?.subscriptionId}`,
        req,
      );
      setSuccess("Meal selection updated. (Changes apply next month)");
    } catch (e: unknown) {
      setError(isApiError(e) ? e.message : "Failed to update subscription");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <BaseModal
      open={open}
      onClose={onClose}
      panelClassName="min-w-[520px] px-8 py-6"
    >
      {({ close }) => (
        <div className="flex flex-col gap-4">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-lg font-semibold text-slate-900">
                Edit meal selection
              </h2>
              <p className="mt-1 text-xs text-slate-500">
                Changes apply next month.
              </p>
            </div>

            <button
              className="text-slate-500 hover:text-slate-900"
              onClick={close}
              aria-label="Close"
            >
              ✕
            </button>
          </div>

          {!target ? (
            <div className="text-sm text-slate-600">
              No subscription selected.
            </div>
          ) : !plan ? (
            <div className="text-sm text-slate-600">
              Plan details unavailable (cannot edit meals).
            </div>
          ) : (plan.planMealResponseDtos ?? []).length === 0 ? (
            <div className="text-sm text-slate-600">
              No meals found for this plan.
            </div>
          ) : (
            <div className="max-h-[320px] overflow-auto rounded-xl border border-slate-200 p-3">
              <div className="flex flex-col gap-2">
                {(plan.planMealResponseDtos ?? []).map((m) => {
                  const id = m.planMealId as UUID | undefined;
                  if (!id) return null;

                  const checked = selected.includes(id);

                  return (
                    <label
                      key={id}
                      className={[
                        "flex items-start gap-3 rounded-xl border px-3 py-2 cursor-pointer",
                        checked
                          ? "border-blue-300 bg-blue-50"
                          : "border-slate-200 bg-white",
                      ].join(" ")}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => toggle(id)}
                      />
                      <div className="min-w-0">
                        <div className="text-sm font-semibold text-slate-900">
                          {m.name}{" "}
                          <span className="text-slate-500">
                            — ¥{m.pricePerMonth}
                          </span>
                          {m.primary ? (
                            <span className="ml-2 text-xs text-emerald-700">
                              (Primary)
                            </span>
                          ) : null}
                        </div>
                        <div className="text-xs text-slate-600">
                          {m.description}
                        </div>
                      </div>
                    </label>
                  );
                })}
              </div>
            </div>
          )}

          <div>
            {submitting && <PageLoading text="Updating meal selection..." />}

            {success && (
              <div className="rounded-md bg-emerald-50 p-3 text-sm text-emerald-800">
                {success}
              </div>
            )}

            {error && (
              <div className="rounded-md bg-red-50 p-3 text-sm text-red-800">
                {error}
              </div>
            )}
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
              onClick={close}
              disabled={submitting}
            >
              Cancel
            </button>

            <button
              className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-500 disabled:opacity-60"
              onClick={submitEdit}
              disabled={submitting || !target?.plan || selected.length === 0}
            >
              <AiOutlineUpload className="inline mr-1" /> Save
            </button>
          </div>
        </div>
      )}
    </BaseModal>
  );
};

export default EditMealSelectionModal;
