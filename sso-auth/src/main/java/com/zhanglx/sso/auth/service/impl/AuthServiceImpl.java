package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final Argon2PasswordEncoder argon2PasswordEncoder;

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

        if (Integer.valueOf(0).equals(userPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        if (Integer.valueOf(0).equals(userPO.getAllowConcurrentLogin())) {
            StpUtil.logout(userPO.getId());
            log.info("用户 [{}] 不允许并发登录，已清理旧会话", userPO.getUsername());
        }

        StpUtil.login(userPO.getId(), userLoginDTO.getDevice());

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

        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(userPasswordDTO.getNewPassword()));
        userMapper.updateById(userPO);
        StpUtil.logout(userPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, userId);
        }

        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(defaultPassword));
        userMapper.updateById(userPO);
        StpUtil.logout(userPO.getId());
        log.info("管理员已重置用户 [{}] 的密码，并强制其重新登录", userPO.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        LambdaQueryWrapperX<UserPO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(UserPO::getUsername, forgotPasswordDTO.getUsername());
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("忘记密码失败，账号不存在：{}", forgotPasswordDTO.getUsername());
            throw new BusinessException(UserErrorCode.BUSINESS_USER_NOT_FOUND);
        }

        if (Integer.valueOf(0).equals(userPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        if (!verifyVerificationCode(forgotPasswordDTO.getUsername(), forgotPasswordDTO.getVerificationCode())) {
            throw new BusinessException("invalid.verification.code");
        }

        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(forgotPasswordDTO.getNewPassword()));
        userMapper.updateById(userPO);
        StpUtil.logout(userPO.getId());
        log.info("用户 [{}] 通过验证码重置了密码，并已强制下线", userPO.getUsername());
    }

    private boolean verifyVerificationCode(String username, String verificationCode) {
        log.warn("验证码校验逻辑尚未接入真实服务，当前使用占位实现，username={}", username);
        return StringUtils.hasText(verificationCode);
    }

    private void upgradeUserPassword(UserPO userPO, String rawPassword) {
        try {
            String newEncodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword);
            userPO.setPassword(newEncodedPassword);
            userMapper.updateById(userPO);
            log.info("用户 [{}] 密码升级成功", userPO.getUsername());
        } catch (Exception e) {
            log.error("用户 [{}] 密码升级失败", userPO.getUsername(), e);
        }
    }

    private boolean checkpw(String rawPassword, String encodedPassword) {
        return argon2PasswordEncoder.matchesAsyncWithTimeout(rawPassword, encodedPassword);
    }

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
