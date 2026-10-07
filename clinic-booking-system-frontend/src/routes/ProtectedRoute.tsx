import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/useAuth'
import type { Role } from '@/types/auth'
import { PATHS } from './paths'

interface ProtectedRouteProps {
  /** If given, the user's role must be one of these. */
  allowedRoles?: Role[]
}

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Remember where the user was going so login can send them back.
    return <Navigate to={PATHS.login} replace state={{ from: location }} />
  }

  if (allowedRoles && allowedRoles.length > 0 && !hasRole(...allowedRoles)) {
    return <Navigate to={PATHS.forbidden} replace />
  }

  return <Outlet />
}