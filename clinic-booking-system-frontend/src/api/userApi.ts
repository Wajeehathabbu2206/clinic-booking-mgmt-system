import axiosInstance from './axiosInstance'
import type { ApiResponse, PagedResponse } from '@/types/api'
import type { Role } from '@/types/auth'
import type { RoleUpdateRequest, UserProfile, UserProfileUpdateRequest, UserPromotionCandidate } from '@/types/user'

export const userApi = {
  async getClinicOwnerPromotionCandidates(params: { page?: number; size?: number } = {}): Promise<PagedResponse<UserPromotionCandidate>> {
    const res = await axiosInstance.get<ApiResponse<PagedResponse<UserPromotionCandidate>>>(
      '/users/promotion-candidates', { params },
    )
    return res.data.data
  },

  async promoteToClinicOwner(id: number): Promise<UserProfile> {
    const res = await axiosInstance.post<ApiResponse<UserProfile>>(`/users/${id}/promote-clinic-owner`)
    return res.data.data
  },

  async getMyProfile(): Promise<UserProfile> {
    const res = await axiosInstance.get<ApiResponse<UserProfile>>('/users/me')
    return res.data.data
  },

  async updateMyProfile(payload: UserProfileUpdateRequest): Promise<UserProfile> {
    const res = await axiosInstance.put<ApiResponse<UserProfile>>('/users/me', payload)
    return res.data.data
  },

  // SUPER_ADMIN only
  async getUserById(id: number): Promise<UserProfile> {
    const res = await axiosInstance.get<ApiResponse<UserProfile>>(`/users/${id}`)
    return res.data.data
  },

  // SUPER_ADMIN only
  async updateUserRole(id: number, role: Role): Promise<UserProfile> {
    const body: RoleUpdateRequest = { role }
    const res = await axiosInstance.patch<ApiResponse<UserProfile>>(`/users/${id}/role`, body)
    return res.data.data
  },
}
