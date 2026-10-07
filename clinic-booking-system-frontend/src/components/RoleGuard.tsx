import type { ReactNode } from 'react'
import { useAuth } from '@/context/useAuth'
import type { Role } from '@/types/auth'

interface RoleGuardProps {
  roles: Role[]
  children: ReactNode
  fallback?: ReactNode
}

export default function RoleGuard({ roles, children, fallback = null }: RoleGuardProps) {
  const { hasRole } = useAuth()
  return <>{hasRole(...roles) ? children : fallback}</>
}