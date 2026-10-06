import { Link } from 'react-router-dom'
import { PATHS } from '@/routes/paths'

export default function HomePage() {
  return (
    <section className="py-12 text-center">
      <h1 className="text-4xl font-bold text-gray-900">Book clinic appointments, online.</h1>
      <p className="mx-auto mt-4 max-w-xl text-gray-600">
        Find doctors across clinics, pick an available slot, and keep your medical records in one place.
      </p>
      <div className="mt-8 flex justify-center gap-3">
        <Link
          to={PATHS.doctors}
          className="rounded-lg bg-primary-600 px-6 py-3 font-medium text-white hover:bg-primary-700"
        >
          Find a doctor
        </Link>
        <Link
          to={PATHS.register}
          className="rounded-lg border border-gray-300 bg-white px-6 py-3 font-medium text-gray-700 hover:bg-gray-50"
        >
          Create account
        </Link>
      </div>
    </section>
  )
}