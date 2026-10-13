import axiosInstance from './axiosInstance'
import type { ApiResponse, PagedResponse } from '@/types/api'
import type {
  Clinic,
  ClinicCreateRequest,
  ClinicListParams,
  ClinicStaff,
  ClinicUpdateRequest,
  StaffAssignRequest,
} from '@/types/clinic'

export const clinicApi = {
  async list(params: ClinicListParams = {}): Promise<PagedResponse<Clinic>> {
    const res = await axiosInstance.get<ApiResponse<PagedResponse<Clinic>>>('/clinics', { params })
    return res.data.data
  },

  async getById(id: number): Promise<Clinic> {
    const res = await axiosInstance.get<ApiResponse<Clinic>>(`/clinics/${id}`)
    return res.data.data
  },

  async create(payload: ClinicCreateRequest): Promise<Clinic> {
    const res = await axiosInstance.post<ApiResponse<Clinic>>('/clinics', payload)
    return res.data.data
  },

  async update(id: number, payload: ClinicUpdateRequest): Promise<Clinic> {
    const res = await axiosInstance.put<ApiResponse<Clinic>>(`/clinics/${id}`, payload)
    return res.data.data
  },

  // SUPER_ADMIN only
  async deactivate(id: number): Promise<void> {
    await axiosInstance.delete(`/clinics/${id}`)
  },

  // ---- Staff ----

  async getStaff(clinicId: number): Promise<ClinicStaff[]> {
    const res = await axiosInstance.get<ApiResponse<ClinicStaff[]>>(`/clinics/${clinicId}/staff`)
    return res.data.data
  },

  async assignStaff(clinicId: number, payload: StaffAssignRequest): Promise<ClinicStaff> {
    const res = await axiosInstance.post<ApiResponse<ClinicStaff>>(
      `/clinics/${clinicId}/staff`,
      payload,
    )
    return res.data.data
  },

  async removeStaff(clinicId: number, staffId: number): Promise<void> {
    await axiosInstance.delete(`/clinics/${clinicId}/staff/${staffId}`)
  },
}