// Matches backend DoctorResponse
export interface Doctor {
  id: number
  userId: number
  fullName: string
  email: string
  phone?: string | null
  clinicId: number
  clinicName: string
  specialization: string
  qualification?: string | null
  registrationNumber?: string | null
  experienceYears?: number | null
  consultationFee?: number | null
  bio?: string | null
  averageRating?: number | null
  totalReviews?: number | null
  isActive: boolean
  createdAt: string
}

export interface DoctorSearchParams {
  specialization?: string
  clinicId?: number
  name?: string
  minExperience?: number
  maxFee?: number
  page?: number // 0-based
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

export interface DoctorCreateRequest {
  userId: number
  clinicId: number
  specialization: string
  qualification?: string
  registrationNumber?: string
  experienceYears?: number
  consultationFee?: number
  bio?: string
}

export interface DoctorUserOption {
  userId: number
  fullName: string
  email: string
}
