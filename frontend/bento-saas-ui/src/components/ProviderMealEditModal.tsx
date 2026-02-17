import { useEffect, useMemo, useRef, useState } from "react";
import z from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import {
  FiX,
  FiPlus,
  FiTrash2,
  FiSave,
  FiUpload,
  FiImage,
  FiCheckCircle,
  FiAlertTriangle,
} from "react-icons/fi";

import {
  apiGet,
  apiPost,
  apiPut,
  apiDelete,
  isApiError,
} from "../lib/api/http";
import PageLoading from "../components/PageLoading";
import type { PlanResponseDto } from "../lib/api/types";
import "../pages/provider/ProviderPlanCreatePage.css"; // reuse base provider styles

type Props = {
  open: boolean;
  onClose: () => void;
  planId: string;
  plan: PlanResponseDto | null;
  onUpdatedPlan: (p: PlanResponseDto) => void;
  onToast: (msg: string, ok: boolean) => void;
};

const MealEditSchema = z.object({
  name: z.string().min(1, "Meal name is required").max(20),
  description: z.string().max(50),
  pricePerMonth: z.number().min(0, "Price must be >= 0"),
  primary: z.boolean(),
  minSubCount: z.number().min(0).optional(),
  imageKey: z.string().optional(), // image key
});

type MealEditForm = z.infer<typeof MealEditSchema>;

const uniqueFilename = (original: string) => {
  const ts = Date.now();
  const rand = Math.floor(Math.random() * 10000);
  const ext = original.split(".").pop();
  const base = original.replace(/\.[^/.]+$/, "");
  return `${base}_${ts}_${rand}.${ext}`;
};

const getImageKeyFromUrl = (value?: string) => {
  if (!value) return "";
  if (!value.startsWith("http")) return value;

  try {
    const { pathname } = new URL(value);
    return pathname.startsWith("/")
      ? pathname.slice(pathname.lastIndexOf("/") + 1)
      : pathname;
  } catch {
    return value;
  }
};

