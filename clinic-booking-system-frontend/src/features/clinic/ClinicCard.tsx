import { Link } from 'react-router-dom'
import { Card } from '@/components/ui'
import { PATHS } from '@/routes/paths'
import type { Clinic } from '@/types/clinic'
import ClinicStatusBadge from './ClinicStatusBadge'

export default function ClinicCard({ clinic }: { clinic: Clinic }) {
  return (
    <Link
      to={PATHS.clinicDetail(clinic.id)}
      className="block rounded-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
    >
      <Card className="h-full transition-shadow hover:shadow-md">
        <div className="flex items-start justify-between gap-3">
          <h3 className="text-base font-semibold text-gray-900">{clinic.name}</h3>
          {clinic.status === 'INACTIVE' && <ClinicStatusBadge status={clinic.status} />}
        </div>
        <p className="mt-1 text-sm text-gray-600">
          {clinic.city}, {clinic.state}
        </p>
        {clinic.description && (
          <p className="mt-3 line-clamp-2 text-sm text-gray-500">{clinic.description}</p>
        )}
        <p className="mt-3 text-sm text-gray-700">{clinic.phone}</p>
      </Card>
    </Link>
  )
}