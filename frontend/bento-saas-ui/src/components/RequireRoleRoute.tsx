import { Navigate } from "react-router-dom";
import type { Role } from "../lib/api/types";
import { useContext, type PropsWithChildren } from "react";
import AuthContext from "../state/createContext";

type RequireRoleProps = PropsWithChildren & {
  allow: Role[];
};

export const RequireRoleRoute = ({ allow, children }: RequireRoleProps) => {
  const authContext = useContext(AuthContext);

  // allow if user has ANY of the allowed roles
  const allowed =
    authContext?.authenticatedUser?.roles?.some((r) => allow.includes(r)) ??
    false;

  if (!allowed) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
};
