import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { clinicApi } from '@/api/clinicApi'
import { doctorApi } from '@/api/doctorApi'
import { Alert, Button, Card, Input, Select, Textarea } from '@/components/ui'
import type { SelectOption } from '@/components/ui'
import { PATHS } from '@/routes/paths'
import type { DoctorCreateRequest } from '@/types/doctor'
import type { DoctorUserOption } from '@/types/doctor'
import { getErrorMessage } from '@/utils/errors'

export default function DoctorFormPage() {
  const navigate = useNavigate()
  const [clinicOptions, setClinicOptions] = useState<SelectOption[]>([])
  const [clinicsLoading, setClinicsLoading] = useState(true)
  const [clinicLoadError, setClinicLoadError] = useState('')
  const [doctorOptions, setDoctorOptions] = useState<DoctorUserOption[]>([])
  const [doctorsLoading, setDoctorsLoading] = useState(false)
  const [doctorsLoadError, setDoctorsLoadError] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [doctorUserId, setDoctorUserId] = useState('')
  const [clinicId, setClinicId] = useState('')
  const [specialization, setSpecialization] = useState('')
  const [qualification, setQualification] = useState('')
  const [registrationNumber, setRegistrationNumber] = useState('')
  const [experienceYears, setExperienceYears] = useState('')
  const [consultationFee, setConsultationFee] = useState('')
  const [bio, setBio] = useState('')

  useEffect(() => {
    let cancelled = false
    clinicApi.list({ page: 0, size: 100 })
      .then((result) => {
        if (cancelled) return
        setClinicOptions(result.content.map((clinic) => ({
          value: String(clinic.id),
          label: `${clinic.name} (${clinic.city})${clinic.status === 'INACTIVE' ? ' — inactive' : ''}`,
        })))
        setClinicLoadError('')
      })
      .catch((err) => {
        if (!cancelled) setClinicLoadError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setClinicsLoading(false)
      })
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    setDoctorUserId('')
    setDoctorOptions([])
    setDoctorsLoadError('')
    if (!clinicId) return

    let cancelled = false
    setDoctorsLoading(true)
    doctorApi.getEligibleUsers(Number(clinicId))
      .then((users) => {
        if (!cancelled) setDoctorOptions(users)
      })
      .catch((err) => {
        if (!cancelled) setDoctorsLoadError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setDoctorsLoading(false)
      })
    return () => { cancelled = true }
  }, [clinicId])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    const payload: DoctorCreateRequest = {
      userId: Number(doctorUserId),
      clinicId: Number(clinicId),
      specialization: specialization.trim(),
      qualification: qualification.trim() || undefined,
      registrationNumber: registrationNumber.trim() || undefined,
      experienceYears: experienceYears === '' ? undefined : Number(experienceYears),
      consultationFee: consultationFee === '' ? undefined : Number(consultationFee),
      bio: bio.trim() || undefined,
    }

    try {
      const doctor = await doctorApi.promotePatient(payload)
      navigate(PATHS.doctorDetail(doctor.id), { replace: true })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <Link to={PATHS.doctors} className="text-sm font-medium text-primary-600 hover:text-primary-700">
          &larr; Find doctors
        </Link>
        <h1 className="mt-3 text-2xl font-semibold text-gray-900">Add doctor profile</h1>
        <p className="mt-1 text-sm text-gray-600">
          Choose a registered patient to promote to doctor and assign to a clinic you manage.
        </p>
      </div>

      {clinicLoadError && (
        <Alert variant="error" title="Could not load clinics">
          {clinicLoadError}
          <div className="mt-2"><Button size="sm" variant="secondary" onClick={() => window.location.reload()}>Reload page</Button></div>
        </Alert>
      )}
      {error && <Alert variant="error" title="Could not create doctor profile">{error}</Alert>}

      <Card>
        <form className="space-y-5" onSubmit={handleSubmit}>
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Clinic" required placeholder={clinicsLoading ? 'Loading clinics…' : 'Select a clinic'}
              options={clinicOptions} value={clinicId} disabled={clinicsLoading || clinicOptions.length === 0}
              onChange={(event) => setClinicId(event.target.value)} />
            <Select label="Doctor account" required
              placeholder={!clinicId ? 'Select a clinic first' : doctorsLoading ? 'Loading doctor accounts…' : 'Select a doctor account'}
              options={doctorOptions.map((user) => ({ value: String(user.userId), label: `${user.fullName} — ${user.email} (${user.role === 'PATIENT' ? 'patient to promote' : 'doctor account'})` }))}
              value={doctorUserId} disabled={!clinicId || doctorsLoading || doctorOptions.length === 0}
              onChange={(event) => setDoctorUserId(event.target.value)} />
            <Input label="Specialization" required maxLength={100} value={specialization}
              onChange={(event) => setSpecialization(event.target.value)} />
            <Input label="Qualification" maxLength={255} value={qualification}
              onChange={(event) => setQualification(event.target.value)} />
            <Input label="Registration number" maxLength={50} value={registrationNumber}
              onChange={(event) => setRegistrationNumber(event.target.value)} />
            <Input label="Experience (years)" type="number" min={0} step={1} value={experienceYears}
              onChange={(event) => setExperienceYears(event.target.value)} />
            <Input label="Consultation fee" type="number" min={0} step="0.01" value={consultationFee}
              onChange={(event) => setConsultationFee(event.target.value)} />
          </div>
          {doctorsLoadError && <Alert variant="error" title="Could not load doctor accounts">{doctorsLoadError}</Alert>}
          {clinicId && !doctorsLoading && !doctorsLoadError && doctorOptions.length === 0 && (
            <p className="text-sm text-gray-600">No unassigned doctor accounts are available.</p>
          )}
          <Textarea label="Bio" maxLength={2000} value={bio}
            onChange={(event) => setBio(event.target.value)} />
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => navigate(-1)} disabled={saving}>Cancel</Button>
            <Button type="submit" loading={saving} disabled={clinicsLoading || clinicOptions.length === 0 || doctorsLoading || !doctorUserId}>Create profile</Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
