/// <reference path="./types/index.d.ts" />

interface IAppOption extends WechatMiniprogram.IAnyObject {
  globalData: {
    apiBaseUrl: string,
    tokenName?: string,
    tokenValue?: string,
    memberInfo?: WechatMiniprogram.IAnyObject,
    ready: boolean,
  }
  loginPromise?: Promise<any> | null,
  ensureMemberSession: (forceRefresh?: boolean) => Promise<any>,
  refreshCurrentMember: () => Promise<any>,
  clearMemberSession: () => void,
  applySession: (session: any) => void,
}
