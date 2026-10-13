import { createBrowserRouter } from 'react-router-dom'
import { AuthLayout, DashboardLayout, PublicLayout } from '@/components/layout'
import PlaceholderPage from '@/components/PlaceholderPage'
import LoginPage from '@/features/auth/LoginPage'
import RegisterPage from '@/features/auth/RegisterPage'
import ClinicDetailPage from '@/features/clinic/ClinicDetailPage'
import ClinicFormPage from '@/features/clinic/ClinicFormPage'
import ClinicListPage from '@/features/clinic/ClinicListPage'
import ProfilePage from '@/features/user/ProfilePage'
import DoctorListPage from '@/features/doctor/DoctorListPage'
import DoctorDetailPage from '@/features/doctor/DoctorDetailPage'
import DoctorFormPage from '@/features/doctor/DoctorFormPage'
import ForbiddenPage from '@/pages/ForbiddenPage'
import HomePage from '@/pages/HomePage'
import NotFoundPage from '@/pages/NotFoundPage'
import { ROUTE_ROLES } from './navConfig'
import { PATHS } from './paths'
import ProtectedRoute from './ProtectedRoute'

export const router = createBrowserRouter([
  // Public pages (guests included)
  {
    element: <PublicLayout />,
    children: [
      { path: PATHS.home, element: <HomePage /> },
      { path: PATHS.clinics, element: <ClinicListPage /> },
      { path: PATHS.clinicDetail(), element: <ClinicDetailPage /> },
      { path: PATHS.doctors, element: <DoctorListPage /> },
      {
        element: <ProtectedRoute allowedRoles={ROUTE_ROLES.doctorManage} />,
        children: [{ path: PATHS.doctorNew, element: <DoctorFormPage /> }],
      },
      {
        path: PATHS.doctorDetail(),
        element: <DoctorDetailPage />,
      },
      { path: PATHS.forbidden, element: <ForbiddenPage /> },
    ],
  },

  // Login / register
  {
    element: <AuthLayout />,
    children: [
      { path: PATHS.login, element: <LoginPage /> },
      { path: PATHS.register, element: <RegisterPage /> },
    ],
  },

  // Logged-in area: must be authenticated
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          {
            path: PATHS.dashboard,
            element: <PlaceholderPage title="Dashboard" phase="F10 – Analytics Dashboard" />,
          },
          { path: PATHS.profile, element: <ProfilePage /> },
          {
            path: PATHS.appointments,
            element: <PlaceholderPage title="Appointments" phase="F5 – Appointment Booking" />,
          },
          {
            path: PATHS.notifications,
            element: <PlaceholderPage title="Notifications" phase="F7 – Notifications" />,
          },

          // Role-restricted pages
          {
            element: <ProtectedRoute allowedRoles={ROUTE_ROLES.clinicManage} />,
            children: [
              {
                path: PATHS.clinicNew,
                element: <ClinicFormPage />,
              },
              {
                path: PATHS.clinicEdit(),
                element: <ClinicFormPage />,
              },
            ],
          },
          {
            element: <ProtectedRoute allowedRoles={ROUTE_ROLES.calendar} />,
            children: [
              {
                path: PATHS.calendar,
                element: <PlaceholderPage title="Calendar" phase="F6 – Calendar Views" />,
              },
            ],
          },
          {
            element: <ProtectedRoute allowedRoles={ROUTE_ROLES.medicalRecords} />,
            children: [
              {
                path: PATHS.medicalRecords,
                element: <PlaceholderPage title="Medical Records" phase="F8 – Medical Records" />,
              },
            ],
          },
        ],
      },
    ],
  },

  { path: '*', element: <NotFoundPage /> },
])
