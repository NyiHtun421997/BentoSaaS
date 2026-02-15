import { useState, type PropsWithChildren } from "react";
import type { AuthenticatedUser } from "../lib/api/types";
import authService from "../lib/services/authService";
import AuthContext from "./createContext";

export type AuthContextType = {
  authenticatedUser: AuthenticatedUser | null;
  login: (
    path: string,
    form: Record<string, string>,
  ) => Promise<AuthenticatedUser>;
  logout: () => Promise<void>;
};

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [authenticatedUser, setAuthenticatedUser] =
    useState<AuthenticatedUser | null>(() =>
      authService.getAuthenticatedUser(),
    );

  const login = async (path: string, form: Record<string, string>) => {
    const authenticatedUser = await authService.login(path, form);
    setAuthenticatedUser(authenticatedUser);
    return authenticatedUser;
  };

  const logout = async () => {
    authService.logout();
    setAuthenticatedUser(null);
  };

  const value: AuthContextType = {
    authenticatedUser,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
