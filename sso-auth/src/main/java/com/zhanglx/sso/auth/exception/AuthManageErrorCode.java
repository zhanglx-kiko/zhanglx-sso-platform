package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证后台管理域统一错误码。
 */
@Getter
@RequiredArgsConstructor
public enum AuthManageErrorCode implements ErrorCode {

    REQUEST_ID_INVALID(ResultCode.BAD_REQUEST.getCode(), "request.id.invalid"),

    APP_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "app.ids.cannot.be.empty"),
    APP_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "app.id.cannot.be.blank"),
    APP_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "app.not.found"),
    APP_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "app.code.cannot.be.blank"),
    APP_NAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "app.name.cannot.be.blank"),
    APP_CODE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "app.code.already.exists"),
    APP_NAME_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "app.name.already.exists"),
    APP_CODE_INVALID(ResultCode.BAD_REQUEST.getCode(), "app.code.invalid"),
    APP_DISABLED(ResultCode.CONFLICT.getCode(), "app.disabled"),
    APP_DISABLED_CANNOT_ASSIGN(ResultCode.CONFLICT.getCode(), "app.disabled.cannot.assign"),
    APP_ONLY_SYSTEM_USER_ASSIGNMENT_SUPPORTED(ResultCode.BAD_REQUEST.getCode(), "app.only.system.user.support.assignment"),
    APP_ASSIGNED_TO_USER(ResultCode.CONFLICT.getCode(), "app.is.assigned.to.user"),
    APP_ASSIGNED_TO_ROLE(ResultCode.CONFLICT.getCode(), "app.is.assigned.to.role"),

    POST_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "post.ids.cannot.be.empty"),
    POST_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "post.id.cannot.be.blank"),
    POST_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "post.not.found"),
    POST_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "post.code.cannot.be.blank"),
    POST_NAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "post.name.cannot.be.blank"),
    POST_CODE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "post.code.already.exists"),
    POST_NAME_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "post.name.already.exists"),
    POST_IDS_INVALID(ResultCode.BAD_REQUEST.getCode(), "post.ids.invalid"),
    POST_DISABLED_CANNOT_ASSIGN(ResultCode.CONFLICT.getCode(), "post.disabled.cannot.assign"),

    DEPT_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dept.id.cannot.be.blank"),
    DEPT_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "dept.not.found"),
    DEPT_NAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dept.name.cannot.be.blank"),
    DEPT_NAME_ALREADY_EXISTS_UNDER_PARENT(ResultCode.CONFLICT.getCode(), "dept.name.already.exists.under.parent"),
    DEPT_PARENT_CANNOT_SELF(ResultCode.BAD_REQUEST.getCode(), "dept.parent.cannot.be.self"),
    DEPT_PARENT_CANNOT_BE_DESCENDANT(ResultCode.BAD_REQUEST.getCode(), "dept.parent.cannot.be.descendant"),
    DEPT_PARENT_DISABLED_CANNOT_ENABLE(ResultCode.CONFLICT.getCode(), "dept.parent.disabled.cannot.enable"),
    DEPT_IDS_INVALID(ResultCode.BAD_REQUEST.getCode(), "dept.ids.invalid"),
    DEPT_DISABLED_CANNOT_BIND(ResultCode.CONFLICT.getCode(), "dept.disabled.cannot.bind"),
    DEPT_HAS_CHILDREN(ResultCode.CONFLICT.getCode(), "dept.has.children"),
    DEPT_HAS_USERS(ResultCode.CONFLICT.getCode(), "dept.has.users"),
    DEPT_BOUND_TO_ROLE_SCOPE(ResultCode.CONFLICT.getCode(), "dept.bound.to.role.scope"),

    ROLE_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "role.id.cannot.be.blank"),
    ROLE_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "role.ids.cannot.be.empty"),
    ROLE_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "role.not.found"),
    ROLE_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "role.code.cannot.be.blank"),
    ROLE_NAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "role.name.cannot.be.blank"),
    ROLE_CODE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "role.code.already.exists"),
    ROLE_NAME_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "role.name.already.exists"),
    ROLE_ONLY_CUSTOM_SCOPE_CAN_BIND_DEPTS(ResultCode.BAD_REQUEST.getCode(), "role.only.custom.scope.can.bind.depts"),
    ROLE_USER_IDS_INVALID(ResultCode.BAD_REQUEST.getCode(), "role.user.ids.invalid"),
    ROLE_PERMISSION_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "role.permission.ids.cannot.be.empty"),
    ROLE_PERMISSION_IDS_INVALID(ResultCode.BAD_REQUEST.getCode(), "role.permission.ids.invalid"),

    USER_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "user.id.cannot.be.blank"),
    USER_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "user.ids.cannot.be.empty"),
    USER_STATUS_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "user.status.cannot.be.blank"),
    USER_STATUS_UNSUPPORTED(ResultCode.BAD_REQUEST.getCode(), "user.status.unsupported"),
    USER_PHONE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "user.phone.already.exists"),
    USER_DEPT_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "user.dept.not.found"),
    USER_DEPT_DISABLED(ResultCode.CONFLICT.getCode(), "user.dept.disabled"),

    CONFIG_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "config.id.cannot.be.blank"),
    CONFIG_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "config.not.found"),
    CONFIG_KEY_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "config.key.cannot.be.blank"),
    CONFIG_KEY_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "config.key.already.exists"),
    CONFIG_BUILT_IN_CREATE_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "config.built.in.create.forbidden"),
    CONFIG_BUILT_IN_UPDATE_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "config.built.in.update.forbidden"),
    CONFIG_BUILT_IN_DELETE_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "config.built.in.delete.forbidden"),
    CONFIG_TYPE_CANNOT_CHANGE(ResultCode.BAD_REQUEST.getCode(), "config.type.cannot.change"),

    DICT_TYPE_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dict.type.id.cannot.be.blank"),
    DICT_TYPE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dict.type.cannot.be.blank"),
    DICT_TYPE_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "dict.type.not.found"),
    DICT_TYPE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "dict.type.already.exists"),
    DICT_TYPE_HAS_DATA(ResultCode.CONFLICT.getCode(), "dict.type.has.data"),
    DICT_TYPE_DISABLED_CANNOT_CREATE_DATA(ResultCode.CONFLICT.getCode(), "dict.type.disabled.cannot.create.data"),
    DICT_DATA_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dict.data.id.cannot.be.blank"),
    DICT_DATA_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "dict.data.not.found"),
    DICT_LABEL_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dict.label.cannot.be.blank"),
    DICT_VALUE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "dict.value.cannot.be.blank"),
    DICT_LABEL_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "dict.label.already.exists"),
    DICT_VALUE_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "dict.value.already.exists"),

    PERMISSION_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "permission.id.cannot.be.blank"),
    PERMISSION_IDS_EMPTY(ResultCode.BAD_REQUEST.getCode(), "permission.ids.cannot.be.empty"),
    PERMISSION_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "permission.not.found"),
    PERMISSION_IDENTIFICATION_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "permission.identification.already.exists"),
    PERMISSION_ROLE_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "permission.role.id.cannot.be.blank"),
    PERMISSION_USERNAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "permission.username.cannot.be.blank"),
    PERMISSION_STATUS_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "permission.status.cannot.be.blank"),
    PERMISSION_IDENTIFICATION_MAPPING_NOT_FOUND(ResultCode.BAD_REQUEST.getCode(), "permission.identification.mapping.not.found"),
    PERMISSION_IDENTIFICATION_CYCLE_DETECTED(ResultCode.BAD_REQUEST.getCode(), "permission.identification.cycle.detected"),

    LOGIN_LOG_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "login.log.not.found");

    private final Integer code;
    private final String messageKey;
}