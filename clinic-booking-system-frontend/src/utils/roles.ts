import type { Role } from '@/types/auth'

export const ROLE_LABELS: Record<Role, string> = {
  SUPER_ADMIN: 'Super Admin',
  CLINIC_ADMIN: 'Clinic Admin',
  DOCTOR: 'Doctor',
  RECEPTIONIST: 'Receptionist',
  PATIENT: 'Patient',
}