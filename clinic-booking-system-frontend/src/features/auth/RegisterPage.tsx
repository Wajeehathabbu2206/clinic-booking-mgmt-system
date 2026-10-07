import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { Button, Input } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import { PATHS } from '@/routes/paths'
import { getErrorMessage, getFieldErrors } from '@/utils/errors'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_REGEX = /^[0-9+\-\s]{7,15}$/

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Registration also logs the user in, so this redirects right after success.
  if (isAuthenticated) {
    return <Navigate to={PATHS.dashboard} replace />
  }

  const validate = (): Record<string, string> => {
    const next: Record<string, string> = {}
    if (!fullName.trim()) next.fullName = 'Full name is required'
    if (!email.trim()) next.email = 'Email is required'
    else if (!EMAIL_REGEX.test(email.trim())) next.email = 'Enter a valid email address'
    if (phoneNumber.trim() && !PHONE_REGEX.test(phoneNumber.trim())) {
      next.phoneNumber = 'Enter a valid phone number'
    }
    if (!password) next.password = 'Password is required'
    else if (password.length < 8) next.password = 'Password must be at least 8 characters'
    if (!confirmPassword) next.confirmPassword = 'Please confirm your password'
    else if (confirmPassword !== password) next.confirmPassword = 'Passwords do not match'
    return next
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setFormError('')

    const clientErrors = validate()
    setErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return

    setSubmitting(true)
    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
        phoneNumber: phoneNumber.trim() || undefined,
      })
      // AuthContext updates -> the isAuthenticated check above redirects.
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-gray-900">Create your account</h1>
      <p className="mt-1 text-sm text-gray-600">Book clinic appointments and keep your records in one place.</p>

      <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-4">
        {formError && (
          <div
            role="alert"
            className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700"
          >
            {formError}
          </div>
        )}

        <Input
          label="Full name"
          autoComplete="name"
          placeholder="Your full name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          error={errors.fullName}
        />

        <Input
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
        />

        <Input
          label="Phone number (optional)"
          type="tel"
          autoComplete="tel"
          placeholder="+91 98765 43210"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          error={errors.phoneNumber}
        />

        <Input
          label="Password"
          type={showPassword ? 'text' : 'password'}
          autoComplete="new-password"
          placeholder="At least 8 characters"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={errors.password}
        />

        <div>
          <Input
            label="Confirm password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Re-enter your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            error={errors.confirmPassword}
          />
          <label className="mt-2 flex items-center gap-2 text-xs text-gray-600">
            <input
              type="checkbox"
              checked={showPassword}
              onChange={(e) => setShowPassword(e.target.checked)}
            />
            Show passwords
          </label>
        </div>

        <Button type="submit" fullWidth loading={submitting}>
          Create account
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        Already have an account?{' '}
        <Link to={PATHS.login} className="font-medium text-primary-600 hover:text-primary-700">
          Log in
        </Link>
      </p>
    </div>
  )
}