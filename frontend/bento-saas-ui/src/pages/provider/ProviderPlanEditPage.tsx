import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import z from "zod";
import {
  FiArrowLeft,
  FiCalendar,
  FiDollarSign,
  FiFileText,
  FiImage,
  FiMapPin,
  FiTag,
  FiUpload,
  FiEdit3,
  FiPackage,
  FiSave,
} from "react-icons/fi";

import AuthContext from "../../state/createContext";
import { apiGet, apiPost, apiPut, isApiError } from "../../lib/api/http";
import AlertModal from "../../components/AlertModal";
import PageLoading from "../../components/PageLoading";
import ProviderMealEditModal from "../../components/ProviderMealEditModal";

import type { CategoryDto, PlanResponseDto } from "../../lib/api/types";
import "./ProviderPlanEditPage.css";
import LoadingButton from "../../components/LoadingButton";

// -------------------- schema (root fields only) --------------------
const GeoPointSchema = z.object({
  latitude: z.number(),
  longitude: z.number(),
});

const AddressSchema = z.object({
  postalCode: z
    .string()
    .min(1, "Postal code is required")
    .regex(/^\d{3}-\d{4}$/, {
      message: "Postal code must be in the format 123-4567",
    }),
  prefecture: z.string().min(1, "Prefecture is required"),
  city: z.string().min(1, "City is required"),
  district: z.string().min(1, "District is required"),
  chomeBanGo: z.string().min(1, "Chome/Ban/Go is required"),
  buildingNameRoomNo: z
    .string()
    .min(1, "Building name/room number is required"),
  location: GeoPointSchema,
});

const PlanRootSchema = z.object({
  title: z.string().min(1, "Title is required").max(20),
  description: z.string().min(1, "Description is required").max(50),
  categoryIds: z.array(z.string()).min(1, "Select at least 1 category"),
  displaySubscriptionFee: z.number().min(0, "Fee must be >= 0"),
  skipDays: z.array(z.string()).max(2, "Skip days must be at most 2"),
  imageKey: z.string().optional(), // plan image key
  address: AddressSchema,
});

type PlanRootForm = z.infer<typeof PlanRootSchema>;

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

