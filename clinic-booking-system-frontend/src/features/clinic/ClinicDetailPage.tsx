import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { clinicApi } from '@/api/clinicApi'
import { doctorApi } from '@/api/doctorApi'
import { Alert, Button, Card, EmptyState, Modal, Pagination, Spinner } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import DoctorCard from '@/features/doctor/DoctorCard'
import { PATHS } from '@/routes/paths'
import type { PagedResponse } from '@/types/api'
import { ROLES } from '@/types/auth'
import type { Clinic } from '@/types/clinic'
import type { Doctor } from '@/types/doctor'
import { getErrorMessage, getErrorStatus } from '@/utils/errors'
import { formatDate } from '@/utils/format'
import ClinicStatusBadge from './ClinicStatusBadge'

const DOCTORS_PAGE_SIZE = 6

function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{children}</dd>
    </div>
  )
}

function ClinicDetail({ idParam }: { idParam: string | undefined }) {
  const clinicId = Number(idParam)
  const validId = Number.isInteger(clinicId) && clinicId > 0
  const { hasRole } = useAuth()
  const isAdmin = hasRole(ROLES.SUPER_ADMIN, ROLES.CLINIC_ADMIN, ROLES.CLINIC_OWNER)
  const isSuperAdmin = hasRole(ROLES.SUPER_ADMIN)

  const [clinic, setClinic] = useState<Clinic | null>(null)
  const [loading, setLoading] = useState(validId)
  const [error, setError] = useState('')
  const [notFound, setNotFound] = useState(!validId)
  const [reloadKey, setReloadKey] = useState(0)

  const [doctorPage, setDoctorPage] = useState(0)
  const [doctors, setDoctors] = useState<PagedResponse<Doctor> | null>(null)
  const [doctorsLoading, setDoctorsLoading] = useState(validId)
  const [doctorsError, setDoctorsError] = useState('')
  const [doctorsReloadKey, setDoctorsReloadKey] = useState(0)

  const [confirmOpen, setConfirmOpen] = useState(false)
  const [deactivating, setDeactivating] = useState(false)
  const [activating, setActivating] = useState(false)
  const [actionError, setActionError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!validId) return
    let cancelled = false
    setLoading(true)
    clinicApi
      .getById(clinicId)
      .then((data) => {
        if (cancelled) return
        setClinic(data)
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
  }, [clinicId, validId, reloadKey])

  useEffect(() => {
    if (!validId) return
    let cancelled = false
    setDoctorsLoading(true)
    doctorApi
      .getByClinic(clinicId, { page: doctorPage, size: DOCTORS_PAGE_SIZE })
      .then((data) => {
        if (cancelled) return
        setDoctors(data)
        setDoctorsError('')
      })
      .catch((err) => {
        if (!cancelled) setDoctorsError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setDoctorsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [clinicId, validId, doctorPage, doctorsReloadKey])

  const handleDeactivate = async () => {
    setDeactivating(true)
    setActionError('')
    try {
      await clinicApi.deactivate(clinicId)
      setClinic((prev) => (prev ? { ...prev, status: 'INACTIVE' } : prev))
      setConfirmOpen(false)
      setSuccess('Clinic deactivated successfully')
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setDeactivating(false)
    }
  }

  const handleActivate = async () => {
    setActivating(true)
    setActionError('')
    try {
      await clinicApi.activate(clinicId)
      setClinic((prev) => (prev ? { ...prev, status: 'ACTIVE' } : prev))
      setSuccess('Clinic activated successfully')
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setActivating(false)
    }
  }

  if (notFound) {
    return (
      <div className="mx-auto max-w-md py-16 text-center">
        <h1 className="text-2xl font-semibold text-gray-900">Clinic not found</h1>
        <p className="mt-2 text-sm text-gray-600">
          This clinic doesn&apos;t exist or the link is incorrect.
        </p>
        <div className="mt-6">
          <Link to={PATHS.clinics}>
            <Button>Back to clinics</Button>
          </Link>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="flex justify-center py-16">
        <Spinner className="h-8 w-8 text-primary-600" />
      </div>
    )
  }

  if (error || !clinic) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Alert variant="error" title="Could not load this clinic">
          {error || 'Something went wrong'}
        </Alert>
        <Button variant="secondary" onClick={() => setReloadKey((k) => k + 1)}>
          Try again
        </Button>
      </div>
    )
  }

  const addressLines = [clinic.addressLine1, clinic.addressLine2].filter(Boolean).join(', ')

  return (
    <div className="space-y-6">
      <Link to={PATHS.clinics} className="text-sm font-medium text-primary-600 hover:text-primary-700">
        &larr; All clinics
      </Link>

      {clinic.status === 'INACTIVE' && (
        <Alert variant="warning" title="This clinic is inactive">
          This clinic is currently not active.
        </Alert>
      )}

      {success && (
        <Alert variant="success" onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      <Card>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-2xl font-semibold text-gray-900">{clinic.name}</h1>
              <ClinicStatusBadge status={clinic.status} />
            </div>
            <p className="mt-1 text-sm text-gray-600">
              {clinic.city}, {clinic.state}
            </p>
          </div>

          {isAdmin && (
            <div className="flex flex-wrap gap-2">
              <Link to={PATHS.clinicEdit(clinic.id)}>
                <Button variant="secondary">Edit</Button>
              </Link>
              {isSuperAdmin && clinic.status === 'ACTIVE' && (
                <Button variant="danger" onClick={() => setConfirmOpen(true)}>
                  Deactivate
                </Button>
              )}
              {isSuperAdmin && clinic.status === 'INACTIVE' && (
                <Button onClick={handleActivate} loading={activating}>
                  Activate
                </Button>
              )}
            </div>
          )}
        </div>

        {actionError && !confirmOpen && (
          <Alert variant="error" className="mt-4">{actionError}</Alert>
        )}

        {clinic.description && (
          <p className="mt-4 text-sm text-gray-700">{clinic.description}</p>
        )}

        <dl className="mt-6 grid gap-4 sm:grid-cols-2">
          <Detail label="Address">
            {addressLines}
            <br />
            {clinic.city}, {clinic.state} {clinic.pincode}
          </Detail>
          <Detail label="Contact">
            <a href={`tel:${clinic.phone}`} className="text-primary-600 hover:text-primary-700">
              {clinic.phone}
            </a>
            {clinic.email && (
              <>
                <br />
                <a
                  href={`mailto:${clinic.email}`}
                  className="text-primary-600 hover:text-primary-700"
                >
                  {clinic.email}
                </a>
              </>
            )}
          </Detail>
          <Detail label="Registration number">{clinic.registrationNumber}</Detail>
          <Detail label="Listed since">{formatDate(clinic.createdAt)}</Detail>
        </dl>
      </Card>

      <section className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-lg font-semibold text-gray-900">Doctors at this clinic</h2>
                <Link
                    to={`${PATHS.doctors}?clinicId=${clinic.id}`}
                    className="text-sm font-medium text-primary-600 hover:text-primary-700"
                >
                    Search and filter &rarr;
                </Link>
        </div>
        {doctorsError && (
          <Alert variant="error" title="Could not load doctors">
            <p>{doctorsError}</p>
            <div className="mt-2">
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setDoctorsReloadKey((k) => k + 1)}
              >
                Try again
              </Button>
            </div>
          </Alert>
        )}

        {!doctors && doctorsLoading ? (
          <div className="flex justify-center py-8">
            <Spinner className="h-6 w-6 text-primary-600" />
          </div>
        ) : doctors && doctors.content.length === 0 ? (
          <EmptyState
            title="No doctors listed yet"
            description="This clinic has no active doctors at the moment."
          />
        ) : doctors ? (
          <>
            <div
              className={`grid gap-4 sm:grid-cols-2 lg:grid-cols-3 ${doctorsLoading ? 'opacity-60' : ''}`}
            >
              {doctors.content.map((doctor) => (
                <DoctorCard key={doctor.id} doctor={doctor} />
              ))}
            </div>
            <Pagination
              pageNumber={doctors.pageNumber}
              totalPages={doctors.totalPages}
              onPageChange={setDoctorPage}
            />
          </>
        ) : null}
      </section>

      <Modal
        open={confirmOpen}
        title="Deactivate clinic?"
        onClose={() => (deactivating ? undefined : setConfirmOpen(false))}
        footer={
          <>
            <Button variant="secondary" onClick={() => setConfirmOpen(false)} disabled={deactivating}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDeactivate} loading={deactivating}>
              Deactivate
            </Button>
          </>
        }
      >
        <p>
          {clinic.name} will be hidden from the public clinics list. Its page and doctors stay
          reachable by direct link.
        </p>
        {actionError && (
          <Alert variant="error" className="mt-3">
            {actionError}
          </Alert>
        )}
      </Modal>
    </div>
  )
}

export default function ClinicDetailPage() {
  const { id } = useParams()
  // key resets all state when the user moves between clinics
  return <ClinicDetail key={id} idParam={id} />
}
