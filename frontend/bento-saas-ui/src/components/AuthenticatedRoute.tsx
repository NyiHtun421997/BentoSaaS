import { useContext, type PropsWithChildren } from "react";
import { Navigate, useLocation } from "react-router-dom";
import AuthContext from "../state/createContext";

export const AuthenticatedRoute = ({ children }: PropsWithChildren) => {
  const authContext = useContext(AuthContext);
  const location = useLocation();

  if (!authContext || !authContext.authenticatedUser) {
    // Redirect to login page with return url
    return <>{children}</>;
  }

  if (authContext.authenticatedUser.roles.includes("ADMIN")) {
    return <Navigate to="/admin" state={{ from: location }} replace />;
  }

  if (authContext.authenticatedUser.roles.includes("PROVIDER")) {
    return <Navigate to="/provider/plans" state={{ from: location }} replace />;
  }

  if (authContext.authenticatedUser.roles.includes("USER")) {
    return <Navigate to="/app/browse" state={{ from: location }} replace />;
  }
};
