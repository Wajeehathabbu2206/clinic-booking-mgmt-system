import axiosInstance from './axiosInstance'
import type { ApiResponse } from '@/types/api'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export const authApi = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/login', payload)
    return res.data.data
  },

  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/register', payload)
    return res.data.data
  },
}