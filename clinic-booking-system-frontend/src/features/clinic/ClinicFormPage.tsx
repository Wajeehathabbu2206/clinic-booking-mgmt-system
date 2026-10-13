import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { clinicApi } from '@/api/clinicApi'
import { Alert, Button, Card, Input, Spinner, Textarea } from '@/components/ui'
import { PATHS } from '@/routes/paths'
import type { ClinicCreateRequest, ClinicUpdateRequest } from '@/types/clinic'
import { getErrorMessage } from '@/utils/errors'

type ClinicFormValues = ClinicCreateRequest

const emptyForm: ClinicFormValues = {
  name: '',
  registrationNumber: '',
  description: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  pincode: '',
  phone: '',
  email: '',
}

export default function ClinicFormPage() {
  const { id } = useParams()
  const editingId = id ? Number(id) : null
  const navigate = useNavigate()
  const [form, setForm] = useState<ClinicFormValues>(emptyForm)
  const [loading, setLoading] = useState(editingId !== null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (editingId === null) return
    if (!Number.isInteger(editingId) || editingId < 1) {
      setError('Invalid clinic ID')
      setLoading(false)
      return
    }

    let cancelled = false
    clinicApi
      .getById(editingId)
      .then((clinic) => {
        if (cancelled) return
        setForm({
          name: clinic.name,
          registrationNumber: clinic.registrationNumber,
          description: clinic.description ?? '',
          addressLine1: clinic.addressLine1,
          addressLine2: clinic.addressLine2 ?? '',
          city: clinic.city,
          state: clinic.state,
          pincode: clinic.pincode,
          phone: clinic.phone,
          email: clinic.email ?? '',
        })
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
  }, [editingId])

  const setField = (field: keyof ClinicFormValues, value: string) => {
    setForm((current) => ({ ...current, [field]: value }))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editingId === null) {
        const created = await clinicApi.create(form)
        navigate(PATHS.clinicDetail(created.id), { replace: true })
      } else {
        const payload: ClinicUpdateRequest = {
          name: form.name,
          description: form.description,
          addressLine1: form.addressLine1,
          addressLine2: form.addressLine2,
          city: form.city,
          state: form.state,
          pincode: form.pincode,
          phone: form.phone,
          email: form.email,
        }
        const updated = await clinicApi.update(editingId, payload)
        navigate(PATHS.clinicDetail(updated.id), { replace: true })
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <div className="flex justify-center py-16"><Spinner className="h-8 w-8 text-primary-600" /></div>
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <Link to={PATHS.clinics} className="text-sm font-medium text-primary-600 hover:text-primary-700">
          &larr; All clinics
        </Link>
        <h1 className="mt-3 text-2xl font-semibold text-gray-900">
          {editingId === null ? 'Add clinic' : 'Edit clinic'}
        </h1>
      </div>

      {error && <Alert variant="error" title="Could not save clinic">{error}</Alert>}

      <Card>
        <form className="space-y-5" onSubmit={handleSubmit}>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Clinic name" required minLength={3} maxLength={150} value={form.name}
              onChange={(event) => setField('name', event.target.value)} />
            <Input label="Registration number" required disabled={editingId !== null} value={form.registrationNumber}
              onChange={(event) => setField('registrationNumber', event.target.value)} />
            <Input label="Address line 1" required value={form.addressLine1}
              onChange={(event) => setField('addressLine1', event.target.value)} />
            <Input label="Address line 2" value={form.addressLine2}
              onChange={(event) => setField('addressLine2', event.target.value)} />
            <Input label="City" required value={form.city}
              onChange={(event) => setField('city', event.target.value)} />
            <Input label="State" required value={form.state}
              onChange={(event) => setField('state', event.target.value)} />
            <Input label="Pincode" required inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={form.pincode}
              onChange={(event) => setField('pincode', event.target.value)} />
            <Input label="Phone" required type="tel" inputMode="numeric" pattern="[6-9][0-9]{9}" maxLength={10} value={form.phone}
              onChange={(event) => setField('phone', event.target.value)} />
            <Input label="Email" type="email" value={form.email}
              onChange={(event) => setField('email', event.target.value)} />
          </div>
          <Textarea label="Description" value={form.description}
            onChange={(event) => setField('description', event.target.value)} />
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => navigate(-1)} disabled={saving}>Cancel</Button>
            <Button type="submit" loading={saving}>{editingId === null ? 'Create clinic' : 'Save changes'}</Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
