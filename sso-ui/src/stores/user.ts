import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginVO } from '@/types/auth'
import { STORAGE_KEYS } from '@/constants'

const getStoredUserInfo = (): LoginVO | null => {
  const rawValue = localStorage.getItem(STORAGE_KEYS.USER_INFO)
  if (!rawValue) {
    return null
  }

  try {
    const parsedValue = JSON.parse(rawValue)
    return typeof parsedValue === 'object' && parsedValue !== null ? (parsedValue as LoginVO) : null
  } catch {
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(STORAGE_KEYS.TOKEN) || '')
  const userInfo = ref<LoginVO | null>(getStoredUserInfo())

  const setLoginState = (data: LoginVO): void => {
    token.value = data.tokenValue
    userInfo.value = data
    localStorage.setItem(STORAGE_KEYS.TOKEN, data.tokenValue)
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(data))
  }

  const logout = (): void => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
  }

  const clearAll = (): void => {
    logout()
    sessionStorage.clear()
  }

  const hasToken = (): boolean => {
    return !!token.value
  }

  const hasSession = (): boolean => {
    return Boolean(token.value && userInfo.value?.username)
  }

  const getCurrentUserId = (): string => {
    return userInfo.value?.id || ''
  }

  const isCurrentUser = (payload?: { id?: string | null; username?: string | null }): boolean => {
    if (!payload) {
      return false
    }

    if (payload.id && payload.id === userInfo.value?.id) {
      return true
    }

    return Boolean(payload.username && payload.username === userInfo.value?.username)
  }

  return {
    token,
    userInfo,
    setLoginState,
    logout,
    clearAll,
    hasToken,
    hasSession,
    getCurrentUserId,
    isCurrentUser,
  }
})
