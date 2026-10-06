import axios from 'axios'
import type { ApiErrorResponse } from '@/types/api'

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Cannot reach the server. Please check your connection.'
    }
    const body = error.response.data as Partial<ApiErrorResponse> | undefined
    if (body?.message) return body.message
    return `Request failed (${error.response.status})`
  }
  if (error instanceof Error) return error.message
  return 'Something went wrong'
}

export function getErrorStatus(error: unknown): number | null {
  return axios.isAxiosError(error) ? (error.response?.status ?? null) : null
}

/**
 * Returns a { fieldName: message } map when the backend attaches
 * validation details in `data`, otherwise an empty object.
 */
export function getFieldErrors(error: unknown): Record<string, string> {
  if (!axios.isAxiosError(error)) return {}
  const data = (error.response?.data as Partial<ApiErrorResponse> | undefined)?.data
  if (!data || typeof data !== 'object' || Array.isArray(data)) return {}

  const result: Record<string, string> = {}
  for (const [key, value] of Object.entries(data as Record<string, unknown>)) {
    if (typeof value === 'string') result[key] = value
  }
  return result
}