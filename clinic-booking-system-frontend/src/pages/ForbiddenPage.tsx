import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import { PATHS } from '@/routes/paths'

export default function ForbiddenPage() {
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  const handleRelogin = () => {
    navigate(PATHS.login, {
      replace: true,
      state: { notice: 'Please log in again to refresh your access.' },
    })
    logout()
  }

  return (
    <div className="mx-auto max-w-md py-16 text-center">
      <p className="text-5xl font-bold text-primary-600">403</p>
      <h1 className="mt-4 text-2xl font-semibold text-gray-900">Access denied</h1>
      <p className="mt-2 text-sm text-gray-600">
        You don&apos;t have permission to view this page with your current role.
      </p>

      <div className="mt-6 flex flex-wrap justify-center gap-3">
        <Link to={isAuthenticated ? PATHS.dashboard : PATHS.home}>
          <Button>{isAuthenticated ? 'Go to dashboard' : 'Go home'}</Button>
        </Link>
        {isAuthenticated && (
          <Button variant="secondary" onClick={handleRelogin}>
            Role recently changed? Log in again
          </Button>
        )}
      </div>
    </div>
  )
}