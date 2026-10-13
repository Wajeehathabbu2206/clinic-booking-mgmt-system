import { useEffect, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { userApi } from '@/api/userApi'
import { Alert, Badge, Button, Card, Input, Select, Spinner } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import { PATHS } from '@/routes/paths'
import type { Gender, UserProfile, UserProfileUpdateRequest } from '@/types/user'
import { getErrorMessage, getFieldErrors } from '@/utils/errors'
import { formatDate, getInitials, todayLocal } from '@/utils/format'
import { ROLE_LABELS } from '@/utils/roles'

const PHONE_REGEX = /^[6-9]\d{9}$/
const PINCODE_REGEX = /^\d{6}$/
const URL_REGEX = /^https?:\/\/\S+$/i

const GENDER_OPTIONS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
]

interface FormState {
  fullName: string
  phoneNumber: string
  profilePictureUrl: string
  gender: string
  dateOfBirth: string
  addressLine1: string
  city: string
  state: string
  pincode: string
}

const EMPTY_FORM: FormState = {
  fullName: '',
  phoneNumber: '',
  profilePictureUrl: '',
  gender: '',
  dateOfBirth: '',
  addressLine1: '',
  city: '',
  state: '',
  pincode: '',
}

function toForm(p: UserProfile): FormState {
  return {
    fullName: p.fullName ?? '',
    phoneNumber: p.phoneNumber ?? '',
    profilePictureUrl: p.profilePictureUrl ?? '',
    gender: p.gender ?? '',
    dateOfBirth: p.dateOfBirth ?? '',
    addressLine1: p.addressLine1 ?? '',
    city: p.city ?? '',
    state: p.state ?? '',
    pincode: p.pincode ?? '',
  }
}

function validate(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {}
  const name = form.fullName.trim()
  if (!name) errors.fullName = 'Full name is required'
  else if (name.length < 2 || name.length > 100) errors.fullName = 'Full name must be 2–100 characters'

  if (form.phoneNumber.trim() && !PHONE_REGEX.test(form.phoneNumber.trim())) {
    errors.phoneNumber = 'Enter a valid 10-digit Indian mobile number'
  }
  if (form.pincode.trim() && !PINCODE_REGEX.test(form.pincode.trim())) {
    errors.pincode = 'Enter a valid 6-digit pincode'
  }
  if (form.dateOfBirth && form.dateOfBirth > todayLocal()) {
    errors.dateOfBirth = 'Date of birth cannot be in the future'
  }
  if (form.profilePictureUrl.trim() && !URL_REGEX.test(form.profilePictureUrl.trim())) {
    errors.profilePictureUrl = 'Enter a valid link starting with http:// or https://'
  }
  return errors
}

const opt = (value: string): string | undefined => value.trim() || undefined

function toPayload(form: FormState): UserProfileUpdateRequest {
  return {
    fullName: form.fullName.trim(),
    phoneNumber: opt(form.phoneNumber),
    profilePictureUrl: opt(form.profilePictureUrl),
    gender: (form.gender || undefined) as Gender | undefined,
    dateOfBirth: form.dateOfBirth || undefined,
    addressLine1: opt(form.addressLine1),
    city: opt(form.city),
    state: opt(form.state),
    pincode: opt(form.pincode),
  }
}

const genderLabel = (g?: string | null): string | null =>
  g ? g.charAt(0) + g.slice(1).toLowerCase() : null

function Avatar({ name, url }: { name: string; url?: string | null }) {
  const [failed, setFailed] = useState(false)
  if (url && !failed) {
    return (
      <img
        src={url}
        alt=""
        onError={() => setFailed(true)}
        className="h-16 w-16 rounded-full object-cover"
      />
    )
  }
  return (
    <span className="flex h-16 w-16 items-center justify-center rounded-full bg-primary-600 text-xl font-semibold text-white">
      {getInitials(name)}
    </span>
  )
}

function Detail({
  label,
  value,
  className,
}: {
  label: string
  value?: string | number | null
  className?: string
}) {
  const hasValue = value !== undefined && value !== null && value !== ''
  return (
    <div className={className}>
      <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{hasValue ? value : '—'}</dd>
    </div>
  )
}

