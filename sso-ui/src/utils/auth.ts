import type { Router } from 'vue-router'
import {
  AUTH_EVENT_STORAGE_KEY,
  AUTH_EXPIRED_BIZ_CODES,
  AUTH_EXPIRED_KEYWORDS,
  HTTP_STATUS,
  ROUTE_WHITE_LIST,
} from '@/constants'
import { closeGlobalError, extractErrorMessage, openGlobalError } from '@/stores/globalError'
import { useMenuStore } from '@/stores/menu'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'

type RequestCanceller = () => void
type RouterResetHandler = () => void

export interface LogoutAndRedirectOptions {
  broadcast?: boolean
  message?: string
  redirect?: boolean | string
  replace?: boolean
  skipMessage?: boolean
}

export interface AuthFailureMatch {
  code?: number
  matched: boolean
  message: string
}

const DEFAULT_AUTH_EXPIRED_MESSAGE = '登录状态已失效，请重新登录'
const AUTH_EXPIRED_CODE_SET = new Set<number>(AUTH_EXPIRED_BIZ_CODES)

let appRouter: Router | null = null
let cancelPendingRequestsHandler: RequestCanceller | null = null
let logoutInProgress = false
let logoutPromise: Promise<void> | null = null
let resetRouterHandler: RouterResetHandler | null = null

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

const emitAuthEvent = (message: string): void => {
  try {
    localStorage.setItem(
      AUTH_EVENT_STORAGE_KEY,
      JSON.stringify({
        at: Date.now(),
        message,
        type: 'logout',
      }),
    )
  } catch {
    // Ignore storage sync failures.
  }
}

const resolveRedirect = (redirect?: boolean | string): string | undefined => {
  if (redirect === false) {
    return undefined
  }

  if (typeof redirect === 'string' && redirect.trim()) {
    return redirect.trim()
  }

  if (!appRouter) {
    return undefined
  }

  const { fullPath, path } = appRouter.currentRoute.value

  if (!fullPath || isWhiteListPath(path)) {
    return undefined
  }

  return fullPath
}

const clearAuthState = (): void => {
  cancelPendingRequestsHandler?.()
  closeGlobalError()
  resetRouterHandler?.()

  usePermissionStore().clearPermission()
  useMenuStore().clearMenu()
  useUserStore().clearAll()
}

const navigateToLogin = async (redirect?: string, replace = true): Promise<void> => {
  if (!appRouter) {
    return
  }

  const target = redirect
    ? {
        path: '/login',
        query: { redirect },
      }
    : {
        path: '/login',
      }

  try {
    if (replace) {
      await appRouter.replace(target)
    } else {
      await appRouter.push(target)
    }
  } catch {
    // Ignore duplicated navigation and guard interruptions.
  }
}

export const registerAuthRequestCanceller = (handler: RequestCanceller): void => {
  cancelPendingRequestsHandler = handler
}

export const registerAuthRouter = (
  router: Router,
  resetHandler: RouterResetHandler,
): void => {
  appRouter = router
  resetRouterHandler = resetHandler
}

export const isWhiteListPath = (path: string): boolean => {
  return ROUTE_WHITE_LIST.includes(path as (typeof ROUTE_WHITE_LIST)[number])
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

export const setLogoutInProgress = (value: boolean): void => {
  logoutInProgress = value
}

export const isLogoutInProgress = (): boolean => {
  return logoutInProgress
}

export const logoutAndRedirect = async (
  options: LogoutAndRedirectOptions = {},
): Promise<void> => {
  if (logoutPromise) {
    return logoutPromise
  }

  const message = options.message?.trim() || DEFAULT_AUTH_EXPIRED_MESSAGE
  const redirect = resolveRedirect(options.redirect)

  logoutPromise = (async () => {
    clearAuthState()

    if (options.broadcast !== false) {
      emitAuthEvent(message)
    }

    if (!options.skipMessage) {
      openGlobalError(message, {
        autoCloseMs: 3200,
      })
    }

    await navigateToLogin(redirect, options.replace !== false)
  })().finally(() => {
    setTimeout(() => {
      logoutPromise = null
    }, 400)
  })

  return logoutPromise
}