const ProviderMealEditModal = ({
  open,
  onClose,
  planId,
  plan,
  onUpdatedPlan,
  onToast,
}: Props) => {
  const meals = useMemo(() => {
    return (
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
      )?.planMealResponseDtos ?? []
    );
  }, [plan]);

  const [selectedMealId, setSelectedMealId] = useState<string | null>(null);
  const selectedMeal = useMemo(
    () =>
      meals.find((m) => String(m.planMealId) === String(selectedMealId)) ??
      null,
    [meals, selectedMealId],
  );

  const [mode, setMode] = useState<"edit" | "add">("edit");

  const [busy, setBusy] = useState(false);

  // image upload
  const [uploading, setUploading] = useState(false);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  const existingPrimaryCount = meals.filter(
    (m) => !!m.primary && String(m.planMealId) !== String(selectedMealId),
  ).length;

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isValid },
  } = useForm<MealEditForm>({
    resolver: zodResolver(MealEditSchema),
    mode: "onChange",
    defaultValues: {
      name: "",
      description: "",
      pricePerMonth: 0,
      primary: false,
      minSubCount: 0,
      imageKey: "",
    },
  });

  const currentPrimary = !!watch("primary");

  useEffect(() => {
    if (!open) return;

    // default selection
    if (meals.length > 0) {
      setMode("edit");
      setSelectedMealId(String(meals[0].planMealId));
    } else {
      setMode("add");
      setSelectedMealId(null);
    }
  }, [open, meals]);

  useEffect(() => {
    if (!open) return;

    if (mode === "edit" && selectedMeal) {
      reset({
        name: selectedMeal.name ?? "",
        description: selectedMeal.description ?? "",
        pricePerMonth: Number(selectedMeal.pricePerMonth ?? 0),
        primary: !!selectedMeal.primary,
        minSubCount: Number(selectedMeal.minSubCount ?? 0),
        imageKey: getImageKeyFromUrl(selectedMeal.image),
      });

      setImagePreview(
        typeof selectedMeal.image === "string" &&
          selectedMeal.image.includes("http")
          ? selectedMeal.image
          : null,
      );
      setUploading(false);
    }

    if (mode === "add") {
      reset({
        name: "",
        description: "",
        pricePerMonth: 0,
        primary: meals.length === 0, // first meal default primary
        minSubCount: 0,
        imageKey: "",
      });
      setImagePreview(null);
      setUploading(false);
    }
  }, [open, mode, selectedMeal, reset, meals.length]);

  const closeSafe = () => {
    if (busy || uploading) return;
    onClose();
  };

  const validatePrimaryInvariant = (nextPrimaryForThisMeal: boolean) => {
    const totalPrimary =
      existingPrimaryCount + (nextPrimaryForThisMeal ? 1 : 0);
    return totalPrimary >= 1;
  };

  const uploadMealImage = async (file: File) => {
    if (!file.type.startsWith("image/"))
      throw new Error("Please select an image file");
    if (file.size > 5 * 1024 * 1024)
      throw new Error("Image size must be less than 5MB");

    setUploading(true);
    try {
      const name = uniqueFilename(file.name);

      const presign = await apiPost<{ url: string; file: string }>(
        `/plan-management/api/v1/provider/plan/file/meal?filename=${encodeURIComponent(name)}`,
      );

      const res = await fetch(presign.url, {
        method: "PUT",
        body: file,
        headers: { "Content-Type": file.type },
      });

      if (!res.ok) throw new Error("Failed to upload meal image");

      const previewUrl = URL.createObjectURL(file);
      setImagePreview(previewUrl);
      setValue("imageKey", presign.file, {
        shouldValidate: true,
        shouldDirty: true,
      });

      onToast("Meal image uploaded. (Save to persist)", true);
    } finally {
      setUploading(false);
    }
  };

  const onSelectFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      await uploadMealImage(file);
    } catch (err: unknown) {
      onToast(err instanceof Error ? err.message : "Upload failed", false);
    } finally {
      e.target.value = "";
    }
  };

  const saveEdit = async (values: MealEditForm) => {
    if (!selectedMealId) return;

    // invariant: must have at least one primary overall
    if (!validatePrimaryInvariant(values.primary)) {
      onToast(
        "At least one primary meal is required. You cannot remove the last primary.",
        false,
      );
      return;
    }

    setBusy(true);
    try {
      const payload: MealEditForm = {
        name: values.name,
        description: values.description,
        pricePerMonth: values.pricePerMonth,
        primary: values.primary,
        minSubCount: values.minSubCount,
        imageKey: values.imageKey || undefined,
      };

      // PUT /v1/provider/plan/meal?planId=...&mealId=...
      const updated = await apiPut<PlanResponseDto>(
        `/plan-management/api/v1/provider/plan/meal?planId=${encodeURIComponent(planId)}&mealId=${encodeURIComponent(
          selectedMealId,
        )}`,
        payload,
      );

      onUpdatedPlan(updated);
      onToast("Meal updated.", true);
    } catch (e: unknown) {
      onToast(isApiError(e) ? e.message : "Failed to update meal", false);
    } finally {
      setBusy(false);
    }
  };

  const addMeal = async (values: MealEditForm) => {
    // invariant: must have at least one primary overall
    const currentPrimaryCount = meals.filter((m) => !!m.primary).length;
    if (currentPrimaryCount === 0 && !values.primary) {
      onToast(
        "First meal must be Primary (at least one primary required).",
        false,
      );
      return;
    }

    setBusy(true);
    try {
      const payload: MealEditForm = {
        name: values.name,
        description: values.description,
        pricePerMonth: values.pricePerMonth,
        primary: values.primary,
        minSubCount: values.minSubCount,
        imageKey: values.imageKey || undefined,
      };

      // POST /v1/provider/plan/meal/{planId}
      const updated = await apiPost<PlanResponseDto>(
        `/plan-management/api/v1/provider/plan/meal/${planId}`,
        payload,
      );

      onUpdatedPlan(updated);

      // select newly added meal if possible (take last)
      const last = updated.planMealResponseDtos?.slice(-1)?.[0];
      if (last?.planMealId) {
        setMode("edit");
        setSelectedMealId(String(last.planMealId));
      }

      onToast("Meal added.", true);
    } catch (e: unknown) {
      onToast(isApiError(e) ? e.message : "Failed to add meal", false);
    } finally {
      setBusy(false);
    }
  };

  const deleteMeal = async () => {
    if (!selectedMealId) return;

    const isLastMeal = meals.length <= 1;
    if (isLastMeal) {
      onToast("You must have at least 1 meal.", false);
      return;
    }

    // invariant: if this is the last primary, prevent delete
    const thisIsPrimary = !!selectedMeal?.primary;
    const remainingPrimary = meals.filter(
      (m) => !!m.primary && String(m.planMealId) !== String(selectedMealId),
    ).length;
    if (thisIsPrimary && remainingPrimary === 0) {
      onToast("You cannot delete the last primary meal.", false);
      return;
    }

    if (!confirm("Remove this meal?")) return;

    setBusy(true);
    try {
      const updated = await apiDelete<PlanResponseDto | void>(
        `/plan-management/api/v1/provider/plan/meal?planId=${encodeURIComponent(planId)}&mealId=${encodeURIComponent(
          selectedMealId,
        )}`,
      );

      if (updated && updated.planMealResponseDtos) {
        onUpdatedPlan(updated as PlanResponseDto);
      } else {
        // fallback: manually refetch plan
        const fresh = await apiGet<PlanResponseDto>(
          `/plan-management/api/v1/plan/${planId}`,
        );
        onUpdatedPlan(fresh);
      }

      // pick next meal
      const list =
        updated?.planMealResponseDtos ??
        meals.filter((m) => String(m.planMealId) !== String(selectedMealId));
      const next = list[0];
      setSelectedMealId(next ? String(next.planMealId) : null);

      onToast("Meal removed.", true);
    } catch (e: unknown) {
      onToast(isApiError(e) ? e.message : "Failed to remove meal", false);
    } finally {
      setBusy(false);
    }
  };

  if (!open) return null;

  const primaryInvariantOk = validatePrimaryInvariant(currentPrimary);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-4xl rounded-2xl bg-white shadow-xl border border-slate-200 overflow-hidden">
        {/* header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-200 bg-slate-50">
          <div>
            <div className="text-lg font-extrabold text-slate-900">
              Edit Meals
            </div>
            <div className="text-xs text-slate-500">
              Uses separate endpoints (add/update/delete). Must keep at least
              one primary meal.
            </div>
          </div>
          <button
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 font-semibold hover:bg-slate-50"
            onClick={closeSafe}
            aria-label="Close"
            disabled={busy || uploading}
          >
            <FiX />
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-[280px_1fr]">
          {/* left list */}
          <div className="border-b md:border-b-0 md:border-r border-slate-200 bg-white">
            <div className="p-4">
              <div className="flex gap-2">
                <button
                  type="button"
                  className={`provider-btn ${mode === "edit" ? "provider-btn-primary" : ""}`}
                  onClick={() => setMode("edit")}
                  disabled={meals.length === 0}
                >
                  Edit
                </button>
                <button
                  type="button"
                  className={`provider-btn ${mode === "add" ? "provider-btn-primary" : ""}`}
                  onClick={() => {
                    setMode("add");
                    setSelectedMealId(null);
                  }}
                >
                  <FiPlus className="inline" /> Add
                </button>
              </div>

              <div className="mt-3 text-xs text-slate-500">
                Meals: <b className="text-slate-900">{meals.length}</b> •
                Primary:{" "}
                <b
                  className={
                    meals.some((m) => m.primary)
                      ? "text-emerald-600"
                      : "text-rose-600"
                  }
                >
                  {meals.filter((m) => m.primary).length}
                </b>
              </div>
            </div>

            <div className="max-h-[420px] overflow-auto px-2 pb-3">
              {meals.length === 0 ? (
                <div className="px-3 pb-4 text-sm text-slate-500">
                  No meals yet. Add one.
                </div>
              ) : (
                meals.map((m) => {
                  const active =
                    String(m.planMealId) === String(selectedMealId);
                  return (
                    <button
                      key={String(m.planMealId)}
                      type="button"
                      onClick={() => {
                        setMode("edit");
                        setSelectedMealId(String(m.planMealId));
                      }}
                      className={[
                        "w-full text-left rounded-xl px-3 py-2 mb-2 border",
                        active
                          ? "border-slate-900 bg-slate-50"
                          : "border-slate-200 hover:bg-slate-50",
                      ].join(" ")}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <div className="font-semibold text-slate-800">
                          {m.name}
                        </div>
                        <span
                          className={[
                            "rounded-full px-2 py-0.5 text-xs font-semibold",
                            m.primary
                              ? "bg-indigo-50 text-indigo-700"
                              : "bg-slate-100 text-slate-600",
                          ].join(" ")}
                        >
                          {m.primary ? "Primary" : "Opt"}
                        </span>
                      </div>
                      <div className="text-xs text-slate-500 line-clamp-1">
                        {m.description}
                      </div>
                    </button>
                  );
                })
              )}
            </div>
          </div>

          {/* right editor */}
          <div className="p-5">
            <form onSubmit={handleSubmit(mode === "add" ? addMeal : saveEdit)}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-sm font-extrabold text-slate-900">
                    {mode === "add" ? "Add New Meal" : "Edit Meal"}
                  </div>

                  <div className="mt-1 text-xs">
                    {primaryInvariantOk ? (
                      <span className="inline-flex items-center gap-1 text-emerald-700">
                        <FiCheckCircle /> Primary invariant OK
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-rose-600 font-semibold">
                        <FiAlertTriangle /> Cannot remove last primary
                      </span>
                    )}
                  </div>
                </div>

                {mode === "edit" ? (
                  <button
                    type="button"
                    className="provider-btn provider-btn-danger"
                    onClick={deleteMeal}
                    disabled={busy || uploading}
                  >
                    <FiTrash2 className="inline" /> Remove
                  </button>
                ) : null}
              </div>

              {/* image */}
              <div className="mt-4 flex flex-col gap-3">
                <input
                  type="hidden"
                  value={watch("imageKey") ?? ""}
                  {...register("imageKey")}
                />

                <div className="h-64 rounded-xl border-3 border-dashed border-slate-200 bg-slate-50 flex items-center justify-center overflow-hidden object-cover">
                  {uploading ? (
                    <div className="provider-loading-container">
                      <PageLoading text="Uploading..." />
                    </div>
                  ) : imagePreview ? (
                    <img
                      src={imagePreview}
                      alt="Meal preview"
                      className="provider-image-preview"
                      onError={() => setImagePreview(null)}
                    />
                  ) : (
                    <div className="provider-image-placeholder">
                      <FiImage
                        size={42}
                        className="provider-placeholder-icon"
                      />
                      <div>No image</div>
                    </div>
                  )}
                </div>

                <div className="flex flex-col gap-2 items-center">
                  <input
                    ref={fileRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={onSelectFile}
                  />

                  <button
                    type="button"
                    className="provider-btn provider-btn-upload w-48 justify-center"
                    onClick={() => fileRef.current?.click()}
                    disabled={busy || uploading}
                  >
                    <FiUpload className="inline" /> Upload Meal Image
                  </button>

                  <div className="text-xs text-slate-500">
                    Key saved into <span className="font-mono">imageKey</span>{" "}
                    (save to persist)
                  </div>

                  <div className="text-xs text-slate-400 font-mono break-all">
                    key: {watch("imageKey") || "—"}
                  </div>
                </div>
              </div>

              {/* fields */}
              <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="provider-label">Name</label>
                  <input className="provider-input" {...register("name")} />
                  {errors.name?.message ? (
                    <div className="provider-err">{errors.name.message}</div>
                  ) : null}
                </div>

                <div>
                  <label className="provider-label">Price per month</label>
                  <input
                    className="provider-input"
                    type="number"
                    step="1"
                    {...register("pricePerMonth", { valueAsNumber: true })}
                  />
                  {errors.pricePerMonth?.message ? (
                    <div className="provider-err">
                      {errors.pricePerMonth.message}
                    </div>
                  ) : null}
                </div>

                <div className="md:col-span-2">
                  <label className="provider-label">Description</label>
                  <input
                    className="provider-input"
                    {...register("description")}
                  />
                  {errors.description?.message ? (
                    <div className="provider-err">
                      {errors.description.message}
                    </div>
                  ) : null}
                </div>

                <div>
                  <label className="provider-label">Min Sub Count</label>
                  <input
                    className="provider-input"
                    type="number"
                    step="1"
                    {...register("minSubCount", {
                      valueAsNumber: true,
                      setValueAs: (value) =>
                        value === "" || Number.isNaN(value)
                          ? undefined
                          : Number(value),
                    })}
                  />
                  {errors.minSubCount?.message ? (
                    <div className="provider-err">
                      {errors.minSubCount.message}
                    </div>
                  ) : null}
                </div>

                <div className="flex items-center gap-3">
                  <label className="provider-toggle">
                    <input type="checkbox" {...register("primary")} />
                    <span>Primary</span>
                  </label>
                  <div className="text-xs text-slate-500">
                    Existing other primary:{" "}
                    <b className="text-slate-900">{existingPrimaryCount}</b>
                  </div>
                </div>
              </div>

              <div className="mt-5 flex items-center justify-end gap-2">
                <button
                  type="button"
                  className="provider-btn"
                  onClick={closeSafe}
                  disabled={busy || uploading}
                >
                  Close
                </button>

                <button
                  type="submit"
                  className="provider-btn provider-btn-primary"
                  disabled={
                    busy || uploading || !isValid || !primaryInvariantOk
                  }
                >
                  {busy ? (
                    <span className="inline-flex items-center gap-2">
                      <span className="provider-spinner" /> Saving...
                    </span>
                  ) : (
                    <>
                      <FiSave className="inline" />{" "}
                      {mode === "add" ? "Add Meal" : "Save Meal"}
                    </>
                  )}
                </button>
              </div>

              {!primaryInvariantOk && mode === "edit" ? (
                <div className="mt-3 rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  You are trying to remove the last primary meal. Set another
                  meal as Primary first.
                </div>
              ) : null}
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProviderMealEditModal;
