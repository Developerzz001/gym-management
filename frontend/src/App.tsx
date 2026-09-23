import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthLayout } from '@/layouts/AuthLayout';
import { MainLayout } from '@/layouts/MainLayout';
import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { LoginPage } from '@/pages/auth/LoginPage';
import { UnauthorizedPage } from '@/pages/misc/UnauthorizedPage';
import { NotFoundPage } from '@/pages/misc/NotFoundPage';
import { useAppSelector } from '@/app/hooks';

import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { ClientsPage, InquiriesPage } from '@/pages/admin/ClientsPage';
import { CoachesPage } from '@/pages/admin/CoachesPage';
import { DieticiansPage } from '@/pages/admin/DieticiansPage';
import { MembershipsPage } from '@/pages/admin/MembershipsPage';

import { CoachDashboardPage } from '@/pages/coach/CoachDashboardPage';
import { AssignedClientsPage } from '@/pages/coach/AssignedClientsPage';
import { WorkoutPlansPage } from '@/pages/coach/WorkoutPlansPage';
import { SessionsPage } from '@/pages/coach/SessionsPage';
import { ExercisesPage } from '@/pages/admin/ExercisesPage';

import { DieticianDashboardPage } from '@/pages/dietician/DieticianDashboardPage';
import { DietPlansPage } from '@/pages/dietician/DietPlansPage';
import { SupplementsPage } from '@/pages/dietician/SupplementsPage';
import { MedicinesPage } from '@/pages/dietician/MedicinesPage';

import { ClientDashboardPage } from '@/pages/client/ClientDashboardPage';
import { MyWorkoutPage } from '@/pages/client/MyWorkoutPage';
import { MyDietPage } from '@/pages/client/MyDietPage';
import { MyProgressPage } from '@/pages/client/MyProgressPage';
import { NotificationsPage } from '@/pages/client/NotificationsPage';
import { ProfilePage } from '@/pages/misc/ProfilePage';
import { BillingPage } from '@/pages/admin/BillingPage';
import { AttendancePage } from '@/pages/admin/AttendancePage';
import { MyInvoicesPage } from '@/pages/client/MyInvoicesPage';
import { BranchDashboardPage, BranchManagementPage, BranchReportsPage, BranchSettingsPage,
  OrganizationDashboardPage, OrganizationManagementPage, TransfersPage } from '@/pages/platform/MultiBranchPages';
import { UserManagementPage } from '@/pages/platform/UserManagementPage';

function homePathForRole(role: string | null): string {
  switch (role) {
    case 'SUPER_ADMIN':
      return '/platform/organizations';
    case 'ORGANIZATION_ADMIN':
      return '/platform/organization-dashboard';
    case 'BRANCH_MANAGER':
      return '/platform/branch-dashboard';
    case 'RECEPTIONIST':
      return '/platform/clients';
    case 'COACH':
      return '/coach/dashboard';
    case 'ADMIN':
      return '/admin/dashboard';
    case 'FITNESS_COACH':
      return '/coach/dashboard';
    case 'DIETICIAN':
      return '/dietician/dashboard';
    case 'CLIENT':
      return '/client/dashboard';
    default:
      return '/login';
  }
}

function RootRedirect() {
  const { isAuthenticated, role } = useAppSelector((state) => state.auth);
  return <Navigate to={isAuthenticated ? homePathForRole(role) : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />

      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/organizations" element={<OrganizationManagementPage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN', 'ORGANIZATION_ADMIN']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/branches" element={<BranchManagementPage />} />
        <Route path="/platform/organization-dashboard" element={<OrganizationDashboardPage />} />
        <Route path="/platform/reports" element={<BranchReportsPage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN', 'BRANCH_MANAGER']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/branch-dashboard" element={<BranchDashboardPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['BRANCH_MANAGER', 'RECEPTIONIST']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/inquiries" element={<InquiriesPage />} />
        <Route path="/platform/clients" element={<ClientsPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['BRANCH_MANAGER', 'RECEPTIONIST']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/billing" element={<BillingPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['BRANCH_MANAGER', 'RECEPTIONIST']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/memberships" element={<MembershipsPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['BRANCH_MANAGER']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/transfers" element={<TransfersPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['ORGANIZATION_ADMIN', 'BRANCH_MANAGER']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/users" element={<UserManagementPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN', 'ORGANIZATION_ADMIN', 'BRANCH_MANAGER']}><MainLayout /></ProtectedRoute>}>
        <Route path="/platform/settings" element={<BranchSettingsPage />} />
      </Route>

      <Route
        path="/profile"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'SUPER_ADMIN', 'ORGANIZATION_ADMIN', 'BRANCH_MANAGER', 'RECEPTIONIST', 'CLIENT', 'FITNESS_COACH', 'COACH', 'DIETICIAN']}>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<ProfilePage />} />
      </Route>

      <Route
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
        <Route path="/admin/inquiries" element={<InquiriesPage />} />
        <Route path="/admin/clients" element={<ClientsPage />} />
        <Route path="/admin/coaches" element={<CoachesPage />} />
        <Route path="/admin/dieticians" element={<DieticiansPage />} />
        <Route path="/admin/memberships" element={<MembershipsPage />} />
        <Route path="/admin/billing" element={<BillingPage />} />
        <Route path="/admin/attendance" element={<AttendancePage />} />
      </Route>

      <Route
        element={
          <ProtectedRoute allowedRoles={['FITNESS_COACH', 'COACH']}>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/coach/dashboard" element={<CoachDashboardPage />} />
        <Route path="/coach/clients" element={<AssignedClientsPage />} />
        <Route path="/coach/exercises" element={<ExercisesPage />} />
        <Route path="/coach/workout-plans" element={<WorkoutPlansPage />} />
        <Route path="/coach/sessions" element={<SessionsPage />} />
      </Route>

      <Route
        element={
          <ProtectedRoute allowedRoles={['DIETICIAN']}>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dietician/dashboard" element={<DieticianDashboardPage />} />
        <Route path="/dietician/diet-plans" element={<DietPlansPage />} />
        <Route path="/dietician/supplements" element={<SupplementsPage />} />
        <Route path="/dietician/medicines" element={<MedicinesPage />} />
      </Route>

      <Route
        element={
          <ProtectedRoute allowedRoles={['CLIENT']}>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/client/dashboard" element={<ClientDashboardPage />} />
        <Route path="/client/workout" element={<MyWorkoutPage />} />
        <Route path="/client/diet" element={<MyDietPage />} />
        <Route path="/client/progress" element={<MyProgressPage />} />
        <Route path="/client/invoices" element={<MyInvoicesPage />} />
        <Route path="/client/notifications" element={<NotificationsPage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

