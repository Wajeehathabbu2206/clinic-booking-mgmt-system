import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { doctorApi } from '@/api/doctorApi'
import { Alert, Badge, Button, Card, Spinner } from '@/components/ui'
import { PATHS } from '@/routes/paths'
import type { Doctor } from '@/types/doctor'
import { formatCurrency, formatDate, getInitials } from '@/utils/format'
import { getErrorMessage, getErrorStatus } from '@/utils/errors'

function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{children}</dd>
    </div>
  )
}

export default function DoctorDetailPage() {
  const { id: idParam } = useParams()
  const doctorId = Number(idParam)
  const validId = Number.isInteger(doctorId) && doctorId > 0
  const [doctor, setDoctor] = useState<Doctor | null>(null)
  const [loading, setLoading] = useState(validId)
  const [notFound, setNotFound] = useState(!validId)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (!validId) return
    let cancelled = false
    setLoading(true)
    doctorApi
      .getById(doctorId)
      .then((result) => {
        if (cancelled) return
        setDoctor(result)
        setNotFound(false)
        setError('')
      })
      .catch((err) => {
        if (cancelled) return
        if (getErrorStatus(err) === 404) setNotFound(true)
        else setError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [doctorId, validId, reloadKey])

  if (notFound) {
    return (
      <div className="mx-auto max-w-md py-16 text-center">
        <h1 className="text-2xl font-semibold text-gray-900">Doctor not found</h1>
        <p className="mt-2 text-sm text-gray-600">This doctor profile does not exist or the link is incorrect.</p>
        <div className="mt-6"><Link to={PATHS.doctors}><Button>Back to doctors</Button></Link></div>
      </div>
    )
  }

  if (loading) {
    return <div className="flex justify-center py-16"><Spinner className="h-8 w-8 text-primary-600" /></div>
  }

  if (error || !doctor) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Alert variant="error" title="Could not load this doctor">{error || 'Something went wrong'}</Alert>
        <Button variant="secondary" onClick={() => setReloadKey((key) => key + 1)}>Try again</Button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <Link to={PATHS.doctors} className="text-sm font-medium text-primary-600 hover:text-primary-700">
        &larr; Find doctors
      </Link>

      <Card>
        <div className="flex flex-col gap-5 sm:flex-row sm:items-start">
          <span className="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-primary-600 text-2xl font-semibold text-white">
            {getInitials(doctor.fullName)}
          </span>
          <div className="min-w-0 flex-1">
            <h1 className="text-2xl font-semibold text-gray-900">{doctor.fullName}</h1>
            <div className="mt-2 flex flex-wrap gap-2">
              <Badge variant="blue">{doctor.specialization}</Badge>
              {doctor.isActive && <Badge variant="green">Active</Badge>}
            </div>
            {doctor.qualification && <p className="mt-3 text-gray-700">{doctor.qualification}</p>}
            <p className="mt-2 text-sm text-gray-600">
              {(doctor.totalReviews ?? 0) > 0
                ? `★ ${Number(doctor.averageRating ?? 0).toFixed(1)} · ${doctor.totalReviews} reviews`
                : 'No reviews yet'}
            </p>
          </div>
        </div>

        <dl className="mt-8 grid gap-x-8 gap-y-6 sm:grid-cols-2">
          <Detail label="Clinic">
            <Link to={PATHS.clinicDetail(doctor.clinicId)} className="font-medium text-primary-600 hover:text-primary-700">
              {doctor.clinicName}
            </Link>
          </Detail>
          <Detail label="Experience">
            {doctor.experienceYears == null ? 'Not listed' : `${doctor.experienceYears} years`}
          </Detail>
          <Detail label="Consultation fee">{formatCurrency(doctor.consultationFee)}</Detail>
          <Detail label="Registration number">{doctor.registrationNumber || 'Not listed'}</Detail>
          <Detail label="Email"><a className="text-primary-600 hover:text-primary-700" href={`mailto:${doctor.email}`}>{doctor.email}</a></Detail>
          <Detail label="Phone">
            {doctor.phone ? <a className="text-primary-600 hover:text-primary-700" href={`tel:${doctor.phone}`}>{doctor.phone}</a> : 'Not listed'}
          </Detail>
          <Detail label="Profile listed">{formatDate(doctor.createdAt)}</Detail>
        </dl>

        {doctor.bio && (
          <section className="mt-8 border-t border-gray-200 pt-6">
            <h2 className="text-base font-semibold text-gray-900">About</h2>
            <p className="mt-2 whitespace-pre-line text-sm leading-6 text-gray-700">{doctor.bio}</p>
          </section>
        )}
      </Card>
    </div>
  )
}
