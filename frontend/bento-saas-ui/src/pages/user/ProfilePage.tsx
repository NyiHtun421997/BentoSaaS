import { useContext, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "../../state/createContext";
import { apiGet, apiPut, isApiError } from "../../lib/api/http";
import PageLoading from "../../components/PageLoading";
import AlertModal from "../../components/AlertModal";
import type { UserResponseDTO } from "../../lib/api/types";
import "./ProfilePage.css";
import z from "zod";
import { zodResolver } from "@hookform/resolvers/zod/dist/zod.js";
import { useForm } from "react-hook-form";
import { PhoneNumberUtil } from "google-libphonenumber";
import {
  FiUser,
  FiPhone,
  FiImage,
  FiMapPin,
  FiMap,
  FiHome,
  FiNavigation,
  FiCreditCard,
  FiBriefcase,
  FiSave,
  FiArrowLeft,
  FiMail,
} from "react-icons/fi";

const phoneUtil = PhoneNumberUtil.getInstance();

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
});

const UserFormSchema = z.object({
  firstName: z
    .string()
    .min(1, "First name is required")
    .refine((val) => val.trim() !== "", "First name cannot be empty"),
  lastName: z
    .string()
    .min(1, "Last name is required")
    .refine((val) => val.trim() !== "", "Last name cannot be empty"),
  phNo: z
    .string()
    .min(1, "Phone number is required")
    .refine((val) => val.trim() !== "", "Phone number cannot be empty")
    .refine((val) => {
      try {
        const parsed = phoneUtil.parse(val, "JP");
        return phoneUtil.isValidNumber(parsed);
      } catch {
        return false;
      }
    }, "Invalid phone number"),
  imageUrl: z.url("Please enter a valid URL"),
  address: AddressSchema,
});

type UserForm = z.infer<typeof UserFormSchema>;

const toFormDefaults = (u: UserResponseDTO): UserForm => ({
  firstName: u.firstName ?? "",
  lastName: u.lastName ?? "",
  phNo: u.phNo ?? "",
  imageUrl: u.imageUrl ?? "",
  address: {
    postalCode: u.address?.postalCode ?? "",
    prefecture: u.address?.prefecture ?? "",
    city: u.address?.city ?? "",
    district: u.address?.district ?? "",
    chomeBanGo: u.address?.chomeBanGo ?? "",
    buildingNameRoomNo: u.address?.buildingNameRoomNo ?? "",
  },
});

const fieldError = (msg?: string) =>
  msg ? <div className="mt-1 text-xs text-rose-600">{msg}</div> : null;

