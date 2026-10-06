import { Link } from 'react-router-dom'
import { PATHS } from '@/routes/paths'

export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4 text-center">
      <p className="text-6xl font-bold text-primary-600">404</p>
      <h1 className="mt-2 text-xl font-semibold text-gray-900">Page not found</h1>
      <p className="mt-1 text-sm text-gray-500">The page you are looking for does not exist.</p>
      <Link
        to={PATHS.home}
        className="mt-6 rounded-lg bg-primary-600 px-5 py-2 text-sm font-medium text-white hover:bg-primary-700"
      >
        Back to home
      </Link>
    </div>
  )
}