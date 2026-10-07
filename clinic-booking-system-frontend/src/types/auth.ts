export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  CLINIC_ADMIN: 'CLINIC_ADMIN',
  DOCTOR: 'DOCTOR',
  PATIENT: 'PATIENT',
  RECEPTIONIST: 'RECEPTIONIST',
} as const

export type Role = (typeof ROLES)[keyof typeof ROLES]

export interface AuthUser {
  id: number
  email: string
  fullName: string
  role: Role
}

export interface AuthResponse extends AuthUser {
  token: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest extends LoginRequest {
  fullName: string
  phoneNumber?: string
}
