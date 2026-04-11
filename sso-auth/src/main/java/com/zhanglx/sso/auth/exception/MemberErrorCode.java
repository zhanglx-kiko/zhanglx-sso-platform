package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 会员域错误码定义。
 */
@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_ACCOUNT_EMPTY(ResultCode.BAD_REQUEST.getCode(), "member.account.empty"),
    MEMBER_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "member.info.not.found"),
    MEMBER_ACCOUNT_DISABLED(ResultCode.FORBIDDEN.getCode(), "member.account.disabled"),
    MEMBER_ACCOUNT_FROZEN(ResultCode.FORBIDDEN.getCode(), "member.account.frozen"),
    MEMBER_ACCOUNT_CANCELLED(ResultCode.FORBIDDEN.getCode(), "member.account.cancelled"),
    MEMBER_PHONE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.phone.cannot.be.blank"),
    MEMBER_VERIFICATION_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.verification.code.cannot.be.blank"),
    MEMBER_VERIFICATION_SCENE_INVALID(ResultCode.BAD_REQUEST.getCode(), "member.verification.scene.invalid"),
    MEMBER_PHONE_ALREADY_BOUND(ResultCode.CONFLICT.getCode(), "member.phone.already.bound"),
    MEMBER_PHONE_BIND_SAME_AS_CURRENT(ResultCode.BAD_REQUEST.getCode(), "member.phone.bind.same.as.current"),
    MEMBER_PHONE_NOT_CURRENT_BOUND(ResultCode.BAD_REQUEST.getCode(), "member.phone.not.current.bound"),
    MEMBER_PHONE_UPDATE_REQUIRES_VERIFICATION(ResultCode.BAD_REQUEST.getCode(), "member.phone.update.requires.verification"),
    MEMBER_MANAGE_REASON_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.manage.reason.cannot.be.blank"),
    MEMBER_MANAGE_ACTION_TYPE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.manage.action.type.cannot.be.blank"),
    MEMBER_TARGET_STATUS_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.target.status.cannot.be.blank"),
    MEMBER_STATUS_EXPIRE_TIME_INVALID(ResultCode.BAD_REQUEST.getCode(), "member.status.expire.time.invalid"),
    MEMBER_DISABLE_ONLY_NORMAL_OR_FROZEN(ResultCode.CONFLICT.getCode(), "member.disable.only.normal.or.frozen"),
    MEMBER_ENABLE_ONLY_DISABLED(ResultCode.CONFLICT.getCode(), "member.enable.only.disabled"),
    MEMBER_FREEZE_ONLY_NORMAL(ResultCode.CONFLICT.getCode(), "member.freeze.only.normal"),
    MEMBER_UNFREEZE_ONLY_FROZEN(ResultCode.CONFLICT.getCode(), "member.unfreeze.only.frozen");

    /**
     * 错误码。
     */
    private final Integer code;
    /**
     * 消息键。
     */
    private final String messageKey;

}