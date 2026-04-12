import type { MemberInfo, MemberLoginVO } from '../types/api'

const TOKEN_NAME_KEY = 'memberTokenName'
const TOKEN_VALUE_KEY = 'memberTokenValue'
const MEMBER_INFO_KEY = 'memberInfo'

export function getStoredTokenName() {
  return (wx.getStorageSync(TOKEN_NAME_KEY) as string) || 'token'
}

export function getStoredTokenValue() {
  return (wx.getStorageSync(TOKEN_VALUE_KEY) as string) || ''
}

export function getStoredMemberInfo() {
  return (wx.getStorageSync(MEMBER_INFO_KEY) as MemberInfo) || null
}

export function persistLoginToken(loginInfo: MemberLoginVO | null) {
  if (!loginInfo?.tokenValue) {
    wx.removeStorageSync(TOKEN_NAME_KEY)
    wx.removeStorageSync(TOKEN_VALUE_KEY)
    return
  }
  wx.setStorageSync(TOKEN_NAME_KEY, loginInfo.tokenName || 'token')
  wx.setStorageSync(TOKEN_VALUE_KEY, loginInfo.tokenValue)
}

export function persistMemberInfo(memberInfo: MemberInfo | null) {
  if (!memberInfo) {
    wx.removeStorageSync(MEMBER_INFO_KEY)
    return
  }
  wx.setStorageSync(MEMBER_INFO_KEY, memberInfo)
}

export function clearStoredSession() {
  wx.removeStorageSync(TOKEN_NAME_KEY)
  wx.removeStorageSync(TOKEN_VALUE_KEY)
  wx.removeStorageSync(MEMBER_INFO_KEY)
}
