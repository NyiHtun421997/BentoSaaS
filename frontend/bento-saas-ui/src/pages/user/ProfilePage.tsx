import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthContext from "../../state/createContext";
import { apiGet, apiPost, apiPut, isApiError } from "../../lib/api/http";
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
  FiMapPin,
  FiMap,
  FiHome,
  FiNavigation,
  FiCreditCard,
  FiBriefcase,
  FiSave,
  FiArrowLeft,
  FiMail,
  FiUpload,
  FiCamera,
} from "react-icons/fi";
import { TbFileDescription } from "react-icons/tb";
import { MdDriveFileRenameOutline } from "react-icons/md";

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
  description: z
    .string()
    .max(500, "Description must be 500 characters or less")
    .optional(),
  imageKey: z.string().optional(),
  address: AddressSchema,
});

type UserForm = z.infer<typeof UserFormSchema>;

const toFormDefaults = (u: UserResponseDTO): UserForm => ({
  firstName: u.firstName ?? "",
  lastName: u.lastName ?? "",
  phNo: u.phNo ?? "",
  description: u.description ?? "",
  imageKey: "",
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

  const [uploading, setUploading] = useState(false);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [uploadedImageKey, setUploadedImageKey] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isValid },
  } = useForm<UserForm>({
    resolver: zodResolver(UserFormSchema),
    mode: "onChange",
    defaultValues: {
      firstName: "",
      lastName: "",
      phNo: "",
      description: "",
      imageKey: "",
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
        setImagePreview(data.image || null);
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

  const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith("image/")) {
      setAlertMsg("Please select a valid image file");
      setAlertSuccess(false);
      setAlertOpen(true);
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setAlertMsg("Image size must be less than 5MB");
      setAlertSuccess(false);
      setAlertOpen(true);
      return;
    }

    setUploading(true);
    setAlertMsg(null);

    try {
      // Generate unique filename with timestamp and random number
      const timestamp = Date.now();
      const randomNum = Math.floor(Math.random() * 10000);
      const fileExtension = file.name.split(".").pop();
      const baseFilename = file.name.replace(/\.[^/.]+$/, "");
      const uniqueFilename = `${baseFilename}_${timestamp}_${randomNum}.${fileExtension}`;

      // Generate pre-signed URL
      const urlResponse = await apiPost<{ url: string; file: string }>(
        `/user/api/v1/user/file?filename=${encodeURIComponent(uniqueFilename)}`,
      );

      // Upload directly to S3
      const uploadResponse = await fetch(urlResponse.url, {
        method: "PUT",
        body: file,
        headers: {
          "Content-Type": file.type,
        },
      });

      if (!uploadResponse.ok) {
        throw new Error("Failed to upload image");
      }

      // Set preview and store key
      const previewUrl = URL.createObjectURL(file);
      setImagePreview(previewUrl);
      setUploadedImageKey(urlResponse.file);

      setAlertMsg("Image uploaded successfully! Don't forget to save.");
      setAlertSuccess(true);
      setAlertOpen(true);
    } catch (e: unknown) {
      setAlertMsg(isApiError(e) ? e.message : "Failed to upload image");
      setAlertSuccess(false);
      setAlertOpen(true);
    } finally {
      setUploading(false);
    }
  };

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
      // Format phone number to E.164 format
      let formattedPhNo = values.phNo;
      try {
        const parsedPhone = phoneUtil.parse(values.phNo, "JP");
        formattedPhNo = phoneUtil.format(parsedPhone, 0); // 0 = E164 format
      } catch {
        // If parsing fails, use original (validation already passed)
        formattedPhNo = values.phNo;
      }

      const payload = {
        firstName: values.firstName,
        lastName: values.lastName,
        phNo: formattedPhNo,
        description: values.description || undefined,
        imageKey: uploadedImageKey || undefined,
        address: values.address,
      };

      const updated = await apiPut<UserResponseDTO>(
        `/user/api/v1/${userId}`,
        payload,
      );
      setUser(updated);
      setImagePreview(updated.image || null);
      setUploadedImageKey(null);
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
          {/* Profile Picture Section */}
          <div className="profile-image-section">
            <div className="profile-image-container">
              <div className="profile-image-wrapper">
                {uploading ? (
                  <div className="profile-image-loading">
                    <div className="spinner" />
                    <span className="loading-text">Uploading...</span>
                  </div>
                ) : imagePreview ? (
                  <img
                    src={imagePreview}
                    alt="Profile"
                    className="profile-image"
                    onError={() => setImagePreview(null)}
                  />
                ) : (
                  <div className="profile-image-placeholder">
                    <FiUser className="placeholder-icon" />
                  </div>
                )}
                <button
                  type="button"
                  className="profile-image-edit-btn"
                  onClick={() => fileInputRef.current?.click()}
                  disabled={uploading}
                >
                  <FiCamera className="w-4 h-4" />
                </button>
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageSelect}
                className="hidden"
              />
            </div>
            <div className="profile-image-info">
              <h3 className="profile-image-title">Profile Picture</h3>
              <p className="profile-image-subtitle">
                Click the camera icon to upload a new photo
              </p>
              <button
                type="button"
                className="profile-upload-btn"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
              >
                <FiUpload className="w-4 h-4" />
                {uploading ? "Uploading..." : "Choose Image"}
              </button>
              <p className="profile-image-hint">JPG, PNG or GIF. Max 5MB</p>
            </div>
          </div>

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
                    <MdDriveFileRenameOutline className="label-icon" />
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
                    <MdDriveFileRenameOutline className="label-icon" />
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
                    <TbFileDescription className="label-icon" />
                    Description
                  </label>
                  <div className="input-wrapper">
                    <textarea
                      className="profile-textarea"
                      placeholder="Tell us about yourself..."
                      rows={4}
                      maxLength={500}
                      {...register("description")}
                    />
                  </div>
                  <div className="char-count">
                    {watch("description")?.length ?? 0} / 500
                  </div>
                  {fieldError(errors.description?.message)}
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
