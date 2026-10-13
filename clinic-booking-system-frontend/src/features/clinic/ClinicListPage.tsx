import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { clinicApi } from '@/api/clinicApi'
import { Alert, Button, EmptyState, Input, Pagination, Select, Spinner } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import { PATHS } from '@/routes/paths'
import type { PagedResponse } from '@/types/api'
import { ROLES } from '@/types/auth'
import type { Clinic, ClinicStatus } from '@/types/clinic'
import { getErrorMessage } from '@/utils/errors'
import ClinicCard from './ClinicCard'

const PAGE_SIZE = 9

const STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
]

export default function ClinicListPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole(ROLES.SUPER_ADMIN, ROLES.CLINIC_ADMIN, ROLES.CLINIC_OWNER)

  const [searchInput, setSearchInput] = useState('')
  const [cityInput, setCityInput] = useState('')
  const [status, setStatus] = useState<ClinicStatus | ''>('')
  const [page, setPage] = useState(0)
  const [reloadKey, setReloadKey] = useState(0)

  const [data, setData] = useState<PagedResponse<Clinic> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const search = useDebouncedValue(searchInput.trim())
  const city = useDebouncedValue(cityInput.trim())

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    clinicApi
      .list({
        search: search || undefined,
        city: city || undefined,
        status: isAdmin && status ? status : undefined,
        page,
        size: PAGE_SIZE,
      })
      .then((res) => {
        if (cancelled) return
        setData(res)
        setError('')
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [search, city, status, page, isAdmin, reloadKey])

  const hasFilters = !!(searchInput || cityInput || status)

  const clearFilters = () => {
    setSearchInput('')
    setCityInput('')
    setStatus('')
    setPage(0)
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Clinics</h1>
          <p className="mt-1 text-sm text-gray-600">Find a clinic near you.</p>
        </div>
        {isAdmin && (
          <Link to={PATHS.clinicNew}>
            <Button>Add clinic</Button>
          </Link>
        )}
      </div>

      <div className={`grid gap-3 sm:grid-cols-2 ${isAdmin ? 'lg:grid-cols-4' : 'lg:grid-cols-3'}`}>
        <Input
          label="Search by name"
          placeholder="Clinic name"
          value={searchInput}
          onChange={(e) => {
            setSearchInput(e.target.value)
            setPage(0)
          }}
        />
        <Input
          label="City"
          placeholder="e.g. Chennai"
          value={cityInput}
          onChange={(e) => {
            setCityInput(e.target.value)
            setPage(0)
          }}
        />
        {isAdmin && (
          <Select
            label="Status"
            placeholder="All statuses"
            options={STATUS_OPTIONS}
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as ClinicStatus | '')
              setPage(0)
            }}
          />
        )}
        <div className="flex items-end">
          <Button variant="secondary" onClick={clearFilters} disabled={!hasFilters}>
            Clear filters
          </Button>
        </div>
      </div>

      {error && (
        <Alert variant="error" title="Could not load clinics">
          <p>{error}</p>
          <div className="mt-2">
            <Button size="sm" variant="secondary" onClick={() => setReloadKey((k) => k + 1)}>
              Try again
            </Button>
          </div>
        </Alert>
      )}

      {!data && loading ? (
        <div className="flex justify-center py-16">
          <Spinner className="h-8 w-8 text-primary-600" />
        </div>
      ) : data && data.content.length === 0 ? (
        <EmptyState
          title="No clinics found"
          description={
            hasFilters
              ? 'Try changing or clearing your filters.'
              : 'There are no clinics to show yet.'
          }
          action={
            hasFilters ? (
              <Button variant="secondary" onClick={clearFilters}>
                Clear filters
              </Button>
            ) : undefined
          }
        />
      ) : data ? (
        <>
          <div
            className={`grid gap-4 sm:grid-cols-2 lg:grid-cols-3 ${loading ? 'opacity-60' : ''}`}
          >
            {data.content.map((clinic) => (
              <ClinicCard key={clinic.id} clinic={clinic} />
            ))}
          </div>
          <Pagination
            pageNumber={data.pageNumber}
            totalPages={data.totalPages}
            onPageChange={setPage}
          />
        </>
      ) : null}
    </div>
  )
}
