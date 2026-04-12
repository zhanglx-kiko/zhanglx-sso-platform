package com.zhanglx.sso.gateway.config;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.exception.SameTokenInvalidException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关 Sa-Token 鉴权配置。
 */
@Slf4j
@Configuration
public class SaTokenConfigure {

    /**
     * 会员端登录逻辑。
     */
    private static final StpLogic MEMBER_STP_LOGIC = new StpLogic("member");

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截全部请求，再按白名单放行公共接口。
                .addInclude("/**")
                // 开放地址：登录、忘记密码、会员注册、接口文档等无需鉴权。
                .addExclude("/favicon.ico",
                        "/apis/v1/auth/s/login",
                        "/apis/v1/auth/s/forgot-password/verification-code/send",
                        "/apis/v1/auth/s/forgot-password/verification-code/verify",
                        "/apis/v1/auth/s/forgot-password",
                        "/apis/v1/auth/m/login",
                        "/apis/v1/auth/m/register",
                        "/apis/v1/auth/m/forgot-password",
                        "/apis/v1/auth/m/verification-code/send",
                        "/apis/v1/auth/m/wechat/login",
                        "/apis/v1/auth/isLogin",
                        "/oauth2/*",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/doc.html")
                // 鉴权逻辑：会员自助链路走 member 体系，会员后台管理链路明确归入系统后台鉴权。
                .setAuth(obj -> {
                    SaRouter.match("/apis/v1/**/internal/**", r -> SaSameUtil.checkCurrentRequestToken());
                    SaRouter.match("/apis/v1/auth/s/members/**", r -> StpUtil.checkLogin());
                    SaRouter.match("/apis/v1/auth/m/**", r -> MEMBER_STP_LOGIC.checkLogin());
                    SaRouter.match("/apis/v1/user/m/**", r -> MEMBER_STP_LOGIC.checkLogin());
                    SaRouter.match("/**")
                            .notMatch("/apis/v1/**/internal/**", "/apis/v1/auth/m/**", "/apis/v1/user/m/**")
                            .check(r -> StpUtil.checkLogin());
                })
                // 统一把 Sa-Token 异常转成稳定结果，并把关键细节打进日志。
                .setError(this::buildAuthErrorResult);
    }

    /**
     * 统一网关鉴权异常返回，同时补全控制台日志，便于联调和排障。
     */
    private SaResult buildAuthErrorResult(Throwable throwable) {
        if (throwable instanceof NotLoginException exception) {
            log.warn("网关未登录或会话已失效，type={}, loginType={}, message={}",
                    exception.getType(),
                    exception.getLoginType(),
                    exception.getMessage(),
                    exception);
            return SaResult.code(SaResult.CODE_NOT_LOGIN).setMsg(resolveNotLoginMessage(exception));
        }

        if (throwable instanceof DisableServiceException exception) {
            log.warn("网关账号或令牌已被禁用，loginType={}, loginId={}, service={}, message={}",
                    exception.getLoginType(),
                    exception.getLoginId(),
                    exception.getService(),
                    exception.getMessage(),
                    exception);
            return SaResult.code(SaResult.CODE_NOT_LOGIN).setMsg("账号已被禁用，请联系管理员");
        }

        if (throwable instanceof NotPermissionException exception) {
            log.warn("网关权限校验失败，permission={}", exception.getPermission(), exception);
            return SaResult.code(SaResult.CODE_NOT_PERMISSION).setMsg("没有访问权限");
        }

        if (throwable instanceof NotRoleException exception) {
            log.warn("网关角色校验失败，role={}", exception.getRole(), exception);
            return SaResult.code(SaResult.CODE_NOT_PERMISSION).setMsg("没有访问权限");
        }

        if (throwable instanceof SameTokenInvalidException exception) {
            log.warn("网关同端互斥令牌校验失败: {}", exception.getMessage(), exception);
            return SaResult.code(SaResult.CODE_NOT_LOGIN).setMsg("登录状态校验失败，请重新登录");
        }

        if (throwable instanceof SaTokenException exception) {
            log.warn("网关 Sa-Token 异常: {}", exception.getMessage(), exception);
            return SaResult.code(SaResult.CODE_NOT_LOGIN).setMsg("登录状态已失效，请重新登录");
        }

        log.error("网关鉴权发生未知异常", throwable);
        return SaResult.error("系统繁忙，请稍后再试");
    }

    /**
     * 统一整理未登录场景返回文案，避免把原始 token 细节直接暴露给前端。
     */
    private String resolveNotLoginMessage(NotLoginException exception) {
        if (NotLoginException.NOT_TOKEN.equals(exception.getType())) {
            return "未登录，请先登录";
        }
        return "登录状态已失效，请重新登录";
    }

}
