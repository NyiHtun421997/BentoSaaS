import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { useState, useContext } from "react";
import AuthContext from "../../state/createContext";
import type { AuthenticatedUser } from "../../lib/api/types";
import type { ApiError } from "../../lib/api/http";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import "./LoginPage.css";
import { Link } from "react-router-dom";
import LoadingButton from "../../components/LoadingButton";

/* ---------- Types & Constants ---------- */
const LoginFormSchema = z.object({
  email: z
    .email({ message: "Please enter a valid email address" })
    .min(1, { message: "Please enter an email address" }),
  password: z
    .string()
    .min(8, { message: "Password must be at least 8 characters long" }),
});

type LoginForm = z.infer<typeof LoginFormSchema>;

const LOGIN_PATH = "/user/api/auth/login";

/* ---------- Component ---------- */

const LoginPage = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(LoginFormSchema),
  });

  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is not provided");
  }
  const { login } = authContext;

  const onSubmit = async (values: LoginForm) => {
    setError(null);
    setLoading(true);

    try {
      const authenticatedUser: AuthenticatedUser = await login(LOGIN_PATH, {
        username: values.email, // Spring form-login default
        password: values.password,
      });

      if (authenticatedUser.effectiveRole === "USER")
        navigate("/app/browse", { replace: true });
      else if (authenticatedUser.effectiveRole === "PROVIDER")
        navigate("/provider/plans", { replace: true });
      else navigate("/admin", { replace: true });
    } catch (err) {
      const e = err as Partial<ApiError>;
      setError(e.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-subtitle">Sign in to manage your bento plans.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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

          <input
            type="password"
            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
            {...register("password", { required: true })}
            placeholder="Password"
          />

          <LoadingButton
            type="submit"
            loading={loading}
            loadingText="Logging in..."
            className="w-full rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:bg-emerald-400"
          >
            Login
          </LoadingButton>
        </form>

        {error ? (
          <p className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
            {error}
          </p>
        ) : null}

        <Link
          to="/signup"
          className="mt-4 block text-sm text-emerald-600 hover:text-emerald-500"
        >
          Don't have an account? Sign up
        </Link>
      </div>
    </div>
  );
};

export default LoginPage;
