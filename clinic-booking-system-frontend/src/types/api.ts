export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp?: string
}

// Error bodies may omit `data` entirely (backend uses NON_NULL).
export interface ApiErrorResponse {
  success: false
  message: string
  data?: unknown
  timestamp?: string
}

export interface PagedResponse<T> {
  content: T[]
  pageNumber: number 
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}