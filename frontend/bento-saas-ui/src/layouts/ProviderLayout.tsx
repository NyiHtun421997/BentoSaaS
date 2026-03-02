import { Link, Outlet, useNavigate } from "react-router-dom";
import { useContext, useEffect, useMemo, useRef, useState } from "react";
import AuthContext from "../state/createContext";
import "./ProviderLayout.css";
import {
  FiClipboard,
  FiLogOut,
  FiBell,
  FiMenu,
  FiUser,
  FiGrid,
} from "react-icons/fi";
import { apiGet, apiPut } from "../lib/api/http";
import type { NotificationDto } from "../lib/api/types";
import { buildNotificationView } from "../lib/services/notificationService";

const ProviderLayout = () => {
  const authContext = useContext(AuthContext);
  const { authenticatedUser, logout } = authContext || {};
  const { token, email } = authenticatedUser || {};
  const navigate = useNavigate();

  const [menuOpen, setMenuOpen] = useState(false);
  const [notiOpen, setNotiOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const notiRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const data = await apiGet<NotificationDto[] | null>(
          `/notification/api/v1`,
        );
        setNotifications(Array.isArray(data) ? data : []);
      } catch {
        setNotifications([]);
      }
    };
    fetchNotifications();
  }, []);

  const safeNotifications = useMemo(
    () => (Array.isArray(notifications) ? notifications : []),
    [notifications],
  );

  const unreadCount = useMemo(
    () => safeNotifications.filter((n) => !n.read).length,
    [safeNotifications],
  );

  const sortedNotifications = useMemo(() => {
    return [...safeNotifications].sort((a, b) => {
      const ta = a.createdAt ? Date.parse(a.createdAt) : 0;
      const tb = b.createdAt ? Date.parse(b.createdAt) : 0;
      return tb - ta;
    });
  }, [safeNotifications]);

  const markNotiAsRead = async (id: number) => {
    await apiPut<NotificationDto>(`/notification/api/v1/${id}/read`);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n)),
    );
  };

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
                  to="/app/browse"
                  role="menuitem"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => setMenuOpen(false)}
                >
                  <FiGrid aria-hidden="true" />
                  Browse
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

            <Link to="/app/browse" className="inline-flex items-center gap-2">
              <FiGrid aria-hidden="true" />
              Browse
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
          <div className="relative" ref={notiRef}>
            <button
              onClick={() => {
                setNotiOpen((v) => {
                  const next = !v;
                  return next;
                });
              }}
            >
              <FiBell />
              {unreadCount > 0 && (
                <span className="ml-1 text-xs font-bold text-red-600">
                  {unreadCount}
                </span>
              )}
            </button>

            {notiOpen && (
              <div className="absolute right-0 mt-2 w-80 rounded-xl border bg-white shadow-lg">
                <div className="max-h-72 overflow-y-auto p-2">
                  {sortedNotifications.length === 0 ? (
                    <div>No notifications</div>
                  ) : (
                    sortedNotifications.map((noti) => {
                      const view = buildNotificationView(noti, true);

                      return (
                        <button
                          key={noti.id}
                          className={`mb-2 w-full rounded-lg border p-3 text-left hover:bg-slate-100 ${
                            noti.read ? "bg-white" : "bg-indigo-50"
                          }`}
                          onClick={() => {
                            setNotiOpen(false);
                            navigate(view.to);
                            markNotiAsRead(noti.id);
                          }}
                        >
                          <div className="font-semibold">{view.title}</div>
                          <div className="text-sm text-slate-600">
                            {view.subtitle}
                          </div>
                        </button>
                      );
                    })
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </nav>
      <Outlet />
    </div>
  );
};

export default ProviderLayout;
