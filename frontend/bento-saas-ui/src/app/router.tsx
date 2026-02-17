import { createBrowserRouter, Navigate } from "react-router-dom";
import PublicLayout from "../layouts/PublicLayout";
import UserLayout from "../layouts/UserLayout";
import ProviderLayout from "../layouts/ProviderLayout";
import { RequireAuthRoute } from "../components/RequreAuthRoute";
import { RequireRoleRoute } from "../components/RequireRoleRoute";
import { AuthenticatedRoute } from "../components/AuthenticatedRoute";

import LoginPage from "../pages/public/LoginPage";
import SignupPage from "../pages/public/SignupPage";

import BrowsePlansPage from "../pages/user/BrowsePlansPage";
import PlanDetailsPage from "../pages/user/PlanDetailsPage";
import SubscriptionsPage from "../pages/user/SubscriptionsPage";
import InvoicesPage from "../pages/user/InvoicesPage";

import ProviderPlansPage from "../pages/provider/ProviderPlansPage";
import ProviderInsightsPage from "../pages/provider/ProviderInsightsPage";

import ForbiddenPage from "../pages/ForbiddenPage";
import NotFoundPage from "../pages/NotFoundPage";
import ProfileEditPage from "../pages/user/ProfilePage";
import ProviderPlanCreatePage from "../pages/provider/ProviderPlanCreatePage";
import ProviderPlanEditPage from "../pages/provider/ProviderPlanEditPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <AuthenticatedRoute>
        <PublicLayout />
      </AuthenticatedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/login" replace /> },
      { path: "login", element: <LoginPage /> },
      { path: "signup", element: <SignupPage /> },
    ],
  },
  {
    element: (
      <RequireAuthRoute>
        <RequireRoleRoute allow={["USER"]}>
          <UserLayout />
        </RequireRoleRoute>
      </RequireAuthRoute>
    ),
    children: [
      { path: "app/browse", element: <BrowsePlansPage /> },
      { path: "app/plans/:planId", element: <PlanDetailsPage /> },
      { path: "app/subscriptions", element: <SubscriptionsPage /> },
      { path: "app/invoices", element: <InvoicesPage /> },
      { path: "app/profile", element: <ProfileEditPage /> },
    ],
  },
  {
    element: (
      <RequireAuthRoute>
        <RequireRoleRoute allow={["PROVIDER"]}>
          <ProviderLayout />
        </RequireRoleRoute>
      </RequireAuthRoute>
    ),
    children: [
      { path: "provider/plans", element: <ProviderPlansPage /> },
      { path: "provider/insights", element: <ProviderInsightsPage /> },
      {
        path: "/provider/plans/new",
        element: <ProviderPlanCreatePage />,
      },
      {
        path: "/provider/plans/:planId/edit",
        element: <ProviderPlanEditPage />,
      },
      {
        path: "/provider/insights/:planId",
        element: <ProviderInsightsPage />,
      },
    ],
  },
  { path: "/forbidden", element: <ForbiddenPage /> },
  { path: "*", element: <NotFoundPage /> },
]);
