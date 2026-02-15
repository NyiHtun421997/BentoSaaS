import "./SignupPage.css";
import { useState, useRef, useEffect } from "react";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import authService from "../../lib/services/authService";
import type { ApiError } from "../../lib/api/http";
import { FiEye, FiEyeOff } from "react-icons/fi";
import { Link } from "react-router-dom";
import LoadingButton from "../../components/LoadingButton";

const SignupFormSchema = z
  .object({
    email: z
      .email({ message: "Please enter a valid email address" })
      .min(1, { message: "Please enter an email address" }),
    password: z
      .string()
      .min(8, { message: "Password must be at least 8 characters long" }),
    confirmPassword: z
      .string()
      .min(1, { message: "Please confirm your password" }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

type SignupForm = z.infer<typeof SignupFormSchema>;

const SIGNUP_PATH = "/user/api/v1/signup";

const SignupPage = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupForm>({
    resolver: zodResolver(SignupFormSchema),
  });

  const regularBtnRef = useRef<HTMLButtonElement>(null);
  const providerBtnRef = useRef<HTMLButtonElement>(null);
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [isRegularUser, setIsRegularUser] = useState(true);

  const switchRegularProvider = (
    event: React.MouseEvent<HTMLButtonElement>,
  ) => {
    event.preventDefault();
    setIsRegularUser(event.currentTarget.classList.contains("regular"));
  };

  useEffect(() => {
    if (isRegularUser) {
      regularBtnRef.current?.classList.add("bg-white", "shadow-sm");
      providerBtnRef.current?.classList.remove("bg-white", "shadow-sm");
    } else {
      providerBtnRef.current?.classList.add("bg-white", "shadow-sm");
      regularBtnRef.current?.classList.remove("bg-white", "shadow-sm");
    }
  }, [isRegularUser]);

  const onSubmit = async (values: SignupForm) => {
    setError(null);
    setLoading(true);

    try {
      await authService.signup(SIGNUP_PATH, values, isRegularUser);

      // On success, navigate to login page
      setSuccess(true);
      setTimeout(() => {
        navigate("/login", { replace: true });
      }, 1000);
    } catch (err) {
      const e = err as Partial<ApiError>;
      setError(e.message || "Signup failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="signup-page">
      <div className="signup-card">
        <h1 className="signup-title">Create an account</h1>
        <p className="signup-subtitle">
          Choose a tab and fill in your details to get started.
        </p>
        <div className="flex gap-2 rounded-full bg-slate-100 p-1 text-sm font-semibold text-slate-600">
          <button
            onClick={switchRegularProvider}
            ref={regularBtnRef}
            className="flex-1 rounded-full bg-white px-3 py-2 shadow-sm regular"
          >
            Regular User
          </button>
          <button
            onClick={switchRegularProvider}
            ref={providerBtnRef}
            className="flex-1 rounded-full px-3 py-2 provider"
          >
            Provider
          </button>
        </div>
        <div className="mt-6 flex flex-col items-center rounded-xl border border-dashed border-slate-200 bg-slate-100 px-4 py-6 text-sm text-slate-500">
          <h2 className="mb-4 text-lg font-semibold text-slate-700">
            {`${isRegularUser ? "Regular User" : "Provider"} Registration Form`}
          </h2>

          <form
            onSubmit={handleSubmit(onSubmit)}
            className="space-y-4 auth-card"
          >
            {errors.email ? (
              <span className="text-sm text-rose-600">
                {errors.email.message}
              </span>
            ) : null}

            <input
              type="email"
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
              {...register("email", { required: true })}
              placeholder="Email"
            />

            {errors.password ? (
              <span className="text-sm text-rose-600">
                {errors.password.message}
              </span>
            ) : null}

            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 pr-16 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                {...register("password", { required: true })}
                placeholder="Password"
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-700"
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>

            {errors.confirmPassword ? (
              <span className="text-sm text-rose-600">
                {errors.confirmPassword.message}
              </span>
            ) : null}

            <div className="relative">
              <input
                type={showConfirmPassword ? "text" : "password"}
                className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 pr-16 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                {...register("confirmPassword", { required: true })}
                placeholder="Confirm password"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-700"
                aria-label={
                  showConfirmPassword ? "Hide password" : "Show password"
                }
              >
                {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>

            <LoadingButton
              type="submit"
              loading={loading}
              loadingText="Registering..."
              className="w-full rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:bg-emerald-400"
            >
              Create Account
            </LoadingButton>
          </form>
          {error ? (
            <p className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
              {error}
            </p>
          ) : null}
          {success ? (
            <p className="mt-4 rounded-lg bg-emerald-50 px-3 py-2 text-base text-emerald-700">
              Signup successful! Redirecting to login...
            </p>
          ) : null}
          <Link
            to="/login"
            className="mt-4 block text-sm text-emerald-600 hover:text-emerald-500"
          >
            Already have an account? Sign in
          </Link>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
