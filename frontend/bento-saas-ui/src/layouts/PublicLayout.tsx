import { Link, Outlet } from "react-router-dom";
import { useContext } from "react";
import AuthContext from "../state/createContext";
import "./PublicLayout.css";
import { FiLogIn, FiUserPlus, FiLogOut } from "react-icons/fi";

const PublicLayout = () => {
  const authContext = useContext(AuthContext);
  const token = authContext?.authenticatedUser?.token;

  return (
    <div className="public-layout">
      <nav className="public-nav">
        <div className="public-nav-links">
          <Link to="/login" className="inline-flex items-center gap-2">
            <FiLogIn aria-hidden="true" />
            Login
          </Link>
          <Link to="/signup" className="inline-flex items-center gap-2">
            <FiUserPlus aria-hidden="true" />
            Signup
          </Link>
        </div>
        {token ? (
          <button
            type="button"
            className="nav-signout rounded-full border border-slate-200 px-3 py-1.5 text-sm font-semibold text-slate-700 transition"
            onClick={() => authContext?.logout()}
          >
            <FiLogOut className="mr-2 inline-block" aria-hidden="true" />
            Sign out
          </button>
        ) : null}
      </nav>
      <Outlet />
    </div>
  );
};

export default PublicLayout;
