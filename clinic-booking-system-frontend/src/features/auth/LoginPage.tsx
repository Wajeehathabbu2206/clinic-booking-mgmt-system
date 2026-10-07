import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { Button, Input } from '@/components/ui'
import { useAuth } from '@/context/useAuth'
import { PATHS } from '@/routes/paths'
import { getErrorMessage, getFieldErrors } from '@/utils/errors'

interface LocationState {
  from?: { pathname: string }
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const location = useLocation()
  const redirectTo = (location.state as LocationState | null)?.from?.pathname ?? PATHS.dashboard

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Already logged in (or just logged in successfully) -> leave this page.
  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />
  }

  const validate = (): Record<string, string> => {
    const next: Record<string, string> = {}
    if (!email.trim()) next.email = 'Email is required'
    else if (!EMAIL_REGEX.test(email.trim())) next.email = 'Enter a valid email address'
    if (!password) next.password = 'Password is required'
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
      await login({ email: email.trim(), password })
      // AuthContext updates -> the isAuthenticated check above redirects.
    } catch (err) {
      setErrors(getFieldErrors(err))
      setFormError(getErrorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-gray-900">Welcome back</h1>
      <p className="mt-1 text-sm text-gray-600">Log in to manage your appointments.</p>

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
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
        />

        <div>
          <Input
            label="Password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            placeholder="Your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
          />
          <label className="mt-2 flex items-center gap-2 text-xs text-gray-600">
            <input
              type="checkbox"
              checked={showPassword}
              onChange={(e) => setShowPassword(e.target.checked)}
            />
            Show password
          </label>
        </div>

        <Button type="submit" fullWidth loading={submitting}>
          Log in
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        New to MediBook?{' '}
        <Link to={PATHS.register} className="font-medium text-primary-600 hover:text-primary-700">
          Create an account
        </Link>
      </p>
    </div>
  )
}