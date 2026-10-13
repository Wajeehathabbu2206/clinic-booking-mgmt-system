import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { clinicApi } from '@/api/clinicApi'
import { doctorApi } from '@/api/doctorApi'
import { Alert, Button, EmptyState, Input, Pagination, Select, Spinner } from '@/components/ui'
import type { SelectOption } from '@/components/ui'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import type { PagedResponse } from '@/types/api'
import type { Doctor } from '@/types/doctor'
import { getErrorMessage } from '@/utils/errors'
import DoctorCard from './DoctorCard'

const PAGE_SIZE = 9

// Suggestions only: specialization is free text on the backend.
const SPECIALIZATION_SUGGESTIONS = [
  'Cardiology',
  'Dentistry',
  'Dermatology',
  'ENT',
  'General Physician',
  'Gynecology',
  'Neurology',
  'Orthopedics',
  'Pediatrics',
  'Psychiatry',
]

// Backend sorts on Doctor entity fields, so only these are valid sortBy values.
const SORT_OPTIONS: SelectOption[] = [
  { value: 'experienceYears:desc', label: 'Most experienced' },
  { value: 'averageRating:desc', label: 'Top rated' },
  { value: 'consultationFee:asc', label: 'Fee: low to high' },
  { value: 'consultationFee:desc', label: 'Fee: high to low' },
]

type Parsed = { ok: true; value?: number } | { ok: false; message: string }

function parseNonNegative(raw: string, integer: boolean): Parsed {
  const text = raw.trim()
  if (!text) return { ok: true }
  const num = Number(text)
  if (!Number.isFinite(num) || num < 0) return { ok: false, message: 'Enter 0 or a higher number' }
  if (integer && !Number.isInteger(num)) return { ok: false, message: 'Enter a whole number' }
  return { ok: true, value: num }
}

function initialClinicId(raw: string | null): string {
  return raw && /^\d+$/.test(raw) ? raw : ''
}

export default function DoctorListPage() {
  const [searchParams] = useSearchParams()

  const [nameInput, setNameInput] = useState('')
  const [specInput, setSpecInput] = useState('')
  const [clinicId, setClinicId] = useState(() => initialClinicId(searchParams.get('clinicId')))
  const [minExpInput, setMinExpInput] = useState('')
  const [maxFeeInput, setMaxFeeInput] = useState('')
  const [sort, setSort] = useState(SORT_OPTIONS[0].value)
  const [page, setPage] = useState(0)
  const [reloadKey, setReloadKey] = useState(0)

  const [clinicOptions, setClinicOptions] = useState<SelectOption[]>([])
  const [data, setData] = useState<PagedResponse<Doctor> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const name = useDebouncedValue(nameInput.trim())
  const specialization = useDebouncedValue(specInput.trim())
  const minExp = useDebouncedValue(minExpInput)
  const maxFee = useDebouncedValue(maxFeeInput)

  // Field errors come from what the user typed, so they appear instantly.
  const minExpCheck = parseNonNegative(minExpInput, true)
  const maxFeeCheck = parseNonNegative(maxFeeInput, false)

  // Clinic dropdown (public endpoint; guests only receive active clinics)
  useEffect(() => {
    let cancelled = false
    clinicApi
      .list({ page: 0, size: 100 })
      .then((res) => {
        if (cancelled) return
        setClinicOptions(
          res.content.map((c) => ({
            value: String(c.id),
            label: c.status === 'INACTIVE' ? `${c.name} (inactive)` : c.name,
          })),
        )
      })
      .catch(() => {
        // Filter just stays empty; doctor search still works without it.
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    const minExpParsed = parseNonNegative(minExp, true)
    const maxFeeParsed = parseNonNegative(maxFee, false)
    // Don't call the API with invalid numbers; the field error explains why.
    if (!minExpParsed.ok || !maxFeeParsed.ok) return

    const [sortBy, sortDir] = sort.split(':') as [string, 'asc' | 'desc']

    let cancelled = false
    setLoading(true)
    doctorApi
      .search({
        name: name || undefined,
        specialization: specialization || undefined,
        clinicId: clinicId ? Number(clinicId) : undefined,
        minExperience: minExpParsed.value,
        maxFee: maxFeeParsed.value,
        page,
        size: PAGE_SIZE,
        sortBy,
        sortDir,
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
  }, [name, specialization, clinicId, minExp, maxFee, sort, page, reloadKey])

  // A clinicId from a link may not be in the dropdown (e.g. an inactive clinic for a guest).
  const selectOptions =
    clinicId && !clinicOptions.some((o) => o.value === clinicId)
      ? [...clinicOptions, { value: clinicId, label: `Clinic #${clinicId}` }]
      : clinicOptions

  const hasFilters = !!(nameInput || specInput || clinicId || minExpInput || maxFeeInput)

  const clearFilters = () => {
    setNameInput('')
    setSpecInput('')
    setClinicId('')
    setMinExpInput('')
    setMaxFeeInput('')
    setPage(0)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Find Doctors</h1>
        <p className="mt-1 text-sm text-gray-600">
          Search by name or specialization, and narrow down by clinic, experience or fee.
        </p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <Input
          label="Doctor name"
          placeholder="e.g. Priya"
          value={nameInput}
          onChange={(e) => {
            setNameInput(e.target.value)
            setPage(0)
          }}
        />
        <div>
          <Input
            label="Specialization"
            placeholder="e.g. Cardiology"
            list="specialization-suggestions"
            maxLength={100}
            value={specInput}
            onChange={(e) => {
              setSpecInput(e.target.value)
              setPage(0)
            }}
          />
          <datalist id="specialization-suggestions">
            {SPECIALIZATION_SUGGESTIONS.map((s) => (
              <option key={s} value={s} />
            ))}
          </datalist>
        </div>
        <Select
          label="Clinic"
          placeholder="All clinics"
          options={selectOptions}
          value={clinicId}
          onChange={(e) => {
            setClinicId(e.target.value)
            setPage(0)
          }}
        />
        <Input
          label="Minimum experience (years)"
          type="number"
          inputMode="numeric"
          min={0}
          step={1}
          placeholder="e.g. 5"
          value={minExpInput}
          onChange={(e) => {
            setMinExpInput(e.target.value)
            setPage(0)
          }}
          error={minExpCheck.ok ? undefined : minExpCheck.message}
        />
        <Input
          label="Maximum fee (₹)"
          type="number"
          inputMode="decimal"
          min={0}
          placeholder="e.g. 500"
          value={maxFeeInput}
          onChange={(e) => {
            setMaxFeeInput(e.target.value)
            setPage(0)
          }}
          error={maxFeeCheck.ok ? undefined : maxFeeCheck.message}
        />
        <Select
          label="Sort by"
          options={SORT_OPTIONS}
          value={sort}
          onChange={(e) => {
            setSort(e.target.value)
            setPage(0)
          }}
        />
      </div>

      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-600" aria-live="polite">
          {data ? `${data.totalElements} ${data.totalElements === 1 ? 'doctor' : 'doctors'} found` : ' '}
        </p>
        <Button variant="secondary" size="sm" onClick={clearFilters} disabled={!hasFilters}>
          Clear filters
        </Button>
      </div>

      {error && (
        <Alert variant="error" title="Could not load doctors">
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
          title="No doctors found"
          description={
            hasFilters
              ? 'Try removing a filter or searching a different name or specialization.'
              : 'There are no doctors to show yet.'
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
            {data.content.map((doctor) => (
              <DoctorCard key={doctor.id} doctor={doctor} showClinic />
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