const ProfileEditPage = () => {
  const navigate = useNavigate();
  const authContext = useContext(AuthContext);
  const userId = authContext?.authenticatedUser?.userId;

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [user, setUser] = useState<UserResponseDTO | null>(null);

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMsg, setAlertMsg] = useState<string | null>(null);
  const [alertSuccess, setAlertSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isValid },
  } = useForm<UserForm>({
    resolver: zodResolver(UserFormSchema),
    mode: "onChange",
    defaultValues: {
      firstName: "",
      lastName: "",
      phNo: "",
      imageUrl: "",
      address: {
        postalCode: "",
        prefecture: "",
        city: "",
        district: "",
        chomeBanGo: "",
        buildingNameRoomNo: "",
      },
    },
  });

  const canSave = useMemo(() => {
    if (!userId) return false;
    if (saving) return false;
    return isValid;
  }, [userId, saving, isValid]);

  useEffect(() => {
    if (!userId) return;

    const load = async () => {
      setLoading(true);
      setAlertMsg(null);

      try {
        const data = await apiGet<UserResponseDTO>(`/user/api/v1/${userId}`);
        setUser(data);
        reset(toFormDefaults(data)); // ✅ populate RHF form
      } catch (e: unknown) {
        setAlertMsg(isApiError(e) ? e.message : "Failed to load profile");
        setAlertSuccess(false);
        setAlertOpen(true);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [userId, reset]);

  const onSubmit = async (values: UserForm) => {
    if (!userId) {
      setAlertMsg("Missing userId. Please login again.");
      setAlertSuccess(false);
      setAlertOpen(true);
      return;
    }

    setSaving(true);
    setAlertMsg(null);

    try {
      const payload = {
        firstName: values.firstName,
        lastName: values.lastName,
        phNo: values.phNo,
        imageUrl: values.imageUrl || undefined,
        address: values.address,
      };

      const updated = await apiPut<UserResponseDTO>(
        `/user/api/v1/${userId}`,
        payload,
      );
      setUser(updated);
      reset(toFormDefaults(updated));

      setAlertMsg("Profile updated.");
      setAlertSuccess(true);
      setAlertOpen(true);
    } catch (e: unknown) {
      setAlertMsg(isApiError(e) ? e.message : "Failed to update profile");
      setAlertSuccess(false);
      setAlertOpen(true);
    } finally {
      setSaving(false);
    }
  };

  if (!userId) {
    return (
      <div className="page-shell">
        <h1 className="page-title">Profile</h1>
        <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
          Please login again (missing userId in auth state).
        </p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page-shell">
        <PageLoading text="Loading profile…" />
      </div>
    );
  }

  return (
    <div className="page-shell">
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="profile-edit-header">
          <div>
            <h1 className="page-title">✨ Edit Profile</h1>
            <p className="page-subtitle">Update your basic info and address.</p>
          </div>

          <div className="flex gap-2 mt-4">
            <button
              type="button"
              className="profile-btn-secondary"
              onClick={() => navigate(-1)}
            >
              <FiArrowLeft className="w-4 h-4" />
              Back
            </button>

            <button
              type="submit"
              disabled={!canSave}
              className="profile-btn-primary"
            >
              <FiSave className="w-4 h-4" />
              {saving ? "Saving…" : "Save Changes"}
            </button>
          </div>
        </div>

        <div className="profile-card">
          <div className="profile-grid">
            {/* Basic */}
            <div className="profile-section">
              <div className="profile-section-header">
                <FiUser className="section-icon" />
                <div className="profile-section-title">Personal Info</div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="profile-label">
                    <FiUser className="label-icon" />
                    First name
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="Taro"
                      {...register("firstName")}
                    />
                  </div>
                  {fieldError(errors.firstName?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiUser className="label-icon" />
                    Last name
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="Yamada"
                      {...register("lastName")}
                    />
                  </div>
                  {fieldError(errors.lastName?.message)}
                </div>

                <div className="sm:col-span-2">
                  <label className="profile-label">
                    <FiPhone className="label-icon" />
                    Phone number
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="090-xxxx-xxxx"
                      {...register("phNo")}
                    />
                  </div>
                  {fieldError(errors.phNo?.message)}
                </div>

                <div className="sm:col-span-2">
                  <label className="profile-label">
                    <FiImage className="label-icon" />
                    Profile Image URL
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="https://..."
                      {...register("imageUrl")}
                    />
                  </div>
                  {fieldError(errors.imageUrl?.message as string | undefined)}
                </div>
              </div>

              <div className="profile-email-badge">
                <FiMail className="w-3.5 h-3.5" />
                <span className="font-mono">{user?.email ?? "—"}</span>
              </div>
            </div>

            {/* Address */}
            <div className="profile-section">
              <div className="profile-section-header">
                <FiMapPin className="section-icon" />
                <div className="profile-section-title">Address Details</div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="profile-label">
                    <FiCreditCard className="label-icon" />
                    Postal code
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="123-4567"
                      {...register("address.postalCode")}
                    />
                  </div>
                  {fieldError(errors.address?.postalCode?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiMap className="label-icon" />
                    Prefecture
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="Osaka"
                      {...register("address.prefecture")}
                    />
                  </div>
                  {fieldError(errors.address?.prefecture?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiNavigation className="label-icon" />
                    City
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="Osaka-shi"
                      {...register("address.city")}
                    />
                  </div>
                  {fieldError(errors.address?.city?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiMapPin className="label-icon" />
                    District
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="Namba"
                      {...register("address.district")}
                    />
                  </div>
                  {fieldError(errors.address?.district?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiHome className="label-icon" />
                    Chome / Ban / Go
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="1-2-3"
                      {...register("address.chomeBanGo")}
                    />
                  </div>
                  {fieldError(errors.address?.chomeBanGo?.message)}
                </div>

                <div>
                  <label className="profile-label">
                    <FiBriefcase className="label-icon" />
                    Building / Room
                  </label>
                  <div className="input-wrapper">
                    <input
                      className="profile-input"
                      placeholder="ABC Mansion 101"
                      {...register("address.buildingNameRoomNo")}
                    />
                  </div>
                  {fieldError(errors.address?.buildingNameRoomNo?.message)}
                </div>
              </div>
            </div>
          </div>
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

export default ProfileEditPage;
