import { AUTH_EXPIRED_BIZ_CODES, AUTH_EXPIRED_KEYWORDS, HTTP_STATUS } from '@/constants'
import { extractErrorMessage } from '@/stores/globalError'

export interface AuthFailureMatch {
  code?: number
  matched: boolean
  message: string
}

export const DEFAULT_AUTH_EXPIRED_MESSAGE = '登录状态已失效，请重新登录'

const FORBIDDEN_STATUS = 403
const AUTH_EXPIRED_CODE_SET = new Set<number>(AUTH_EXPIRED_BIZ_CODES)

const isRecord = (value: unknown): value is Record<PropertyKey, unknown> => {
  return typeof value === 'object' && value !== null
}

const toComparableCode = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string') {
    const normalized = value.trim()
    if (!normalized) return null

    const parsed = Number(normalized)
    return Number.isFinite(parsed) ? parsed : null
  }

  return null
}

const toRecord = (value: unknown): Record<PropertyKey, unknown> | null => {
  return isRecord(value) ? value : null
}

const normalizeMessage = (value: string): string => {
  return value.toLowerCase().replace(/\s+/g, '')
}

const getMessageCandidates = (payload: unknown): string[] => {
  const messages = new Set<string>()
  const directMessage = extractErrorMessage(payload, '').trim()

  if (directMessage) {
    messages.add(directMessage)
  }

  if (!isRecord(payload)) {
    return Array.from(messages)
  }

  const data = toRecord(payload.data)
  const response = toRecord(payload.response)
  const responseData = response ? toRecord(response.data) : null
  const nestedError = toRecord(payload.error)

  const candidates = [
    payload.msg,
    payload.message,
    data?.msg,
    data?.message,
    responseData?.msg,
    responseData?.message,
    nestedError?.msg,
    nestedError?.message,
  ]

  candidates.forEach((candidate) => {
    if (typeof candidate === 'string' && candidate.trim()) {
      messages.add(candidate.trim())
    }
  })

  return Array.from(messages)
}

const getCodeCandidates = (payload: unknown): number[] => {
  if (!isRecord(payload)) {
    return []
  }

  const data = toRecord(payload.data)
  const response = toRecord(payload.response)
  const responseData = response ? toRecord(response.data) : null

  const rawCodes = [payload.code, data?.code, response?.status, responseData?.code]

  return rawCodes
    .map((item) => toComparableCode(item))
    .filter((item): item is number => item !== null)
}

export const isAuthFailureMessage = (message: string): boolean => {
  if (!message.trim()) {
    return false
  }

  const normalized = normalizeMessage(message)
  return AUTH_EXPIRED_KEYWORDS.some((keyword) =>
    normalized.includes(normalizeMessage(keyword)),
  )
}

export const resolveAuthFailure = (payload: unknown): AuthFailureMatch => {
  const messages = getMessageCandidates(payload)
  const matchedMessage = messages.find((message) => isAuthFailureMessage(message)) || ''
  const codes = getCodeCandidates(payload)

  if (codes.includes(HTTP_STATUS.UNAUTHORIZED)) {
    return {
      code: HTTP_STATUS.UNAUTHORIZED,
      matched: true,
      message: matchedMessage || DEFAULT_AUTH_EXPIRED_MESSAGE,
    }
  }

  const matchedCode = codes.find((code) => AUTH_EXPIRED_CODE_SET.has(code))
  if (typeof matchedCode === 'number') {
    return {
      code: matchedCode,
      matched: true,
      message: matchedMessage || DEFAULT_AUTH_EXPIRED_MESSAGE,
    }
  }

  if (matchedMessage) {
    return {
      matched: true,
      message: matchedMessage,
    }
  }

  return {
    matched: false,
    message: '',
  }
}

export const resolvePermissionBootstrapFailure = (payload: unknown): AuthFailureMatch => {
  const authFailure = resolveAuthFailure(payload)
  if (authFailure.matched) {
    return authFailure
  }

  const codes = getCodeCandidates(payload)
  if (codes.includes(FORBIDDEN_STATUS)) {
    return {
      code: FORBIDDEN_STATUS,
      matched: true,
      message: DEFAULT_AUTH_EXPIRED_MESSAGE,
    }
  }

  return {
    matched: false,
    message: '',
  }
}
