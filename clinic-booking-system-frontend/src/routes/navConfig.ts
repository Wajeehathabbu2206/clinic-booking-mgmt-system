import { ROLES } from '@/types/auth'
import type { Role } from '@/types/auth'
import { PATHS } from './paths'

// Roles allowed on role-restricted routes (undefined = any logged-in user).
export const ROUTE_ROLES = {
  calendar: [ROLES.SUPER_ADMIN, ROLES.CLINIC_ADMIN, ROLES.DOCTOR, ROLES.RECEPTIONIST] as Role[],
  // Backend: records are visible only to the patient and the treating doctor.
  medicalRecords: [ROLES.PATIENT, ROLES.DOCTOR] as Role[],
}

export interface NavItem {
  label: string
  to: string
  roles?: Role[] // omitted = visible to every logged-in user
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', to: PATHS.dashboard },
  { label: 'My Profile', to: PATHS.profile },
  { label: 'Clinics', to: PATHS.clinics },
  { label: 'Find Doctors', to: PATHS.doctors },
  { label: 'Appointments', to: PATHS.appointments },
  { label: 'Calendar', to: PATHS.calendar, roles: ROUTE_ROLES.calendar },
  { label: 'Medical Records', to: PATHS.medicalRecords, roles: ROUTE_ROLES.medicalRecords },
  { label: 'Notifications', to: PATHS.notifications },
]