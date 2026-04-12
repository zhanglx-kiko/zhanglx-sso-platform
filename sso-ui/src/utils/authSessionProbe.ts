import { STORAGE_KEYS, TOKEN_HEADER_KEY } from '@/constants'
import type { AuthSessionStatusVO } from '@/types/auth'

interface ApiResult<T> {
  code?: number
  data?: T
  msg?: string
}

let pendingProbe: Promise<AuthSessionStatusVO | null> | null = null

const buildHeaders = (): HeadersInit => {
  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  return token ? { [TOKEN_HEADER_KEY]: token } : {}
}

const isSessionStatus = (payload: unknown): payload is AuthSessionStatusVO => {
  if (typeof payload !== 'object' || payload === null) {
    return false
  }

  const record = payload as Record<string, unknown>
  return (
    typeof record.loggedIn === 'boolean'
    && typeof record.systemLoggedIn === 'boolean'
    && typeof record.memberLoggedIn === 'boolean'
  )
}

export const probeAuthSessionStatus = async (): Promise<AuthSessionStatusVO | null> => {
  if (pendingProbe) {
    return pendingProbe
  }

  pendingProbe = fetch('/api/apis/v1/auth/isLogin', {
    method: 'GET',
    credentials: 'include',
    headers: buildHeaders(),
  })
    .then(async (response) => {
      if (!response.ok) {
        return null
      }

      const payload = (await response.json().catch(() => null)) as ApiResult<unknown> | null
      if (!payload || Number(payload.code) !== 200 || !isSessionStatus(payload.data)) {
        return null
      }

      return payload.data
    })
    .catch(() => null)
    .finally(() => {
      pendingProbe = null
    })

  return pendingProbe
}
