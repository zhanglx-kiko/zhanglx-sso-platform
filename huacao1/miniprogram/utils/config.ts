const DEVTOOLS_API_BASE_URL = 'http://127.0.0.1:21800'
const DEVICE_API_BASE_URL = 'http://192.168.11.220:21800'
const STORAGE_API_BASE_URL_KEY = 'plantApiBaseUrl'

function resolveDefaultBaseUrl() {
  try {
    const systemInfo = wx.getSystemInfoSync()
    return systemInfo.platform === 'devtools' ? DEVTOOLS_API_BASE_URL : DEVICE_API_BASE_URL
  } catch (error) {
    return DEVTOOLS_API_BASE_URL
  }
}

function resolveBaseUrl() {
  try {
    const storedBaseUrl = wx.getStorageSync(STORAGE_API_BASE_URL_KEY) as string
    if (storedBaseUrl) {
      return storedBaseUrl
    }
    return resolveDefaultBaseUrl()
  } catch (error) {
    return resolveDefaultBaseUrl()
  }
}

export const API_BASE_URL = resolveBaseUrl()
export const REQUEST_TIMEOUT = 15000
export const MAX_UPLOAD_IMAGE_COUNT = 9
export const COMMON_UNITS = ['盆', '株', '棵', '斤', '袋']
