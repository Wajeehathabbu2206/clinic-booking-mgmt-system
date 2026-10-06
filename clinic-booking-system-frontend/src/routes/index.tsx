import { createBrowserRouter } from 'react-router-dom'
import { AuthLayout, DashboardLayout, PublicLayout } from '@/components/layout'
import PlaceholderPage from '@/components/PlaceholderPage'
import HomePage from '@/pages/HomePage'
import NotFoundPage from '@/pages/NotFoundPage'
import { PATHS } from './paths'

export const router = createBrowserRouter([
  // Public pages
  {
    element: <PublicLayout />,
    children: [
      { path: PATHS.home, element: <HomePage /> },
      {
        path: PATHS.doctors,
        element: <PlaceholderPage title="Find Doctors" phase="F3 – Doctor Search" />,
      },
      {
        path: PATHS.doctorDetail(),
        element: <PlaceholderPage title="Doctor Details" phase="F3 – Doctor Search" />,
      },
    ],
  },

  // Login / register
  {
    element: <AuthLayout />,
    children: [
      {
        path: PATHS.login,
        element: <PlaceholderPage title="Log in" phase="F1 – Auth & Roles" />,
      },
      {
        path: PATHS.register,
        element: <PlaceholderPage title="Create account" phase="F1 – Auth & Roles" />,
      },
    ],
  },

  // Logged-in area (route protection is added in F1)
  {
    element: <DashboardLayout />,
    children: [
      {
        path: PATHS.dashboard,
        element: <PlaceholderPage title="Dashboard" phase="F10 – Analytics Dashboard" />,
      },
      {
        path: PATHS.profile,
        element: <PlaceholderPage title="My Profile" phase="F2 – Clinic & User Profiles" />,
      },
      {
        path: PATHS.clinics,
        element: <PlaceholderPage title="Clinics" phase="F2 – Clinic & User Profiles" />,
      },
      {
        path: PATHS.appointments,
        element: <PlaceholderPage title="Appointments" phase="F5 – Appointment Booking" />,
      },
      {
        path: PATHS.calendar,
        element: <PlaceholderPage title="Calendar" phase="F6 – Calendar Views" />,
      },
      {
        path: PATHS.medicalRecords,
        element: <PlaceholderPage title="Medical Records" phase="F8 – Medical Records" />,
      },
      {
        path: PATHS.notifications,
        element: <PlaceholderPage title="Notifications" phase="F7 – Notifications" />,
      },
    ],
  },

  { path: '*', element: <NotFoundPage /> },
])