import { Navigate, useLocation } from "react-router-dom";
import { useContext, type PropsWithChildren } from "react";
import AuthContext from "../state/createContext";

export const RequireAuthRoute = ({ children }: PropsWithChildren) => {
  const authContext = useContext(AuthContext);
  const token = authContext?.authenticatedUser?.token;
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};