const ProviderPlanEditPage = () => {
  const navigate = useNavigate();
  const { planId } = useParams();
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId;

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMsg, setAlertMsg] = useState<string | null>(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  const [categories, setCategories] = useState<CategoryDto[]>([]);
  const [loadingCats, setLoadingCats] = useState(false);

  const [plan, setPlan] = useState<PlanResponseDto | null>(null);

  // plan image upload state
  const [uploadingPlanImage, setUploadingPlanImage] = useState(false);
  const [planImagePreview, setPlanImagePreview] = useState<string | null>(null);
  const planFileRef = useRef<HTMLInputElement>(null);

  // meals modal
  const [mealModalOpen, setMealModalOpen] = useState(false);

  const showAlert = (msg: string, ok: boolean) => {
    setAlertMsg(msg);
    setAlertSuccess(ok);
    setAlertOpen(true);
  };

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isValid },
  } = useForm<PlanRootForm>({
    resolver: zodResolver(PlanRootSchema),
    mode: "onChange",
    defaultValues: {
      title: "",
      description: "",
      categoryIds: [],
      displaySubscriptionFee: 0,
      skipDays: [],
      imageKey: "",
      address: {
        postalCode: "",
        prefecture: "",
        city: "",
        district: "",
        chomeBanGo: "",
        buildingNameRoomNo: "",
        location: { latitude: 0, longitude: 0 },
      },
    },
  });

  const skipDays = watch("skipDays") ?? [];
  const imageKey = watch("imageKey") || "";

  const canSave = useMemo(() => {
    if (!userId) return false;
    if (!planId) return false;
    if (saving) return false;
    return isValid;
  }, [userId, planId, saving, isValid]);

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

        const imageUrl =
          typeof data.image === "string" && data.image.includes("http");
        setPlanImagePreview(imageUrl ? (data.image as string) : null);

        reset({
          title: data.title ?? "",
          description: data.description ?? "",
          categoryIds: (data.categoryIds ?? []) as string[],
          displaySubscriptionFee: Number(data.displaySubscriptionFee ?? 0),
          skipDays: (data.skipDays ?? []) as string[],
          imageKey: getImageKeyFromUrl(data.image),
          address: {
            postalCode: data.address?.postalCode ?? "",
            prefecture: data.address?.prefecture ?? "",
            city: data.address?.city ?? "",
            district: data.address?.district ?? "",
            chomeBanGo: data.address?.chomeBanGo ?? "",
            buildingNameRoomNo: data.address?.buildingNameRoomNo ?? "",
            location: {
              latitude: Number(data.address?.location?.latitude ?? 0),
              longitude: Number(data.address?.location?.longitude ?? 0),
            },
          },
        });
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
  }, [planId, reset]);

  useEffect(() => {
    let mounted = true;

    const loadCats = async () => {
      setLoadingCats(true);
      try {
        const data = await apiGet<CategoryDto[]>(
          `/plan-management/api/v1/category`,
        );
        if (mounted) setCategories(data ?? []);
      } catch (e: unknown) {
        if (!mounted) return;
        showAlert(
          isApiError(e) ? e.message : "Failed to load categories",
          false,
        );
      } finally {
        if (mounted) setLoadingCats(false);
      }
    };

    void loadCats();
    return () => {
      mounted = false;
    };
  }, []);

  const setGeoFromBrowser = () => {
    if (!navigator.geolocation) {
      showAlert("Geolocation is not supported by this browser.", false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setValue("address.location.latitude", pos.coords.latitude, {
          shouldValidate: true,
        });
        setValue("address.location.longitude", pos.coords.longitude, {
          shouldValidate: true,
        });
        showAlert("Location set from browser.", true);
      },
      () => {
        showAlert(
          "Failed to get location. Please allow location permission.",
          false,
        );
      },
      { enableHighAccuracy: true, timeout: 8000 },
    );
  };

  const uploadPlanImage = async (file: File) => {
    if (!file.type.startsWith("image/"))
      throw new Error("Please select an image file");
    if (file.size > 5 * 1024 * 1024)
      throw new Error("Image size must be less than 5MB");

    setUploadingPlanImage(true);
    try {
      const name = uniqueFilename(file.name);

      const presign = await apiPost<{ url: string; file: string }>(
        `/plan-management/api/v1/provider/plan/file/plan?filename=${encodeURIComponent(name)}`,
      );

      const res = await fetch(presign.url, {
        method: "PUT",
        body: file,
        headers: { "Content-Type": file.type },
      });

      if (!res.ok) throw new Error("Failed to upload plan image");

      const previewUrl = URL.createObjectURL(file);
      setPlanImagePreview(previewUrl);
      setValue("imageKey", presign.file, {
        shouldValidate: true,
        shouldDirty: true,
      });

      showAlert("Plan image uploaded. (Save to persist)", true);
    } finally {
      setUploadingPlanImage(false);
    }
  };

  const onSelectPlanImage = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      await uploadPlanImage(file);
    } catch (err: unknown) {
      showAlert(err instanceof Error ? err.message : "Upload failed", false);
    } finally {
      e.target.value = "";
    }
  };

  const onSaveRoot = async (values: PlanRootForm) => {
    if (!userId) {
      showAlert("Missing userId. Please login again.", false);
      return;
    }
    if (!planId) {
      showAlert("Missing planId.", false);
      return;
    }

    setSaving(true);
    try {
      // root update only: do NOT send planMealRequestDtos here
      const payload: Omit<PlanRootForm, "imageKey"> & { imageKey?: string } = {
        title: values.title,
        description: values.description,
        categoryIds: values.categoryIds,
        displaySubscriptionFee: values.displaySubscriptionFee,
        skipDays: values.skipDays,
        imageKey: values.imageKey || undefined,
        address: values.address,
      };

      const updated = await apiPut<PlanResponseDto>(
        `/plan-management/api/v1/provider/plan/${planId}`,
        payload,
      );

      setPlan(updated);

      // if backend returns pre-signed url for image, keep preview from response
      const img = updated.image;
      if (typeof img === "string" && img.includes("http"))
        setPlanImagePreview(img);

      showAlert("Plan updated.", true);
    } catch (e: unknown) {
      showAlert(isApiError(e) ? e.message : "Failed to update plan", false);
    } finally {
      setSaving(false);
    }
  };

  const mealPrimaryCount =
    plan?.planMealResponseDtos?.filter((m) => !!m.primary).length ?? 0;

  if (!userId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Edit Plan</h1>
        <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Missing userId. Please login again.
        </div>
      </div>
    );
  }

  if (!planId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Edit Plan</h1>
        <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Missing planId in route.
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page-shell">
        <PageLoading text="Loading plan..." />
      </div>
    );
  }

  return (
    <div className="page-shell">
      <form onSubmit={handleSubmit(onSaveRoot)}>
        <div className="provider-form-header">
          <div>
            <h1 className="page-title">Edit Plan</h1>
            <p className="page-subtitle">
              Root fields update here. Meals are edited via modal (separate
              endpoints).
            </p>
            <div className="mt-1 text-xs text-slate-500">
              Plan ID: <span className="font-mono">{planId}</span>
            </div>
          </div>

          <div className="flex gap-2">
            <button
              type="button"
              className="provider-btn"
              onClick={() => navigate(-1)}
            >
              <FiArrowLeft className="inline" /> Back
            </button>

            <Link
              to={`/provider/insights/${planId}`}
              className="provider-btn"
              style={{
                textDecoration: "none",
                display: "inline-flex",
                alignItems: "center",
                gap: 8,
              }}
            >
              <FiEdit3 className="inline" /> View Insights
            </Link>

            <LoadingButton
              type="submit"
              loading={saving}
              disabled={!canSave}
              loadingText="Saving..."
              className="provider-btn provider-btn-primary"
            >
              <FiSave className="inline" /> Save
            </LoadingButton>
          </div>
        </div>

        {/* Meals quick panel */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiPackage className="provider-icon" /> Meals
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="text-sm text-slate-600">
              Total meals:{" "}
              <b className="text-slate-900">
                {plan?.planMealResponseDtos?.length ?? 0}
              </b>{" "}
              • Primary:{" "}
              <b
                className={
                  mealPrimaryCount > 0 ? "text-emerald-600" : "text-rose-600"
                }
              >
                {mealPrimaryCount}
              </b>
              {mealPrimaryCount === 0 ? (
                <span className="ml-2 text-rose-600 font-semibold">
                  (INVALID: at least one primary required)
                </span>
              ) : null}
            </div>

            <button
              type="button"
              className="provider-btn provider-btn-add"
              onClick={() => setMealModalOpen(true)}
            >
              <FiEdit3 className="inline" /> Edit Meals
            </button>
          </div>

          <div className="mt-3 grid grid-cols-1 gap-3 md:grid-cols-2">
            {(plan?.planMealResponseDtos ?? []).slice(0, 4).map((m) => (
              <div
                key={String(m.planMealId)}
                className="rounded-2xl border border-slate-200 bg-white p-3"
              >
                <div className="flex items-center justify-between gap-2">
                  <div className="font-semibold text-slate-800">{m.name}</div>
                  <div
                    className={[
                      "rounded-full px-2 py-0.5 text-xs font-semibold",
                      m.primary
                        ? "bg-indigo-50 text-indigo-700"
                        : "bg-slate-50 text-slate-600",
                    ].join(" ")}
                  >
                    {m.primary ? "Primary" : "Optional"}
                  </div>
                </div>
                <div className="mt-1 text-xs text-slate-500 line-clamp-2">
                  {m.description}
                </div>
                <div className="mt-2 text-xs text-slate-600">
                  Price/mo: <b className="text-slate-900">{m.pricePerMonth}</b>{" "}
                  • Current:{" "}
                  <b className="text-slate-900">{m.currentSubCount ?? 0}</b> /
                  Min: <b className="text-slate-900">{m.minSubCount ?? 0}</b>
                </div>
              </div>
            ))}
          </div>

          {(plan?.planMealResponseDtos?.length ?? 0) > 4 ? (
            <div className="mt-2 text-xs text-slate-500">
              Showing first 4 meals. Open “Edit Meals” to manage all.
            </div>
          ) : null}
        </div>

        {/* Plan image */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiImage className="provider-icon" /> Plan Image
          </div>

          <div className="provider-image-row">
            <input
              type="hidden"
              {...register("imageKey")}
              value={watch("imageKey") ?? ""}
            />

            <div className="provider-image-box">
              {uploadingPlanImage ? (
                <div className="provider-loading-container">
                  <PageLoading text="Uploading image..." />
                </div>
              ) : planImagePreview ? (
                <img
                  src={planImagePreview}
                  className="provider-image-preview"
                  alt="Plan preview"
                  onError={() => setPlanImagePreview(null)}
                />
              ) : (
                <div className="provider-image-placeholder">
                  <FiImage size={48} className="provider-placeholder-icon" />
                  <div>No image</div>
                </div>
              )}
            </div>

            <div className="provider-image-actions">
              <input
                ref={planFileRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={onSelectPlanImage}
              />

              <button
                type="button"
                className="provider-btn provider-btn-upload"
                onClick={() => planFileRef.current?.click()}
                disabled={uploadingPlanImage}
              >
                <FiUpload className="inline" /> Upload Image
              </button>

              <div className="text-xs text-slate-500">
                Key saved into <span className="font-mono">image</span> (save to
                persist)
              </div>

              <div className="text-xs text-slate-400 font-mono break-all">
                key: {imageKey || "—"}
              </div>

              {errors.imageKey?.message ? (
                <div className="provider-err">{errors.imageKey.message}</div>
              ) : null}
            </div>
          </div>
        </div>

        {/* Root fields */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiFileText className="provider-icon" /> Plan Info
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <label className="provider-label">Title</label>
              <input className="provider-input" {...register("title")} />
              {errors.title?.message ? (
                <div className="provider-err">{errors.title.message}</div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">
                <FiDollarSign className="inline text-indigo-500" /> Display
                Subscription Fee
              </label>
              <input
                className="provider-input"
                type="number"
                step="1"
                {...register("displaySubscriptionFee")}
              />
              {errors.displaySubscriptionFee?.message ? (
                <div className="provider-err">
                  {errors.displaySubscriptionFee.message}
                </div>
              ) : null}
            </div>

            <div className="md:col-span-2">
              <label className="provider-label">Description</label>
              <input className="provider-input" {...register("description")} />
              {errors.description?.message ? (
                <div className="provider-err">{errors.description.message}</div>
              ) : null}
            </div>

            <div className="md:col-span-2">
              <label className="provider-label">
                <FiTag className="inline text-purple-500" /> Categories
              </label>

              {loadingCats ? (
                <div className="text-sm text-slate-500 flex items-center gap-2">
                  <span className="provider-spinner" /> Loading categories...
                </div>
              ) : (
                <div className="provider-chip-grid">
                  {categories.map((c) => (
                    <label key={String(c.id)} className="provider-chip">
                      <input
                        type="checkbox"
                        value={String(c.id)}
                        {...register("categoryIds")}
                      />
                      <span>{c.name}</span>
                    </label>
                  ))}
                </div>
              )}

              {errors.categoryIds?.message ? (
                <div className="provider-err">{errors.categoryIds.message}</div>
              ) : null}
            </div>

            <div className="md:col-span-2">
              <label className="provider-label">
                <FiCalendar className="inline text-cyan-500" /> Skip Days (max
                2)
              </label>

              <div className="flex gap-2 flex-wrap items-center">
                <input
                  className="provider-input w-[200px]"
                  type="date"
                  onChange={(e) => {
                    const v = e.target.value;
                    if (!v) return;
                    const cur = watch("skipDays") ?? [];
                    if (cur.includes(v)) return;
                    if (cur.length >= 2) return;

                    setValue("skipDays", [...cur, v], {
                      shouldValidate: true,
                      shouldDirty: true,
                    });
                    e.currentTarget.value = "";
                  }}
                />
                <div className="text-xs text-slate-500">
                  Selected: {skipDays.length}/2
                </div>
              </div>

              {skipDays.length > 0 ? (
                <div className="mt-2 flex gap-2 flex-wrap">
                  {skipDays.map((d) => (
                    <button
                      key={d}
                      type="button"
                      className="provider-pill"
                      onClick={() =>
                        setValue(
                          "skipDays",
                          (watch("skipDays") ?? []).filter((x) => x !== d),
                          { shouldValidate: true, shouldDirty: true },
                        )
                      }
                    >
                      {d} ✕
                    </button>
                  ))}
                </div>
              ) : null}

              {errors.skipDays?.message ? (
                <div className="provider-err">{errors.skipDays.message}</div>
              ) : null}
            </div>
          </div>
        </div>

        {/* Address */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiMapPin className="provider-icon" /> Address
          </div>

          <div className="flex items-center justify-between gap-2 mb-3">
            <div className="text-xs text-slate-500">
              Location is required (lat/lng).
            </div>
            <button
              type="button"
              className="provider-btn"
              onClick={setGeoFromBrowser}
            >
              Use browser geolocation
            </button>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <label className="provider-label">Postal code</label>
              <input
                className="provider-input"
                placeholder="123-4567"
                {...register("address.postalCode")}
              />
              {errors.address?.postalCode?.message ? (
                <div className="provider-err">
                  {errors.address.postalCode.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">Prefecture</label>
              <input
                className="provider-input"
                {...register("address.prefecture")}
              />
              {errors.address?.prefecture?.message ? (
                <div className="provider-err">
                  {errors.address.prefecture.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">City</label>
              <input className="provider-input" {...register("address.city")} />
              {errors.address?.city?.message ? (
                <div className="provider-err">
                  {errors.address.city.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">District</label>
              <input
                className="provider-input"
                {...register("address.district")}
              />
              {errors.address?.district?.message ? (
                <div className="provider-err">
                  {errors.address.district.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">Chome/Ban/Go</label>
              <input
                className="provider-input"
                {...register("address.chomeBanGo")}
              />
              {errors.address?.chomeBanGo?.message ? (
                <div className="provider-err">
                  {errors.address.chomeBanGo.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">Building/Room</label>
              <input
                className="provider-input"
                {...register("address.buildingNameRoomNo")}
              />
            </div>

            <div>
              <label className="provider-label">Latitude</label>
              <input
                className="provider-input"
                type="number"
                step="0.000001"
                {...register("address.location.latitude")}
              />
              {errors.address?.location?.latitude?.message ? (
                <div className="provider-err">
                  {errors.address.location.latitude.message}
                </div>
              ) : null}
            </div>

            <div>
              <label className="provider-label">Longitude</label>
              <input
                className="provider-input"
                type="number"
                step="0.000001"
                {...register("address.location.longitude")}
              />
              {errors.address?.location?.longitude?.message ? (
                <div className="provider-err">
                  {errors.address.location.longitude.message}
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </form>

      {/* Meals Modal */}
      <ProviderMealEditModal
        open={mealModalOpen}
        onClose={() => setMealModalOpen(false)}
        planId={planId}
        plan={plan}
        onUpdatedPlan={(p) => setPlan(p)}
        onToast={(msg, ok) => showAlert(msg, ok)}
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

export default ProviderPlanEditPage;
