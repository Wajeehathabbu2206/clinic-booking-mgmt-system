import { Link } from 'react-router-dom'
import { Badge, Card } from '@/components/ui'
import { PATHS } from '@/routes/paths'
import type { Doctor } from '@/types/doctor'
import { formatCurrency, getInitials } from '@/utils/format'

interface DoctorCardProps {
  doctor: Doctor
  showClinic?: boolean
}

export default function DoctorCard({ doctor, showClinic = false }: DoctorCardProps) {
  const hasReviews = (doctor.totalReviews ?? 0) > 0

  return (
    <Link
      to={PATHS.doctorDetail(doctor.id)}
      className="block rounded-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
    >
      <Card className="h-full transition-shadow hover:shadow-md">
        <div className="flex items-start gap-3">
          <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-primary-600 text-sm font-semibold text-white">
            {getInitials(doctor.fullName)}
          </span>
          <div className="min-w-0">
            <h3 className="truncate text-base font-semibold text-gray-900">{doctor.fullName}</h3>
            <div className="mt-1">
              <Badge variant="blue">{doctor.specialization}</Badge>
              {!doctor.isActive && <Badge variant="red" className="ml-2">Inactive</Badge>}
            </div>
          </div>
        </div>

        {doctor.qualification && (
          <p className="mt-3 text-sm text-gray-600">{doctor.qualification}</p>
        )}
        {showClinic && <p className="mt-1 text-sm text-gray-500">{doctor.clinicName}</p>}

        <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-sm">
          <span className="text-gray-600">
            {doctor.experienceYears !== null && doctor.experienceYears !== undefined
              ? `${doctor.experienceYears} yrs experience`
              : 'Experience not listed'}
          </span>
          <span className="font-medium text-gray-900">{formatCurrency(doctor.consultationFee)}</span>
        </div>

        <p className="mt-2 text-xs text-gray-500">
          {hasReviews
            ? `★ ${Number(doctor.averageRating ?? 0).toFixed(1)} (${doctor.totalReviews} reviews)`
            : 'No reviews yet'}
        </p>
      </Card>
    </Link>
  )
}
