package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.domain.vo.SmsVerificationCodeSendVO;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.auth.service.support.AuthLoginAuditSupport;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.sms.enums.SmsVerificationBusinessType;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendResult;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;
import com.zhanglx.sso.sms.service.SmsVerificationCodeManager;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    /**
     * 用户映射器。
     */
    private final UserMapper userMapper;
    /**
     * Argon2 密码编码器。
     */
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    /**
     * 登录审计支持组件。
     */
    private final AuthLoginAuditSupport authLoginAuditSupport;
    /**
     * 操作保护组件。
     */
    private final AuthOperationGuard authOperationGuard;
    /**
     * 短信验证码管理器。
     */
    private final SmsVerificationCodeManager smsVerificationCodeManager;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;

    /**
     * 默认密码。
     */
    @Value("${default.password:123456}")
    private String defaultPassword;

    @Override
    public LoginVO login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null
                || !StringUtils.hasText(userLoginDTO.getUsername())
                || !StringUtils.hasText(userLoginDTO.getPassword())) {
            throw new BusinessException("user.account.empty");
        }

        LambdaQueryWrapperX<UserPO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(UserPO::getUsername, userLoginDTO.getUsername());
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("登录失败，账号不存在：{}", userLoginDTO.getUsername());
            throw new BusinessException(UserErrorCode.BUSINESS_USER_NOT_FOUND);
        }

        if (!checkpw(userLoginDTO.getPassword(), userPO.getPassword())) {
            log.warn("登录失败，密码错误：{}", userLoginDTO.getUsername());
            throw new BusinessException(UserErrorCode.USER_PASSWORD_ERROR);
        }

        if (UserStatusEnum.DISABLED.matches(userPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        if (YesNoEnum.NO.matches(userPO.getAllowConcurrentLogin())) {
            StpUtil.logout(userPO.getId());
            log.info("用户 [{}] 不允许并发登录，已清理旧会话", userPO.getUsername());
        }

        StpUtil.login(userPO.getId(), userLoginDTO.getDevice());
        authLoginAuditSupport.storeAdminSession(
                userPO.getUsername(),
                StringUtils.hasText(userPO.getNickname()) ? userPO.getNickname() : userPO.getUsername(),
                AuthLoginAuditSupport.CLIENT_TYPE_SYS_PASSWORD
        );

        if (argon2PasswordEncoder.needUpgrade(userPO.getPassword())) {
            log.info("检测到用户 [{}] 密码参数需要升级", userPO.getUsername());
            upgradeUserPassword(userPO, userLoginDTO.getPassword());
        }

        return assembleLoginVO(userPO, StpUtil.getTokenInfo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordDTO userPasswordDTO) {
        UserPO userPO = userMapper.selectById(userPasswordDTO.getUserId());
        if (userPO == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, userPasswordDTO.getUserId());
        }

        if (!checkpw(userPasswordDTO.getOldPassword(), userPO.getPassword())) {
            throw new BusinessException(UserErrorCode.USER_OLD_PASSWORD_ERROR);
        }

        updateUserPassword(userPO.getId(), argon2PasswordEncoder.encodeAsyncWithTimeout(userPasswordDTO.getNewPassword()));
        StpUtil.logout(userPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        authOperationGuard.checkResetPasswordNotSelf(userId);
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, userId);
        }

        updateUserPassword(userPO.getId(), argon2PasswordEncoder.encodeAsyncWithTimeout(defaultPassword));
        StpUtil.logout(userPO.getId());
        log.info("管理员已重置用户 [{}] 的密码，并强制其重新登录", userPO.getUsername());
    }

    @Override
    public SmsVerificationCodeSendVO sendForgotPasswordVerificationCode(ForgotPasswordVerificationCodeSendDTO sendDTO) {
        UserPO userPO = getActiveUserByUsername(sendDTO.getUsername());
        if (!StringUtils.hasText(userPO.getPhoneNumber())) {
            throw BusinessException.badRequest("user.phone.not.bound");
        }

        SmsVerificationCodeSendResult sendResult = smsVerificationCodeManager.sendCode(
                SmsVerificationCodeSendCommand.builder()
                        .businessType(SmsVerificationBusinessType.SYS_USER)
                        .sceneType(SmsSceneType.FORGOT_PASSWORD)
                        .phoneNumber(userPO.getPhoneNumber())
                        .subjectKey(String.valueOf(userPO.getId()))
                        .clientIp(resolveCurrentClientIp())
                        .build()
        );

        return new SmsVerificationCodeSendVO(
                sendResult.getMaskedPhoneNumber(),
                sendResult.getExpireSeconds(),
                sendResult.getResendIntervalSeconds()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        UserPO userPO = getActiveUserByUsername(forgotPasswordDTO.getUsername());
        if (!StringUtils.hasText(userPO.getPhoneNumber())) {
            throw BusinessException.badRequest("user.phone.not.bound");
        }

        smsVerificationCodeManager.verifyCode(
                SmsVerificationCodeVerifyCommand.builder()
                        .businessType(SmsVerificationBusinessType.SYS_USER)
                        .sceneType(SmsSceneType.FORGOT_PASSWORD)
                        .phoneNumber(userPO.getPhoneNumber())
                        .subjectKey(String.valueOf(userPO.getId()))
                        .verificationCode(forgotPasswordDTO.getVerificationCode())
                        .build()
        );

        updateUserPassword(userPO.getId(), argon2PasswordEncoder.encodeAsyncWithTimeout(forgotPasswordDTO.getNewPassword()));
        StpUtil.logout(userPO.getId());
        log.info("用户 [{}] 通过验证码重置了密码，并已强制下线", userPO.getUsername());
    }

    /**
     * 根据业务条件查询当前可用数据。
     */
    private UserPO getActiveUserByUsername(String username) {
        LambdaQueryWrapperX<UserPO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(UserPO::getUsername, username);
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("修改密码失败，账号不存在：{}", username);
            throw new BusinessException(UserErrorCode.BUSINESS_USER_NOT_FOUND);
        }

        if (UserStatusEnum.DISABLED.matches(userPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        return userPO;
    }

    /**
     * 按新算法升级已有数据。
     */
    private void upgradeUserPassword(UserPO userPO, String rawPassword) {
        try {
            String newEncodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword);
            updateUserPassword(userPO.getId(), newEncodedPassword);
            log.info("用户 [{}] 密码升级成功", userPO.getUsername());
        } catch (Exception e) {
            log.error("用户 [{}] 密码升级失败", userPO.getUsername(), e);
        }
    }

/**
 * 更新内部状态或数据。
 */
    /**
     * 更新系统用户密码。
     */
    private void updateUserPassword(Long userId, String encodedPassword) {
        userMapper.update(
                null,
                new LambdaUpdateWrapper<UserPO>()
                        .eq(UserPO::getId, userId)
                        .set(UserPO::getPassword, encodedPassword)
        );
    }

    /**
     * 校验明文密码与密文是否匹配。
     */
    private boolean checkpw(String rawPassword, String encodedPassword) {
        return argon2PasswordEncoder.matchesAsyncWithTimeout(rawPassword, encodedPassword);
    }

    /**
     * 解析当前请求的客户端 IP。
     */
    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }

    /**
     * 组装返回对象。
     */
    private LoginVO assembleLoginVO(UserPO userPO, SaTokenInfo tokenInfo) {
        LoginVO loginVO = new LoginVO();
        loginVO.setId(userPO.getId());
        loginVO.setUsername(userPO.getUsername());
        loginVO.setNickname(userPO.getNickname());
        loginVO.setAvatar(userPO.getAvatar());
        loginVO.setDeptId(userPO.getDeptId());
        loginVO.setTokenName(tokenInfo.getTokenName());
        loginVO.setTokenValue(tokenInfo.getTokenValue());
        return loginVO;
    }
}