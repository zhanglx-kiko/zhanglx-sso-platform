package com.zhanglx.sso.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用登录态探针返回对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSessionStatusVO {

    /**
     * 是否存在任一已登录态。
     */
    private Boolean loggedIn;

    /**
     * 后台系统登录态是否有效。
     */
    private Boolean systemLoggedIn;

    /**
     * C 端会员登录态是否有效。
     */
    private Boolean memberLoggedIn;
}
