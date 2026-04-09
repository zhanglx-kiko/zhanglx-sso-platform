import router from '@/router'
import { AUTH_EVENT_STORAGE_KEY } from '@/constants'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import { isLogoutInProgress, isWhiteListPath, logoutAndRedirect } from '@/utils/auth'

const SESSION_VALIDATION_INTERVAL_MS = 1500

let lastValidationAt = 0
let monitorRegistered = false
let validationPromise: Promise<void> | null = null

const parseAuthEvent = (
  value: string,
): {
  message?: string
  type?: string
} | null => {
  try {
    const parsed = JSON.parse(value)
    return typeof parsed === 'object' && parsed !== null ? parsed : null
  } catch {
    return null
  }
}

const shouldValidateCurrentRoute = (): boolean => {
  const currentPath = router.currentRoute.value.path
  return !isWhiteListPath(currentPath) && !isLogoutInProgress()
}

export const validateActiveSession = async (force = false): Promise<void> => {
  if (!shouldValidateCurrentRoute()) {
    return
  }

  const userStore = useUserStore()

  if (!userStore.hasToken() || !userStore.hasSession()) {
    await logoutAndRedirect({
      message: '登录信息已失效，请重新登录',
    })
    return
  }

  const now = Date.now()
  if (!force && validationPromise) {
    await validationPromise
    return
  }

  if (!force && now - lastValidationAt < SESSION_VALIDATION_INTERVAL_MS) {
    return
  }

  validationPromise = (async () => {
    try {
      await usePermissionStore().fetchPermissions()
    } finally {
      lastValidationAt = Date.now()
      validationPromise = null
    }
  })()

  await validationPromise
}

export const setupAuthSessionMonitor = (): void => {
  if (monitorRegistered || typeof window === 'undefined') {
    return
  }

  monitorRegistered = true

  window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
      void validateActiveSession(true)
    }
  })

  window.addEventListener('storage', (event) => {
    if (event.key !== AUTH_EVENT_STORAGE_KEY || !event.newValue) {
      return
    }

    if (!shouldValidateCurrentRoute()) {
      return
    }

    const authEvent = parseAuthEvent(event.newValue)
    if (authEvent?.type !== 'logout') {
      return
    }

    void logoutAndRedirect({
      broadcast: false,
      message:
        typeof authEvent.message === 'string' && authEvent.message.trim()
          ? authEvent.message
          : '登录状态已失效，请重新登录',
    })
  })
}
