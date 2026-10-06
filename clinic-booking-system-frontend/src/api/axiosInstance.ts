import axios, { AxiosError } from 'axios'
import { storage } from '@/utils/storage'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

// AuthContext (Stage F1) will register a handler so a 401 triggers a clean logout.
let unauthorizedHandler: (() => void) | null = null

export const setUnauthorizedHandler = (handler: (() => void) | null): void => {
  unauthorizedHandler = handler
}

axiosInstance.interceptors.request.use((config) => {
  const token = storage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status
    const url = error.config?.url ?? ''
    const isAuthCall = url.includes('/auth/login') || url.includes('/auth/register')

    // A 401 on login/register just means wrong credentials, not an expired session.
    if (status === 401 && !isAuthCall) {
      storage.clear()
      if (unauthorizedHandler) {
        unauthorizedHandler()
      } else if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export default axiosInstance