import axiosInstance from './axiosInstance'
import type { ApiResponse } from '@/types/api'
import type { Role } from '@/types/auth'
import type { RoleUpdateRequest, UserProfile, UserProfileUpdateRequest } from '@/types/user'

export const userApi = {
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