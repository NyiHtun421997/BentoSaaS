import { jwtDecode } from "jwt-decode";
import { apiPostFormWithHeaders, apiPost, type ApiError } from "../api/http";
import type { AuthenticatedUser, Role } from "../api/types";

interface JwtPayload {
  sub?: string;
  role?: Array<{ authority: string }>;
  exp?: number;
  iat?: number;
  email?: string;
}

class AuthService {
  authenticatedUser: AuthenticatedUser | null;
  private storageKey = "auth:user";

  constructor() {
    this.authenticatedUser = null;
    this.hydrateFromSession();
  }

  async login(path: string, form: Record<string, string>) {
    const res = await apiPostFormWithHeaders(path, form);

    const authHeader = res.headers.get("Authorization");
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      throw {
        status: 0,
        message: "Missing Authorization header in login response",
      } satisfies ApiError;
    }

    const token = authHeader.replace("Bearer ", "").trim();

    const userId = res.headers.get("X-USER-ID");
    if (!userId) {
      throw {
        status: 0,
        message: "Missing X-USER-ID header in login response",
      } satisfies ApiError;
    }

    const decoded = this.decodeToken(token);
    if (!decoded) {
      throw {
        status: 0,
        message: "Failed to decode token",
      } satisfies ApiError;
    }

    const { role, email } = decoded;
    const effectiveRole = this.chooseEffectiveRole(
      this.extractRolesFromJwt(role || []),
    );

    if (!effectiveRole) {
      throw {
        status: 0,
        message: "No supported roles found in JWT",
      } satisfies ApiError;
    }

    this.authenticatedUser = {
      token,
      userId,
      authorities: [], // optional, derived from JWT already
      roles: this.extractRolesFromJwt(role || []),
      effectiveRole,
      email: email || "",
    };

    this.persistToSession();
    return this.authenticatedUser;
  }

  logout() {
    // Clear authenticated user
    this.authenticatedUser = null;
    this.clearSession();
  }

  async signup(path: string, body: unknown, isRegular: boolean) {
    path = isRegular ? path + "/regular" : path + "/provider";
    return await apiPost(path, body);
  }

  // Decode JWT to get user information
  decodeToken(token: string | null = null) {
    const tokenToUse = token || this.authenticatedUser?.token;
    if (!tokenToUse) {
      return null;
    }

    try {
      const decoded = jwtDecode<JwtPayload>(tokenToUse);

      // Check if token is expired
      const currentTime = Date.now() / 1000;
      if (decoded.exp && decoded.exp < currentTime) {
        this.authenticatedUser = null;
        return null;
      }

      return decoded;
    } catch (error) {
      console.error("Token decode error:", error);
      this.authenticatedUser = null;
      return null;
    }
  }

  chooseEffectiveRole = (roles: Role[]): Role | null => {
    if (roles.includes("ADMIN")) return "ADMIN";
    if (roles.includes("PROVIDER")) return "PROVIDER";
    if (roles.includes("USER")) return "USER";
    return null;
  };

  extractRolesFromJwt = (roleStrings: Array<{ authority: string }>): Role[] => {
    const roles = roleStrings
      .map((r: unknown) => (r as Record<string, unknown>)?.authority)
      .filter((v: unknown): v is string => typeof v === "string")
      .map((v: string) => v.replace("ROLE_", ""))
      .filter(
        (v: string) => v === "USER" || v === "PROVIDER" || v === "ADMIN",
      ) as Role[];

    return Array.from(new Set(roles));
  };

  getAuthenticatedUser() {
    return this.authenticatedUser || null;
  }

  private persistToSession() {
    if (typeof window === "undefined") return;
    if (!this.authenticatedUser) return;
    sessionStorage.setItem(
      this.storageKey,
      JSON.stringify(this.authenticatedUser),
    );
  }

  private clearSession() {
    if (typeof window === "undefined") return;
    sessionStorage.removeItem(this.storageKey);
  }

  private hydrateFromSession() {
    if (typeof window === "undefined") return;
    const raw = sessionStorage.getItem(this.storageKey);
    if (!raw) return;
    try {
      const parsed = JSON.parse(raw) as AuthenticatedUser;
      // Validate token still valid before trusting
      const decoded = this.decodeToken(parsed?.token || null);
      if (!decoded) {
        this.clearSession();
        this.authenticatedUser = null;
        return;
      }
      this.authenticatedUser = parsed;
    } catch {
      this.clearSession();
      this.authenticatedUser = null;
    }
  }
}

const authService = new AuthService();
export default authService;