export default function ProfilePage() {
  const { user, updateUser, logout } = useAuth()
  const navigate = useNavigate()

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let cancelled = false
    userApi
      .getMyProfile()
      .then((data) => {
        if (cancelled) return
        setProfile(data)
        setLoadError('')
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const retry = () => {
    setLoading(true)
    setLoadError('')
    setReloadKey((k) => k + 1)
  }

  const setField =
    (key: keyof FormState) => (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm((prev) => ({ ...prev, [key]: e.target.value }))

  const startEditing = () => {
    if (!profile) return
    setForm(toForm(profile))
    setErrors({})
    setFormError('')
    setSuccess('')
    setEditing(true)
  }

  const cancelEditing = () => {
    setEditing(false)
    setErrors({})
    setFormError('')
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError('')

    const clientErrors = validate(form)
    setErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return

    setSaving(true)
    try {
      const updated = await userApi.updateMyProfile(toPayload(form))
      setProfile(updated)
      updateUser({ fullName: updated.fullName })
      setEditing(false)
      setSuccess('Profile updated successfully')
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleRelogin = () => {
    navigate(PATHS.login, {
      replace: true,
      state: { notice: 'Please log in again to refresh your access.' },
    })
    logout()
  }

  if (loading) {
    return (
      <div className="flex justify-center py-16">
        <Spinner className="h-8 w-8 text-primary-600" />
      </div>
    )
  }

  if (loadError || !profile) {
    return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Alert variant="error" title="Could not load your profile">
          {loadError || 'Something went wrong'}
        </Alert>
        <Button variant="secondary" onClick={retry}>
          Try again
        </Button>
      </div>
    )
  }

  const roleChanged = !!user && profile.role !== user.role

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">My Profile</h1>
        {!editing && <Button onClick={startEditing}>Edit profile</Button>}
      </div>

      {success && (
        <Alert variant="success" onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {roleChanged && (
        <Alert variant="warning" title="Your role has changed">
          <p>
            Your account is now {ROLE_LABELS[profile.role]}. Log in again to update your access.
          </p>
          <div className="mt-2">
            <Button size="sm" variant="secondary" onClick={handleRelogin}>
              Log in again
            </Button>
          </div>
        </Alert>
      )}

      <Card>
        <div className="flex items-center gap-4">
          <Avatar key={profile.profilePictureUrl ?? ''} name={profile.fullName} url={profile.profilePictureUrl} />
          <div className="min-w-0">
            <p className="truncate text-lg font-semibold text-gray-900">{profile.fullName}</p>
            <p className="truncate text-sm text-gray-500">{profile.email}</p>
            <div className="mt-1">
              <Badge variant="blue">{ROLE_LABELS[profile.role]}</Badge>
            </div>
          </div>
        </div>
      </Card>

      {editing ? (
        <Card title="Edit profile">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {formError && (
              <Alert variant="error" onClose={() => setFormError('')}>
                {formError}
              </Alert>
            )}

            <div className="grid gap-4 sm:grid-cols-2">
              <Input
                label="Full name"
                autoComplete="name"
                value={form.fullName}
                onChange={setField('fullName')}
                error={errors.fullName}
              />
              <Input
                label="Email"
                value={profile.email}
                readOnly
                disabled
                hint="Email can't be changed"
              />
              <Input
                label="Phone number"
                type="tel"
                autoComplete="tel"
                placeholder="9876543210"
                value={form.phoneNumber}
                onChange={setField('phoneNumber')}
                error={errors.phoneNumber}
              />
              <Select
                label="Gender"
                placeholder="Select gender"
                options={GENDER_OPTIONS}
                value={form.gender}
                onChange={setField('gender')}
                error={errors.gender}
              />
              <Input
                label="Date of birth"
                type="date"
                max={todayLocal()}
                value={form.dateOfBirth}
                onChange={setField('dateOfBirth')}
                error={errors.dateOfBirth}
              />
              <Input
                label="Profile picture link"
                placeholder="https://..."
                value={form.profilePictureUrl}
                onChange={setField('profilePictureUrl')}
                error={errors.profilePictureUrl}
              />
            </div>

            <Input
              label="Address"
              autoComplete="street-address"
              value={form.addressLine1}
              onChange={setField('addressLine1')}
              error={errors.addressLine1}
            />

            <div className="grid gap-4 sm:grid-cols-3">
              <Input
                label="City"
                value={form.city}
                onChange={setField('city')}
                error={errors.city}
              />
              <Input
                label="State"
                value={form.state}
                onChange={setField('state')}
                error={errors.state}
              />
              <Input
                label="Pincode"
                inputMode="numeric"
                maxLength={6}
                value={form.pincode}
                onChange={setField('pincode')}
                error={errors.pincode}
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="secondary" onClick={cancelEditing} disabled={saving}>
                Cancel
              </Button>
              <Button type="submit" loading={saving}>
                Save changes
              </Button>
            </div>
          </form>
        </Card>
      ) : (
        <Card title="Personal details">
          <dl className="grid gap-4 sm:grid-cols-2">
            <Detail label="User ID" value={profile.id} />
            <Detail label="Phone" value={profile.phoneNumber} />
            <Detail label="Gender" value={genderLabel(profile.gender)} />
            <Detail label="Date of birth" value={formatDate(profile.dateOfBirth)} />
            <Detail label="Address" value={profile.addressLine1} className="sm:col-span-2" />
            <Detail label="City" value={profile.city} />
            <Detail label="State" value={profile.state} />
            <Detail label="Pincode" value={profile.pincode} />
            <Detail label="Member since" value={formatDate(profile.createdAt)} />
          </dl>
          <p className="mt-4 text-xs text-gray-500">
            Your User ID is what a clinic admin needs to add you as clinic staff.
          </p>
        </Card>
      )}
    </div>
  )
}