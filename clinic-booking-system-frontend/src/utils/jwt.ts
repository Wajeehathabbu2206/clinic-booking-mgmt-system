interface JwtPayload {
  sub?: string
  role?: string
  iat?: number
  exp?: number
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const part = token.split('.')[1]
    if (!part) return null

    const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const payload: unknown = JSON.parse(atob(padded))
    if (!isRecord(payload)) return null

    return {
      ...(typeof payload.sub === 'string' && { sub: payload.sub }),
      ...(typeof payload.role === 'string' && { role: payload.role }),
      ...(typeof payload.iat === 'number' && Number.isFinite(payload.iat) && { iat: payload.iat }),
      ...(typeof payload.exp === 'number' && Number.isFinite(payload.exp) && { exp: payload.exp }),
    }
  } catch {
    return null
  }
}

/** Token expiry as a millisecond timestamp, or null if it can't be read. */
export function getTokenExpiryMs(token: string): number | null {
  const expiry = decodeJwtPayload(token)?.exp
  return expiry === undefined ? null : expiry * 1000
}

export function isTokenExpired(token: string): boolean {
  const expiry = getTokenExpiryMs(token)
  return expiry === null || expiry <= Date.now()
}
