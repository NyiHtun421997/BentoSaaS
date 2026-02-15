import { Link, Outlet, useNavigate } from "react-router-dom";
import { useContext, useEffect, useRef, useState } from "react";
import AuthContext from "../state/createContext";
import "./UserLayout.css";
import {
  FiGrid,
  FiInbox,
  FiFileText,
  FiLogOut,
  FiBell,
  FiMenu,
  FiUser,
} from "react-icons/fi";

const UserLayout = () => {
  const authContext = useContext(AuthContext);
  const token = authContext?.authenticatedUser?.token;
  const email = authContext?.authenticatedUser?.email;
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
    authContext?.logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="app-layout">
      <nav className="app-nav">
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
                  to="/app/browse"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiGrid aria-hidden="true" />
                  Browse
                </Link>
                <Link
                  to="/app/subscriptions"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiInbox aria-hidden="true" />
                  Subscriptions
                </Link>
                <Link
                  to="/app/invoices"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiFileText aria-hidden="true" />
                  Invoices
                </Link>
                <div className="my-1 h-px bg-slate-200" />
                <Link
                  to="/app/profile"
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
          <div className="app-nav-links">
            <Link to="/app/browse" className="inline-flex items-center gap-2">
              <FiGrid aria-hidden="true" />
              Browse
            </Link>
            <Link
              to="/app/subscriptions"
              className="inline-flex items-center gap-2"
            >
              <FiInbox aria-hidden="true" />
              Subscriptions
            </Link>
            <Link to="/app/invoices" className="inline-flex items-center gap-2">
              <FiFileText aria-hidden="true" />
              Invoices
            </Link>
          </div>
        </div>

        <div className="app-nav-meta">
          {/* Email becomes a link/button to profile edit */}
          {email ? (
            <Link
              to="/app/profile"
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

export default UserLayout;
