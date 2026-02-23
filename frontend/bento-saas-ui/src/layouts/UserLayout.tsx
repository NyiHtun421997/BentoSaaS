import { Link, Outlet, useNavigate } from "react-router-dom";
import { useContext, useEffect, useMemo, useRef, useState } from "react";
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
import { LuChefHat } from "react-icons/lu";
import type { NotificationDto } from "../lib/api/types";
import { apiGet, apiPut } from "../lib/api/http";
import { buildNotificationView } from "../lib/services/notificationService";

const UserLayout = () => {
  const authContext = useContext(AuthContext);
  const token = authContext?.authenticatedUser?.token;
  const email = authContext?.authenticatedUser?.email;
  const isProvider = authContext?.authenticatedUser?.roles.includes("PROVIDER");
  const navigate = useNavigate();

  const [menuOpen, setMenuOpen] = useState(false);
  const [notiOpen, setNotiOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const notiRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const fetchNotifications = async () => {
      const data = await apiGet<NotificationDto[]>(`/notification/api/v1`);
      setNotifications(data);
    };
    fetchNotifications();
  }, []);

  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.read).length,
    [notifications],
  );

  const sortedNotifications = useMemo(() => {
    return [...notifications].sort((a, b) => {
      const ta = a.createdAt ? Date.parse(a.createdAt) : 0;
      const tb = b.createdAt ? Date.parse(b.createdAt) : 0;
      return tb - ta;
    });
  }, [notifications]);

  const markNotiAsRead = async (id: number) => {
    await apiPut<NotificationDto>(`/notification/api/v1/${id}`);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n)),
    );
  };

  useEffect(() => {
    const onDown = (e: MouseEvent) => {
      if (!menuOpen && !notiOpen) return;
      if (!(e.target instanceof Node)) return;

      const menuEl = menuRef.current;
      const notiEl = notiRef.current;

      if (menuOpen && menuEl && !menuEl.contains(e.target)) {
        setMenuOpen(false);
      }

      if (notiOpen && notiEl && !notiEl.contains(e.target)) {
        setNotiOpen(false);
      }
    };
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [menuOpen, notiOpen]);

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
                {isProvider ? (
                  <Link
                    to="/provider/plans"
                    role="menuitem"
                    className="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                    onClick={() => setMenuOpen(false)}
                  >
                    <LuChefHat aria-hidden="true" />
                    Provider
                  </Link>
                ) : null}
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
            {isProvider ? (
              <Link
                to="/provider/plans"
                className="inline-flex items-center gap-2"
              >
                <LuChefHat aria-hidden="true" />
                Provider
              </Link>
            ) : null}
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
          {/* Notifications */}
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
                      const view = buildNotificationView(noti, isProvider);

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

export default UserLayout;
