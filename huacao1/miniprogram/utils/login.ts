interface MemberLoginPromptOptions {
  message?: string
  replace?: boolean
}

export function promptMemberLogin(options: MemberLoginPromptOptions = {}) {
  const content = options.message || '请前往“我的”页完成微信登录后继续操作。'

  return new Promise<void>((resolve) => {
    wx.showModal({
      title: '请先登录',
      content,
      confirmText: '去登录',
      cancelText: '稍后再说',
      confirmColor: '#355339',
      success: (result) => {
        if (result.confirm) {
          if (options.replace) {
            wx.redirectTo({
              url: '/pages/mine/index',
            })
          } else {
            wx.navigateTo({
              url: '/pages/mine/index',
            })
          }
        }
        resolve()
      },
      fail: () => resolve(),
    })
  })
}
