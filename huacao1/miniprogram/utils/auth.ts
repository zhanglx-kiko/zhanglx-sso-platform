import type { MemberInfo, MemberLoginVO, MemberSession } from '../types/api'
import { request } from './request'
import { clearStoredSession, getStoredMemberInfo, getStoredTokenValue, persistLoginToken, persistMemberInfo } from './session'

function getWechatLoginCode() {
  return new Promise<string>((resolve, reject) => {
    wx.login({
      success: (result) => {
        if (result.code) {
          resolve(result.code)
          return
        }
        reject(new Error('未获取到微信登录凭证'))
      },
      fail: () => {
        reject(new Error('微信登录失败，请稍后重试'))
      },
    })
  })
}

export async function syncCurrentMember() {
  if (!getStoredTokenValue()) {
    return getStoredMemberInfo()
  }
  const memberInfo = await request<MemberInfo>({
    url: '/apis/v1/auth/m/users/current',
    auth: true,
  })
  persistMemberInfo(memberInfo)
  return memberInfo
}

export async function ensureMemberSession(forceRefresh = false): Promise<MemberSession | null> {
  if (!forceRefresh && getStoredTokenValue()) {
    try {
      const memberInfo = await syncCurrentMember()
      return {
        login: null,
        memberInfo,
      }
    } catch (error) {
      clearStoredSession()
    }
  }

  const code = await getWechatLoginCode()
  const loginInfo = await request<MemberLoginVO>({
    url: `/apis/v1/auth/m/wechat/login?code=${encodeURIComponent(code)}`,
    method: 'POST',
  })
  persistLoginToken(loginInfo)

  const memberInfo = await syncCurrentMember()
  return {
    login: loginInfo,
    memberInfo,
  }
}
