import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import z from "zod";
import {
  FiImage,
  FiFileText,
  FiMapPin,
  FiPackage,
  FiCalendar,
  FiTag,
  FiUpload,
  FiTrash2,
  FiPlus,
  FiArrowLeft,
  FiDollarSign,
  FiSave,
} from "react-icons/fi";

import AuthContext from "../../state/createContext";
import { apiGet, apiPost, isApiError } from "../../lib/api/http";
import AlertModal from "../../components/AlertModal";
import PageLoading from "../../components/PageLoading";

import type { CategoryDto, PlanResponseDto } from "../../lib/api/types";
import "./ProviderPlanCreatePage.css";
import LoadingButton from "../../components/LoadingButton";

// -------------------- Schema --------------------
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

const MealSchema = z.object({
  name: z.string().min(1, "Meal name is required").max(20),
  description: z.string().max(50),
  pricePerMonth: z.number().min(0, "Price must be >= 0"),
  primary: z.boolean(),
  minSubCount: z.number().min(0).optional(),
  imageKey: z.string().optional(), // image key
});

const PlanCreateSchema = z
  .object({
    title: z.string().min(1, "Title is required").max(20),
    description: z.string().min(1, "Description is required").max(50),
    categoryIds: z.array(z.string()).min(1, "Select at least 1 category"),
    displaySubscriptionFee: z.number().min(0, "Fee must be >= 0"),
    skipDays: z.array(z.string()).max(2, "Skip days must be at most 2"),
    imageKey: z.string().optional(), // plan image key
    address: AddressSchema,
    planMealRequestDtos: z
      .array(MealSchema)
      .min(1, "At least 1 meal is required"),
  })
  .refine((v) => v.planMealRequestDtos.some((m) => m.primary), {
    message: "At least one primary meal is required",
    path: ["planMealRequestDtos"],
  });

type PlanCreateForm = z.infer<typeof PlanCreateSchema>;

// -------------------- Helpers --------------------
const uniqueFilename = (original: string) => {
  const ts = Date.now();
  const rand = Math.floor(Math.random() * 10000);
  const ext = original.split(".").pop();
  const base = original.replace(/\.[^/.]+$/, "");
  return `${base}_${ts}_${rand}.${ext}`;
};

const fieldErr = (msg?: string) =>
  msg ? <div className="provider-err">{msg}</div> : null;

