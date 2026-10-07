import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '@/api/authApi'
import { setUnauthorizedHandler } from '@/api/axiosInstance'
import { ROLES, type AuthResponse, type AuthUser, type LoginRequest, type RegisterRequest, type Role } from '@/types/auth'
import { getTokenExpiryMs, isTokenExpired } from '@/utils/jwt'
import { storage } from '@/utils/storage'

interface Session {
  token: string | null
  user: AuthUser | null
}

export interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (payload: LoginRequest) => Promise<AuthUser>
  register: (payload: RegisterRequest) => Promise<AuthUser>
  logout: () => void
  hasRole: (...roles: Role[]) => boolean
}

const EMPTY_SESSION: Session = { token: null, user: null }

function isAuthUser(value: unknown): value is AuthUser {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) return false
  const user = value as Record<string, unknown>
  return (
    typeof user.id === 'number' &&
    Number.isFinite(user.id) &&
    typeof user.email === 'string' &&
    typeof user.fullName === 'string' &&
    typeof user.role === 'string' &&
    Object.values(ROLES).some((role) => role === user.role)
  )
}

/** Read the saved session; discard it if it is missing, broken or expired. */
function loadSession(): Session {
  const token = storage.getToken()
  const storedUser: unknown = storage.getUser<unknown>()
  if (!token || !isAuthUser(storedUser) || isTokenExpired(token)) {
    storage.clear()
    return EMPTY_SESSION
  }
  return { token, user: storedUser }
}

function toUser(res: AuthResponse): AuthUser {
  return { id: res.id, email: res.email, fullName: res.fullName, role: res.role }
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session>(loadSession)

  const startSession = useCallback((res: AuthResponse): AuthUser => {
    const user = toUser(res)
    storage.setToken(res.token)
    storage.setUser(user)
    setSession({ token: res.token, user })
    return user
  }, [])

  const login = useCallback(
    async (payload: LoginRequest) => startSession(await authApi.login(payload)),
    [startSession],
  )

  const register = useCallback(
    async (payload: RegisterRequest) => startSession(await authApi.register(payload)),
    [startSession],
  )

  const logout = useCallback(() => {
    storage.clear()
    setSession(EMPTY_SESSION)
  }, [])

  const hasRole = useCallback(
    (...roles: Role[]) => !!session.user && roles.includes(session.user.role),
    [session.user],
  )

  // A 401 from any protected API call (axios interceptor already cleared storage).
  useEffect(() => {
    setUnauthorizedHandler(() => setSession(EMPTY_SESSION))
    return () => setUnauthorizedHandler(null)
  }, [])

  // Auto-logout the moment the JWT expires.
  useEffect(() => {
    if (!session.token) return
    const token = session.token
    const expiry = getTokenExpiryMs(token)
    if (expiry === null) return
    const id = window.setTimeout(
      () => {
        if (isTokenExpired(token)) logout()
      },
      Math.max(expiry - Date.now(), 0),
    )
    return () => window.clearTimeout(id)
  }, [session.token, logout])

  // Keep multiple tabs in sync (login/logout in one tab updates the others).
  useEffect(() => {
    const onStorage = () => setSession(loadSession())
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session.user,
      token: session.token,
      isAuthenticated: !!session.token && !!session.user,
      login,
      register,
      logout,
      hasRole,
    }),
    [session, login, register, logout, hasRole],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
