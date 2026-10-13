import axiosInstance from './axiosInstance'
import type { ApiResponse, PagedResponse } from '@/types/api'
import type { Doctor, DoctorSearchParams } from '@/types/doctor'

export const doctorApi = {
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