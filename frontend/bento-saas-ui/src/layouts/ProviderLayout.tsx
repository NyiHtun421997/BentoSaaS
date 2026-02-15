import { Link, Outlet, useNavigate } from "react-router-dom";
import { useContext, useEffect, useRef, useState } from "react";
import AuthContext from "../state/createContext";
import "./ProviderLayout.css";
import {
  FiClipboard,
  FiBarChart2,
  FiLogOut,
  FiBell,
  FiMenu,
  FiUser,
} from "react-icons/fi";

const ProviderLayout = () => {
  const authContext = useContext(AuthContext);
  const { authenticatedUser, logout } = authContext || {};
  const { token, email } = authenticatedUser || {};
  const navigate = useNavigate();

  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const onDown = (e: MouseEvent) => {
      if (!menuOpen) return;
      const el = menuRef.current;
      if (!el) return;
      if (e.target instanceof Node && !el.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [menuOpen]);

  const handleSignOut = () => {
    if (logout) {
      logout();
    }
    navigate("/login", { replace: true });
  };

  return (
    <div className="provider-layout">
      <nav className="provider-nav">
        <div className="flex items-center gap-3">
          {/* Left-most menu */}
          <div className="relative" ref={menuRef}>
            <button
              type="button"
              className="inline-flex items-center justify-center rounded-full border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
              aria-label="Open menu"
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              onClick={() => setMenuOpen((v) => !v)}
            >
              <FiMenu aria-hidden="true" />
            </button>

            {menuOpen ? (
              <div
                role="menu"
                className="absolute left-0 mt-2 w-56 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-lg"
              >
                <Link
                  to="/provider/plans"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiClipboard aria-hidden="true" />
                  Plans
                </Link>
                <Link
                  to="/provider/insights"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiBarChart2 aria-hidden="true" />
                  Insights
                </Link>
                <div className="my-1 h-px bg-slate-200" />
                <Link
                  to="/provider/profile"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiUser aria-hidden="true" />
                  Edit
                </Link>
              </div>
            ) : null}
          </div>

          {/* Existing top nav links */}
          <div className="provider-nav-links">
            <Link
              to="/provider/plans"
              className="inline-flex items-center gap-2"
            >
              <FiClipboard aria-hidden="true" />
              Plans
            </Link>
            <Link
              to="/provider/insights"
              className="inline-flex items-center gap-2"
            >
              <FiBarChart2 aria-hidden="true" />
              Insights
            </Link>
          </div>
        </div>

        <div className="provider-nav-meta">
          {/* Email becomes a link/button to profile edit */}
          {email ? (
            <Link
              to="/provider/profile"
              className="rounded-full border border-transparent px-2 py-1 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              <div className="inline-flex items-center gap-2">
                <FiUser aria-hidden="true" />
                {email}
              </div>
            </Link>
          ) : null}

          {token ? (
            <button
              type="button"
              className="nav-signout rounded-full border border-slate-200 px-3 py-1.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              onClick={handleSignOut}
            >
              <FiLogOut className="mr-2 inline-block" aria-hidden="true" />
              Sign out
            </button>
          ) : null}

          {/* Notification button (right side) */}
          <button
            type="button"
            className="inline-flex items-center justify-center rounded-full border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            aria-label="Notifications"
          >
            <FiBell aria-hidden="true" />
          </button>
        </div>
      </nav>
      <Outlet />
    </div>
  );
};

export default ProviderLayout;
