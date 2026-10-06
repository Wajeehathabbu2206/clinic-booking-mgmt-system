import axios from 'axios'
import type { ApiResponse } from '@/types/api'

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Cannot reach the server. Please check your connection.'
    }
    const body = error.response.data as Partial<ApiResponse<unknown>> | undefined
    if (body?.message) return body.message
    return `Request failed (${error.response.status})`
  }
  if (error instanceof Error) return error.message
  return 'Something went wrong'
}

export function getErrorStatus(error: unknown): number | null {
  return axios.isAxiosError(error) ? (error.response?.status ?? null) : null
}