// -------------------- Component --------------------
const ProviderPlanCreatePage = () => {
  const navigate = useNavigate();
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId;

  // alerts
  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMsg, setAlertMsg] = useState<string | null>(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  // categories
  const [loadingCats, setLoadingCats] = useState(false);
  const [categories, setCategories] = useState<CategoryDto[]>([]);

  // plan image upload state
  const [uploadingPlanImage, setUploadingPlanImage] = useState(false);
  const [planImagePreview, setPlanImagePreview] = useState<string | null>(null);
  const planFileRef = useRef<HTMLInputElement>(null);

  // meal image upload states (per meal field id)
  const [mealUploading, setMealUploading] = useState<Record<string, boolean>>(
    {},
  );
  const [mealPreview, setMealPreview] = useState<Record<string, string>>({});
  const [activeMealIndex, setActiveMealIndex] = useState<number | null>(null);
  const mealFileRef = useRef<HTMLInputElement>(null);

  // track object urls to revoke
  const objectUrlsRef = useRef<string[]>([]);

  const {
    register,
    control,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors, isSubmitting, isValid },
  } = useForm<PlanCreateForm>({
    resolver: zodResolver(PlanCreateSchema),
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
      planMealRequestDtos: [
        {
          name: "",
          description: "",
          pricePerMonth: 0,
          primary: true,
          minSubCount: 0,
          imageKey: "",
        },
      ],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "planMealRequestDtos",
  });

  const skipDays = watch("skipDays") ?? [];
  const mealList = watch("planMealRequestDtos") ?? [];

  const canCreate = useMemo(() => {
    if (!userId) return false;
    if (isSubmitting) return false;
    return isValid;
  }, [userId, isSubmitting, isValid]);

  // load categories once
  useEffect(() => {
    let mounted = true;

    const loadCategories = async () => {
      setLoadingCats(true);
      try {
        const data = await apiGet<CategoryDto[]>(
          `/plan-management/api/v1/category`,
        );
        if (mounted) setCategories(data ?? []);
      } catch (e: unknown) {
        if (!mounted) return;
        setAlertMsg(isApiError(e) ? e.message : "Failed to load categories");
        setAlertSuccess(false);
        setAlertOpen(true);
      } finally {
        if (mounted) setLoadingCats(false);
      }
    };

    void loadCategories();
    return () => {
      mounted = false;
    };
  }, []);

  // cleanup object urls
  useEffect(() => {
    return () => {
      objectUrlsRef.current.forEach((u) => URL.revokeObjectURL(u));
      objectUrlsRef.current = [];
    };
  }, []);

  // helper to show alert
  const showAlert = (msg: string, ok: boolean) => {
    setAlertMsg(msg);
    setAlertSuccess(ok);
    setAlertOpen(true);
  };

  // -------------------- Geolocation --------------------
  const setGeoFromBrowser = () => {
    if (!navigator.geolocation) {
      showAlert("Geolocation is not supported by this browser.", false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = Math.round(pos.coords.latitude * 100000) / 100000;
        const lng = Math.round(pos.coords.longitude * 100000) / 100000;
        setValue("address.location.latitude", lat, {
          shouldValidate: true,
        });
        setValue("address.location.longitude", lng, {
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

  // -------------------- Plan image upload --------------------
  const uploadPlanImage = async (file: File) => {
    if (!file.type.startsWith("image/"))
      throw new Error("Please select an image file");
    if (file.size > 5 * 1024 * 1024)
      throw new Error("Image size must be less than 5MB");

    setUploadingPlanImage(true);
    try {
      const name = uniqueFilename(file.name);

      // presign
      const presign = await apiPost<{ url: string; file: string }>(
        `/plan-management/api/v1/provider/plan/file/plan?filename=${encodeURIComponent(name)}`,
      );

      // put to s3
      const res = await fetch(presign.url, {
        method: "PUT",
        body: file,
        headers: { "Content-Type": file.type },
      });

      if (!res.ok) throw new Error("Failed to upload plan image");

      const previewUrl = URL.createObjectURL(file);
      objectUrlsRef.current.push(previewUrl);

      setPlanImagePreview(previewUrl);
      setValue("imageKey", presign.file, {
        shouldValidate: true,
        shouldDirty: true,
      });

      showAlert("Plan image uploaded. (Create to persist)", true);
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

  // -------------------- Meal image upload --------------------
  const uploadMealImage = async (
    mealFieldId: string,
    idx: number,
    file: File,
  ) => {
    if (!file.type.startsWith("image/"))
      throw new Error("Please select an image file");
    if (file.size > 5 * 1024 * 1024)
      throw new Error("Image size must be less than 5MB");

    setMealUploading((p) => ({ ...p, [mealFieldId]: true }));
    try {
      const name = uniqueFilename(file.name);

      // presign (meal endpoint)
      const presign = await apiPost<{ url: string; file: string }>(
        `/plan-management/api/v1/provider/plan/file/meal?filename=${encodeURIComponent(name)}`,
      );

      // put to s3
      const res = await fetch(presign.url, {
        method: "PUT",
        body: file,
        headers: { "Content-Type": file.type },
      });

      if (!res.ok) throw new Error("Failed to upload meal image");

      const previewUrl = URL.createObjectURL(file);
      objectUrlsRef.current.push(previewUrl);

      setMealPreview((p) => ({ ...p, [mealFieldId]: previewUrl }));

      // store key in RHF form
      setValue(`planMealRequestDtos.${idx}.imageKey`, presign.file, {
        shouldValidate: true,
        shouldDirty: true,
      });

      showAlert("Meal image uploaded. (Create to persist)", true);
    } finally {
      setMealUploading((p) => ({ ...p, [mealFieldId]: false }));
    }
  };

  const onSelectMealImage = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    const idx = activeMealIndex;
    if (!file || idx === null) return;

    const mealFieldId = fields[idx]?.id;
    if (!mealFieldId) return;

    try {
      await uploadMealImage(mealFieldId, idx, file);
    } catch (err: unknown) {
      showAlert(err instanceof Error ? err.message : "Upload failed", false);
    } finally {
      e.target.value = "";
      setActiveMealIndex(null);
    }
  };

  // -------------------- Submit --------------------
  const onSubmit = async (values: PlanCreateForm) => {
    if (!userId) {
      showAlert("Missing userId. Please login again.", false);
      return;
    }

    // extra guard (UI should already show error)
    if (!values.planMealRequestDtos.some((m) => m.primary)) {
      showAlert("At least one primary meal is required.", false);
      return;
    }

    try {
      const created = await apiPost<PlanResponseDto>(
        `/plan-management/api/v1/provider/plan`,
        values,
        { "X-USER-ID": String(userId) },
      );

      showAlert("Plan created.", true);

      reset(undefined);

      navigate(`/provider/plans/${created.planId}/edit`);
    } catch (e: unknown) {
      showAlert(isApiError(e) ? e.message : "Failed to create plan", false);
    }
  };

  if (!userId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Create Plan</h1>
        <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Missing userId. Please login again.
        </div>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <form onSubmit={handleSubmit(onSubmit)}>
        <input type="hidden" {...register("imageKey")} />

        {/* ONE hidden input for meal uploads */}
        <input
          ref={mealFileRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={onSelectMealImage}
        />

        <div className="provider-form-header">
          <div>
            <h1 className="page-title">Create Plan</h1>
            <p className="page-subtitle">
              Create plan + meals. Must include at least one primary meal.
            </p>
          </div>

          <div className="flex gap-2">
            <button
              type="button"
              className="provider-btn"
              onClick={() => navigate("/provider/plans")}
            >
              <FiArrowLeft className="inline" /> Back
            </button>

            <LoadingButton
              type="submit"
              loading={isSubmitting}
              disabled={!canCreate}
              loadingText="Creating..."
              className="provider-btn provider-btn-primary"
            >
              <FiSave className="inline" /> Create
            </LoadingButton>
          </div>
        </div>

        {/* Plan image */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiImage className="provider-icon" /> Plan Image
          </div>

          <div className="provider-image-row">
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
                Max 5MB. Key saved into <span className="font-mono">image</span>
              </div>

              <div className="text-xs text-slate-400 font-mono break-all">
                key: {watch("imageKey") || "—"}
              </div>

              {fieldErr(errors.imageKey?.message)}
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
              {fieldErr(errors.title?.message)}
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
                {...register("displaySubscriptionFee", { valueAsNumber: true })}
              />
              {fieldErr(errors.displaySubscriptionFee?.message)}
            </div>

            <div className="md:col-span-2">
              <label className="provider-label">Description</label>
              <input className="provider-input" {...register("description")} />
              {fieldErr(errors.description?.message)}
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

              {fieldErr(errors.categoryIds?.message)}
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

              {fieldErr(errors.skipDays?.message)}
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
              {fieldErr(errors.address?.postalCode?.message)}
            </div>

            <div>
              <label className="provider-label">Prefecture</label>
              <input
                className="provider-input"
                {...register("address.prefecture")}
              />
              {fieldErr(errors.address?.prefecture?.message)}
            </div>

            <div>
              <label className="provider-label">City</label>
              <input className="provider-input" {...register("address.city")} />
              {fieldErr(errors.address?.city?.message)}
            </div>

            <div>
              <label className="provider-label">District</label>
              <input
                className="provider-input"
                {...register("address.district")}
              />
              {fieldErr(errors.address?.district?.message)}
            </div>

            <div>
              <label className="provider-label">Chome/Ban/Go</label>
              <input
                className="provider-input"
                {...register("address.chomeBanGo")}
              />
              {fieldErr(errors.address?.chomeBanGo?.message)}
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
                {...register("address.location.latitude", {
                  valueAsNumber: true,
                })}
              />
              {fieldErr(errors.address?.location?.latitude?.message)}
            </div>

            <div>
              <label className="provider-label">Longitude</label>
              <input
                className="provider-input"
                type="number"
                step="0.000001"
                {...register("address.location.longitude", {
                  valueAsNumber: true,
                })}
              />
              {fieldErr(errors.address?.location?.longitude?.message)}
            </div>
          </div>
        </div>

        {/* Meals */}
        <div className="provider-panel">
          <div className="provider-panel-title">
            <FiPackage className="provider-icon" /> Meals (required)
          </div>

          {errors.planMealRequestDtos?.message ? (
            <div className="provider-err">
              {String(errors.planMealRequestDtos.message)}
            </div>
          ) : null}

          <div className="space-y-3">
            {fields.map((f, idx) => (
              <div key={f.id} className="provider-meal-card">
                <div className="provider-meal-top">
                  <div className="font-semibold text-slate-800">
                    Meal #{idx + 1}
                  </div>
                  <input
                    type="hidden"
                    {...register(`planMealRequestDtos.${idx}.imageKey`)}
                  />

                  <div className="flex gap-2 items-center">
                    <label className="provider-toggle">
                      <input
                        type="checkbox"
                        {...register(
                          `planMealRequestDtos.${idx}.primary` as const,
                        )}
                      />
                      <span>Primary</span>
                    </label>

                    <button
                      type="button"
                      className="provider-btn provider-btn-danger"
                      onClick={() => remove(idx)}
                      disabled={fields.length <= 1}
                      title={
                        fields.length <= 1
                          ? "At least one meal is required"
                          : "Remove meal"
                      }
                    >
                      <FiTrash2 className="inline" /> Remove
                    </button>
                  </div>
                </div>

                {/* Meal image upload (FIXED) */}
                <div className="provider-image-row mt-2">
                  <div className="provider-image-box">
                    {mealUploading[f.id] ? (
                      <div className="provider-loading-container">
                        <PageLoading text="Uploading image..." />
                      </div>
                    ) : mealPreview[f.id] ? (
                      <img
                        src={mealPreview[f.id]}
                        className="provider-image-preview"
                        alt={`Meal ${idx + 1} preview`}
                      />
                    ) : (
                      <div className="provider-image-placeholder">
                        <FiImage
                          size={48}
                          className="provider-placeholder-icon"
                        />
                        <div>No image</div>
                      </div>
                    )}
                  </div>

                  <div className="provider-image-actions">
                    <button
                      type="button"
                      className="provider-btn provider-btn-upload"
                      onClick={() => {
                        setActiveMealIndex(idx);
                        mealFileRef.current?.click();
                      }}
                      disabled={!!mealUploading[f.id]}
                    >
                      <FiUpload className="inline" /> Upload Meal Image
                    </button>

                    <div className="text-xs text-slate-500">
                      Max 5MB. Key saved into{" "}
                      <span className="font-mono">
                        planMealRequestDtos[{idx}].image
                      </span>
                    </div>

                    <div className="text-xs text-slate-400 font-mono break-all">
                      key: {watch(`planMealRequestDtos.${idx}.imageKey`) || "—"}
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-1 gap-3 md:grid-cols-2 mt-3">
                  <div>
                    <label className="provider-label">Name</label>
                    <input
                      className="provider-input"
                      {...register(`planMealRequestDtos.${idx}.name` as const)}
                    />
                    {fieldErr(errors.planMealRequestDtos?.[idx]?.name?.message)}
                  </div>

                  <div>
                    <label className="provider-label">Price per month</label>
                    <input
                      className="provider-input"
                      type="number"
                      step="1"
                      {...register(
                        `planMealRequestDtos.${idx}.pricePerMonth` as const,
                        { valueAsNumber: true },
                      )}
                    />
                    {fieldErr(
                      errors.planMealRequestDtos?.[idx]?.pricePerMonth?.message,
                    )}
                  </div>

                  <div className="md:col-span-2">
                    <label className="provider-label">Description</label>
                    <input
                      className="provider-input"
                      {...register(
                        `planMealRequestDtos.${idx}.description` as const,
                      )}
                    />
                    {fieldErr(
                      errors.planMealRequestDtos?.[idx]?.description?.message,
                    )}
                  </div>

                  <div>
                    <label className="provider-label">Min Sub Count</label>
                    <input
                      className="provider-input"
                      type="number"
                      step="1"
                      {...register(
                        `planMealRequestDtos.${idx}.minSubCount` as const,
                        { valueAsNumber: true },
                      )}
                    />
                    {fieldErr(
                      errors.planMealRequestDtos?.[idx]?.minSubCount?.message,
                    )}
                  </div>

                  <div className="text-xs text-slate-500 flex items-center mt-2">
                    Primary meals:{" "}
                    <b className="ml-1">
                      {mealList.filter((m) => m.primary).length}
                    </b>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-3 flex gap-2">
            <button
              type="button"
              className="provider-btn provider-btn-add"
              onClick={() =>
                append({
                  name: "",
                  description: "",
                  pricePerMonth: 0,
                  primary: false,
                  minSubCount: 0,
                  imageKey: "",
                })
              }
            >
              <FiPlus className="inline" /> Add meal
            </button>
          </div>

          {mealList.length > 0 && mealList.every((m) => !m.primary) ? (
            <div className="mt-2 text-sm text-rose-600">
              At least one meal must be Primary.
            </div>
          ) : null}
        </div>
      </form>

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

export default ProviderPlanCreatePage;
