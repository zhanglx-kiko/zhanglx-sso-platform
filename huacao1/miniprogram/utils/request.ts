import type { ApiEnvelope } from '../types/api'
import { API_BASE_URL, REQUEST_TIMEOUT } from './config'
import { clearStoredSession, getStoredTokenName, getStoredTokenValue } from './session'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
type RequestPayload = WechatMiniprogram.IAnyObject | string | ArrayBuffer
type WxRequestMethod = WechatMiniprogram.RequestOption['method']

interface RequestOptions<TData extends RequestPayload> {
  url: string
  method?: HttpMethod
  data?: TData
  auth?: boolean
  header?: Record<string, string>
  showLoading?: boolean
  loadingText?: string
}

interface UploadOptions {
  url: string
  filePath: string
  name?: string
  formData?: Record<string, string>
  auth?: boolean
}

function resolveUrl(url: string) {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  return `${API_BASE_URL}${url}`
}

function normalizeEnvelope<T>(payload: unknown): ApiEnvelope<T> {
  if (typeof payload === 'string') {
    return JSON.parse(payload) as ApiEnvelope<T>
  }
  return payload as ApiEnvelope<T>
}

function hideLoading(showLoading?: boolean) {
  if (showLoading) {
    wx.hideLoading()
  }
}

function rejectWithSessionError(message: string) {
  clearStoredSession()
  return new Error(message)
}

function buildHeaders(extraHeader?: Record<string, string>) {
  const tokenName = getStoredTokenName()
  const tokenValue = getStoredTokenValue()
  return {
    'content-type': 'application/json',
    ...(tokenValue ? { [tokenName]: tokenValue } : {}),
    ...extraHeader,
  }
}

export function request<T, TData extends RequestPayload = WechatMiniprogram.IAnyObject>({
  url,
  method = 'GET',
  data,
  auth = false,
  header,
  showLoading = false,
  loadingText = '加载中',
}: RequestOptions<TData>) {
  if (auth && !getStoredTokenValue()) {
    return Promise.reject(new Error('请先完成微信登录'))
  }

  if (showLoading) {
    wx.showLoading({
      title: loadingText,
      mask: true,
    })
  }

  return new Promise<T>((resolve, reject) => {
    wx.request({
      url: resolveUrl(url),
      method: method as unknown as WxRequestMethod,
      data: data as RequestPayload | undefined,
      timeout: REQUEST_TIMEOUT,
      header: buildHeaders(header),
      success: (response) => {
        const envelope = normalizeEnvelope<T>(response.data)
        const success = response.statusCode >= 200 && response.statusCode < 300 && envelope.code === 200
        if (success) {
          resolve(envelope.data)
          return
        }
        if (response.statusCode === 401 || response.statusCode === 403 || envelope.code === 401 || envelope.code === 403) {
          reject(rejectWithSessionError(envelope.msg || '登录状态已失效'))
          return
        }
        reject(new Error(envelope.msg || '请求失败，请稍后重试'))
      },
      fail: () => {
        reject(new Error('网络开小差了，请检查网关服务是否已启动'))
      },
      complete: () => {
        hideLoading(showLoading)
      },
    })
  })
}

export function uploadFile<T>({
  url,
  filePath,
  name = 'files',
  formData,
  auth = true,
}: UploadOptions) {
  if (auth && !getStoredTokenValue()) {
    return Promise.reject(new Error('请先完成微信登录'))
  }

  return new Promise<T>((resolve, reject) => {
    wx.uploadFile({
      url: resolveUrl(url),
      filePath,
      name,
      formData,
      timeout: REQUEST_TIMEOUT,
      header: buildHeaders({
        'content-type': 'multipart/form-data',
      }),
      success: (response) => {
        const envelope = normalizeEnvelope<T>(response.data)
        if (response.statusCode >= 200 && response.statusCode < 300 && envelope.code === 200) {
          resolve(envelope.data)
          return
        }
        if (response.statusCode === 401 || response.statusCode === 403 || envelope.code === 401 || envelope.code === 403) {
          reject(rejectWithSessionError(envelope.msg || '登录状态已失效'))
          return
        }
        reject(new Error(envelope.msg || '上传失败，请稍后重试'))
      },
      fail: () => {
        reject(new Error('上传失败，请检查网络或服务状态'))
      },
    })
  })
}
