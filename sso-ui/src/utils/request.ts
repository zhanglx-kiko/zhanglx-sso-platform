import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { useUserStore } from '@/stores/user'
import { BIZ_CODE, HTTP_STATUS, TOKEN_HEADER_KEY } from '@/constants'
import { extractErrorMessage, markGlobalErrorHandled, showGlobalError } from '@/stores/globalError'
import {
  isLogoutInProgress,
  logoutAndRedirect,
  registerAuthRequestCanceller,
  resolveAuthFailure,
} from '@/utils/auth'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

const pendingRequests = new Map<string, AbortController>()

const generateRequestKey = (config: InternalAxiosRequestConfig): string => {
  const { method, url } = config
  return `${method}-${url}`
}

const addPendingRequest = (config: InternalAxiosRequestConfig): void => {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const controller = pendingRequests.get(key)
    controller?.abort()
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pendingRequests.set(key, controller)
}

const removePendingRequest = (config: InternalAxiosRequestConfig): void => {
  const key = generateRequestKey(config)
  pendingRequests.delete(key)
}

const cancelAllPendingRequests = (): void => {
  pendingRequests.forEach((controller) => {
    controller.abort()
  })
  pendingRequests.clear()
}

registerAuthRequestCanceller(cancelAllPendingRequests)

let isRedirecting = false
let isLoggingOut = false

const isTokenExpiredError = (message: string): boolean => {
  return resolveAuthFailure({ msg: message }).matched
}

const isLogoutRequest = (config?: InternalAxiosRequestConfig): boolean => {
  if (!config) return false
  const url = config.url || ''
  return url.includes('/logout') || url.includes('/apis/v1/auth/s/logout') || url.includes('/apis/v1/auth/m/logout')
}

const isBusinessResponse = (
  payload: unknown,
): payload is {
  code?: number | string
  data?: unknown
  msg?: string
} => {
  return typeof payload === 'object' && payload !== null && ('code' in payload || 'msg' in payload)
}

const createHandledError = (payload: unknown, fallbackMessage = '系统错误'): Error => {
  return markGlobalErrorHandled(new Error(extractErrorMessage(payload, fallbackMessage)))
}

const handleTokenExpired = (message = '登录状态已失效，请重新登录', skipMessage = false): void => {
  if (isRedirecting) return
  isRedirecting = true

  void logoutAndRedirect({
    message,
    skipMessage,
  }).finally(() => {
    setTimeout(() => {
      isRedirecting = false
    }, 1000)
  })
}

export const setLoggingOut = (value: boolean): void => {
  isLoggingOut = value
}

export const getLoggingOut = (): boolean => {
  return isLoggingOut
}

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    addPendingRequest(config)

    const userStore = useUserStore()
    if (userStore.token) {
      config.headers[TOKEN_HEADER_KEY] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    removePendingRequest(response.config as InternalAxiosRequestConfig)

    const res = response.data
    const config = response.config as InternalAxiosRequestConfig

    if (!isBusinessResponse(res)) {
      return res
    }

    if (Number(res.code) === BIZ_CODE.SUCCESS) {
      return res.data
    }

    if (isLogoutInProgress() && isLogoutRequest(config)) {
      return Promise.reject(new Error(res.msg || 'Error'))
    }

    const userStore = useUserStore()
    const authFailure = resolveAuthFailure(res)
    if (userStore.hasToken() && authFailure.matched) {
      const message = authFailure.message || '登录状态已失效，请重新登录'
      handleTokenExpired(message, isLogoutInProgress())
      return Promise.reject(createHandledError(message))
    }

    if (res.code === BIZ_CODE.SUCCESS) {
      return res.data
    }

    if (isLoggingOut && isLogoutRequest(config)) {
      return Promise.reject(new Error(res.msg || 'Error'))
    }

    if (res.code === BIZ_CODE.TOKEN_EXPIRED) {
      const message = extractErrorMessage(res, '登录已过期，请重新登录')
      handleTokenExpired(message, isLoggingOut())
      return Promise.reject(createHandledError(message))
    }

    if (res.code === BIZ_CODE.UNAUTHORIZED && isTokenExpiredError(res.msg || '')) {
      const message = extractErrorMessage(res, '登录状态已失效，请重新登录')
      handleTokenExpired(message, isLoggingOut())
      return Promise.reject(createHandledError(message))
    }

    if (isTokenExpiredError(res.msg || '')) {
      const message = extractErrorMessage(res, '登录状态已失效，请重新登录')
      handleTokenExpired(message, isLoggingOut())
      return Promise.reject(createHandledError(message))
    }

    showGlobalError(res, {
      fallbackMessage: '系统错误',
    })
    return Promise.reject(createHandledError(res, '系统错误'))
  },
  (error) => {
    removePendingRequest(error.config as InternalAxiosRequestConfig)

    if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
      return Promise.reject(markGlobalErrorHandled(error))
    }

    const config = error.config as InternalAxiosRequestConfig

    if (isLogoutInProgress() && isLogoutRequest(config)) {
      return Promise.reject(error)
    }

    const userStore = useUserStore()
    const authFailure = resolveAuthFailure(error)
    if (userStore.hasToken() && authFailure.matched) {
      handleTokenExpired(authFailure.message || '登录状态已失效，请重新登录', isLogoutInProgress())
      return Promise.reject(markGlobalErrorHandled(error))
    }

    if (isLoggingOut && isLogoutRequest(config)) {
      return Promise.reject(error)
    }

    const status = error.response?.status
    const errorMessage = extractErrorMessage(error, '网络异常，请稍后重试')

    if (status === HTTP_STATUS.UNAUTHORIZED && isTokenExpiredError(errorMessage)) {
      handleTokenExpired(errorMessage, isLoggingOut())
      return Promise.reject(markGlobalErrorHandled(error))
    }

    if (isTokenExpiredError(errorMessage)) {
      handleTokenExpired(errorMessage, isLoggingOut())
      return Promise.reject(markGlobalErrorHandled(error))
    }

    showGlobalError(error, {
      fallbackMessage: '网络异常，请稍后重试',
    })
    return Promise.reject(markGlobalErrorHandled(error))
  },
)

export const cancelRequest = (config: InternalAxiosRequestConfig): void => {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const controller = pendingRequests.get(key)
    controller?.abort()
    pendingRequests.delete(key)
  }
}

export const cancelAllRequests = cancelAllPendingRequests

export default service
