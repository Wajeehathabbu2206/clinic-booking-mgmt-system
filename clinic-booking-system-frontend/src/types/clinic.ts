import type { Role } from './auth'

export type ClinicStatus = 'ACTIVE' | 'INACTIVE'
export type StaffStatus = 'ACTIVE' | 'INACTIVE' | 'REMOVED'

// Matches backend ClinicResponse
export interface Clinic {
  id: number
  name: string
  registrationNumber: string
  description?: string | null
  addressLine1: string
  addressLine2?: string | null
  city: string
  state: string
  pincode: string
  phone: string
  email?: string | null
  status: ClinicStatus
  createdByUserId: number
  createdAt: string
}

// Matches backend ClinicCreateRequest
export interface ClinicCreateRequest {
  name: string
  registrationNumber: string
  description?: string
  addressLine1: string
  addressLine2?: string
  city: string
  state: string
  pincode: string
  phone: string
  email?: string
}

// Matches backend ClinicUpdateRequest (registration number cannot be changed)
export interface ClinicUpdateRequest {
  name?: string
  description?: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  state?: string
  pincode?: string
  phone?: string
  email?: string
}

export interface ClinicListParams {
  city?: string
  search?: string
  status?: ClinicStatus
  page?: number // 0-based
  size?: number
}

// Matches backend StaffAssignRequest
export interface StaffAssignRequest {
  userId: number
  roleInClinic: Role
}

// Matches backend StaffResponse
export interface ClinicStaff {
  staffId: number
  userId: number
  userFullName: string
  userEmail: string
  roleInClinic: Role
  status: StaffStatus
}