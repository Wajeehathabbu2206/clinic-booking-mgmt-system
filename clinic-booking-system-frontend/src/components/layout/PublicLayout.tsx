import { Link, NavLink, Outlet } from 'react-router-dom'
import { PATHS } from '@/routes/paths'
import { cn } from '@/utils/cn'

export default function PublicLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to={PATHS.home} className="text-xl font-bold text-primary-600">
            MediBook
          </Link>

          <nav className="flex items-center gap-2 text-sm">
            <NavLink
              to={PATHS.doctors}
              className={({ isActive }) =>
                cn(
                  'rounded-lg px-3 py-2 font-medium',
                  isActive ? 'text-primary-700' : 'text-gray-600 hover:text-gray-900',
                )
              }
            >
              Find Doctors
            </NavLink>
            <Link
              to={PATHS.login}
              className="rounded-lg px-3 py-2 font-medium text-gray-700 hover:bg-gray-100"
            >
              Log in
            </Link>
            <Link
              to={PATHS.register}
              className="rounded-lg bg-primary-600 px-4 py-2 font-medium text-white hover:bg-primary-700"
            >
              Sign up
            </Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t border-gray-200 bg-white py-4 text-center text-xs text-gray-500">
        &copy; {new Date().getFullYear()} MediBook. All rights reserved.
      </footer>
    </div>
  )
}