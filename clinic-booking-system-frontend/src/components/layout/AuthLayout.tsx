import { Link, Outlet } from 'react-router-dom'
import { PATHS } from '@/routes/paths'

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4 py-8">
      <Link to={PATHS.home} className="mb-6 text-2xl font-bold text-primary-600">
        MediBook
      </Link>
      <div className="w-full max-w-md rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <Outlet />
      </div>
    </div>
  )
}