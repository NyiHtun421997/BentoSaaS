import authService from "../services/authService";

export type ApiError = {
  status: number;
  message: string;
};

export function isApiError(e: unknown): e is ApiError {
  return (
    typeof e === "object" &&
    e !== null &&
    "status" in e &&
    "message" in e &&
    typeof (e as Record<string, unknown>).status === "number" &&
    typeof (e as Record<string, unknown>).message === "string"
  );
}

const baseUrl = import.meta.env.VITE_API_BASE_URL || "";

/**
 * Prevent redirect spam when many requests fail at once.
 * Module-level = shared across all requests.
 */
let alreadyRedirecting = false;

const buildUrl = (path: string) => {
  if (!baseUrl) return path;
  return path.startsWith("/") ? `${baseUrl}${path}` : `${baseUrl}/${path}`;
};

const getAuthToken = () => {
  return authService.getAuthenticatedUser()?.token;
};

const parseResponse = async (res: Response) => {
  const contentType = res.headers.get("content-type") || "";
  if (res.status === 204) return null;
  if (contentType.includes("application/json")) return res.json();
  return res.text();
};

const getReturnTo = () => {
  if (typeof window === "undefined") return "/";
  const { pathname, search, hash } = window.location;
  return `${pathname}${search}${hash}`;
};

const hardRedirect = (to: string) => {
  if (typeof window === "undefined") return;
  // Hard redirect ensures React state resets (important because http.ts can't call AuthProvider.logout()).
  window.location.replace(to);
};

const isOnPublicAuthPage = () => {
  if (typeof window === "undefined") return false;
  const p = window.location.pathname;
  return p === "/login" || p === "/signup";
};

const handleAuthFailureOnce = (status: number) => {
  if (alreadyRedirecting) return;

  // If you're already on login/signup, don't keep redirecting.
  if (isOnPublicAuthPage()) return;

  alreadyRedirecting = true;
  authService.logout();

  if (status === 401) {
    // Token invalid/expired -> clear in-memory auth
    authService.logout();

    const returnTo = encodeURIComponent(getReturnTo());
    hardRedirect(`/login?returnTo=${returnTo}`);
    return;
  }

  if (status === 403) {
    // Auth is valid, but permission denied -> do NOT logout
    hardRedirect("/forbidden");
    return;
  }
};

const handleError = async (res: Response): Promise<never> => {
  // First do global routing behavior (but only once)
  if (res.status === 401 || res.status === 403) {
    handleAuthFailureOnce(res.status);
  }

  // Then still throw a useful error for the caller (page) to show message / stop loading
  let message = res.statusText || "Request failed";
  try {
    const data = await parseResponse(res);

    // If backend returns plain text, use it
    if (typeof data === "string" && data.trim()) {
      message = data;
    }

    // If backend returns JSON, try to extract message/error
    if (data && typeof data === "object") {
      const obj = data as Record<string, unknown>;

      // 1️⃣ Preferred keys
      const preferred =
        (obj.message as string | undefined) ||
        (obj.error as string | undefined);

      if (preferred?.trim()) {
        message = preferred;
      } else {
        // 2️⃣ Fallback: take first string value in object
        const firstStringValue = Object.values(obj).find(
          (v) => typeof v === "string" && v.trim(),
        ) as string | undefined;

        if (firstStringValue) {
          message = firstStringValue;
        }
      }
    }
  } catch {
    // ignore parse errors
  }

  throw { status: res.status, message } satisfies ApiError;
};

/* ---------------- JSON APIs ---------------- */

export const apiRequest = async <T>(
  method: "GET" | "POST" | "PUT" | "DELETE",
  path: string,
  body?: unknown,
  extraHeaders?: Record<string, string>,
): Promise<T> => {
  const token = getAuthToken();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(extraHeaders ?? {}),
  };

  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(buildUrl(path), {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!res.ok) await handleError(res);
  return (await parseResponse(res)) as T;
};

export const apiGet = <T>(
  path: string,
  extraHeaders?: Record<string, string>,
) => apiRequest<T>("GET", path, undefined, extraHeaders);

export const apiPost = <T>(
  path: string,
  body?: unknown,
  extraHeaders?: Record<string, string>,
) => apiRequest<T>("POST", path, body, extraHeaders);

export const apiPut = <T>(
  path: string,
  body?: unknown,
  extraHeaders?: Record<string, string>,
) => apiRequest<T>("PUT", path, body, extraHeaders);

export const apiDelete = <T>(
  path: string,
  extraHeaders?: Record<string, string>,
) => apiRequest<T>("DELETE", path, undefined, extraHeaders);

/* ---------------- FORM LOGIN ---------------- */

export const apiPostFormWithHeaders = async (
  path: string,
  form: Record<string, string>,
) => {
  const body = new URLSearchParams(form).toString();

  const res = await fetch(buildUrl(path), {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body,
  });

  if (!res.ok) await handleError(res);

  return {
    data: await parseResponse(res),
    headers: res.headers,
  };
};
