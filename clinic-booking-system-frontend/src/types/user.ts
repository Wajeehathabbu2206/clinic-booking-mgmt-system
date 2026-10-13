import type { Role } from './auth'

export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

// Matches backend UserProfileResponse
export interface UserProfile {
  id: number
  email: string
  fullName: string
  phoneNumber?: string | null
  profilePictureUrl?: string | null
  gender?: Gender | null
  dateOfBirth?: string | null // yyyy-MM-dd
  addressLine1?: string | null
  city?: string | null
  state?: string | null
  pincode?: string | null
  role: Role
  enabled: boolean
  createdAt: string
}

// Matches backend UserProfileUpdateRequest (all fields optional)
export interface UserProfileUpdateRequest {
  fullName?: string
  phoneNumber?: string
  profilePictureUrl?: string
  gender?: Gender
  dateOfBirth?: string
  addressLine1?: string
  city?: string
  state?: string
  pincode?: string
}

// Matches backend RoleUpdateRequest (assumed shape)
export interface RoleUpdateRequest {
  role: Role
}