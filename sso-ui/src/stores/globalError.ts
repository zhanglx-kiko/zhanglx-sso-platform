import { readonly, reactive } from 'vue'

const DEFAULT_AUTO_CLOSE_MS = 3000
const HANDLED_ERROR_FLAG = Symbol('handledByGlobalError')

export interface GlobalErrorOptions {
  autoCloseMs?: number
  fallbackMessage?: string
}

interface GlobalErrorState {
  autoCloseMs: number
  message: string
  visible: boolean
}

const state = reactive<GlobalErrorState>({
  autoCloseMs: DEFAULT_AUTO_CLOSE_MS,
  message: '',
  visible: false,
})

let closeTimer: ReturnType<typeof setTimeout> | null = null

const isRecord = (value: unknown): value is Record<PropertyKey, unknown> => {
  return typeof value === 'object' && value !== null
}

const toRecord = (value: unknown): Record<PropertyKey, unknown> | null => {
  if (isRecord(value)) return value

  if (typeof value === 'string') {
    const normalized = value.trim()
    if (!normalized.startsWith('{') || !normalized.endsWith('}')) {
      return null
    }

    try {
      const parsed = JSON.parse(normalized)
      return isRecord(parsed) ? parsed : null
    } catch {
      return null
    }
  }

  return null
}

const clearCloseTimer = () => {
  if (!closeTimer) return
  clearTimeout(closeTimer)
  closeTimer = null
}

const scheduleAutoClose = () => {
  clearCloseTimer()

  if (state.autoCloseMs <= 0) return

  closeTimer = setTimeout(() => {
    closeGlobalError()
  }, state.autoCloseMs)
}

export const globalErrorState = readonly(state)

export const closeGlobalError = () => {
  clearCloseTimer()
  state.visible = false
}

export const markGlobalErrorHandled = <T>(payload: T): T => {
  if (isRecord(payload)) {
    Reflect.set(payload, HANDLED_ERROR_FLAG, true)
  }
  return payload
}

export const hasHandledGlobalError = (payload: unknown): boolean => {
  return isRecord(payload) ? Boolean(Reflect.get(payload, HANDLED_ERROR_FLAG)) : false
}

export const extractErrorMessage = (
  payload: unknown,
  fallbackMessage = '系统繁忙，请稍后重试',
): string => {
  if (typeof payload === 'string') {
    return payload.trim() || fallbackMessage
  }

  if (isRecord(payload)) {
    const data = toRecord(payload.data)
    const response = toRecord(payload.response)
    const responseData = response ? toRecord(response.data) : null
    const nestedError = toRecord(payload.error)

    const candidates = [
      payload.msg,
      data?.msg,
      responseData?.msg,
      nestedError?.msg,
      payload.message,
      data?.message,
      responseData?.message,
      nestedError?.message,
    ]

    const matched = candidates.find((item) => typeof item === 'string' && item.trim())
    if (typeof matched === 'string') {
      return matched.trim()
    }
  }

  if (payload instanceof Error) {
    const directMessage = payload.message?.trim()
    if (directMessage) return directMessage
  }

  return fallbackMessage
}

export const openGlobalError = (message: string, options: GlobalErrorOptions = {}): string => {
  const nextMessage = message.trim()
  if (!nextMessage) return ''

  state.message = nextMessage
  state.autoCloseMs = options.autoCloseMs ?? DEFAULT_AUTO_CLOSE_MS
  state.visible = true
  scheduleAutoClose()

  return nextMessage
}

export const showGlobalError = (
  payload: unknown,
  options: GlobalErrorOptions = {},
): string => {
  if (hasHandledGlobalError(payload)) {
    return ''
  }

  const message = extractErrorMessage(payload, options.fallbackMessage)
  markGlobalErrorHandled(payload)

  return openGlobalError(message, options)
}

export const useGlobalErrorState = () => globalErrorState
