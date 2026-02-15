import React from "react";
import type { AuthContextType } from "./AuthContext";

const AuthContext = React.createContext<AuthContextType | null>(null);

export default AuthContext;
