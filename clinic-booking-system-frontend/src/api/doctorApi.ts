import axiosInstance from './axiosInstance'
import type { ApiResponse, PagedResponse } from '@/types/api'
import type { Doctor, DoctorCreateRequest, DoctorSearchParams, DoctorUserOption } from '@/types/doctor'

export const doctorApi = {
  async getEligibleUsers(clinicId: number): Promise<DoctorUserOption[]> {
    const res = await axiosInstance.get<ApiResponse<DoctorUserOption[]>>('/doctors/eligible-users', {
      params: { clinicId },
    })
    return res.data.data
  },

  async create(payload: DoctorCreateRequest): Promise<Doctor> {
    const res = await axiosInstance.post<ApiResponse<Doctor>>('/doctors', payload)
    return res.data.data
  },

  async promotePatient(payload: DoctorCreateRequest): Promise<Doctor> {
    const res = await axiosInstance.post<ApiResponse<Doctor>>('/doctors/promote', payload)
    return res.data.data
  },

  async search(params: DoctorSearchParams = {}): Promise<PagedResponse<Doctor>> {
    const res = await axiosInstance.get<ApiResponse<PagedResponse<Doctor>>>('/doctors/search', {
      params,
    })
    return res.data.data
  },

  async getById(id: number): Promise<Doctor> {
    const res = await axiosInstance.get<ApiResponse<Doctor>>(`/doctors/${id}`)
    return res.data.data
  },

  async deactivate(id: number): Promise<void> {
    await axiosInstance.delete(`/doctors/${id}`)
  },

  async activate(id: number): Promise<void> {
    await axiosInstance.put(`/doctors/${id}/activate`)
  },

  async getByClinic(
    clinicId: number,
    params: { page?: number; size?: number } = {},
  ): Promise<PagedResponse<Doctor>> {
    const res = await axiosInstance.get<ApiResponse<PagedResponse<Doctor>>>(
      `/doctors/clinic/${clinicId}`,
      { params },
    )
    return res.data.data
  },
}
