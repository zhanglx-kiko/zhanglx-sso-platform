package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
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

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 21:14
 * @ClassName: AuthServiceImpl
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final Argon2PasswordEncoder argon2PasswordEncoder;

    @Value("${default.password:123456}")
    private String defaultPassword;

    /**
     * 用户登录实现
     * 完整逻辑：校验 -> 查询 -> 比对 -> 互顶判断 -> 登录 -> 组装结果
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("user.account.empty");
        }

        LambdaQueryWrapperX<UserPO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(UserPO::getUsername, loginDTO.getUsername());
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("登录失败，账号不存在: {}", loginDTO.getUsername());
            throw new BusinessException("business.user.not.found");
        }

        if (!checkpw(loginDTO.getPassword(), userPO.getPassword())) {
            log.warn("登录失败，密码错误. 用户: {}", loginDTO.getUsername());
            throw new BusinessException("user.password.error");
        }

        if (Integer.valueOf(0).equals(userPO.getStatus())) {
            throw new BusinessException("user.account.disabled");
        }

        // --- 互顶/并发控制 ---
        Integer allowConcurrent = userPO.getAllowConcurrentLogin();
        // 如果禁止并发 (0)，则踢掉该账号之前所有的登录 Session
        if (Integer.valueOf(0).equals(allowConcurrent)) {
            // 注意：这里不需要传 device，直接踢掉该 UserID 下的所有令牌
            StpUtil.logout(userPO.getId());
            log.info("用户 [{}] 配置为互顶模式，已强制清理旧会话", userPO.getUsername());
        }

        // 执行登录
        StpUtil.login(userPO.getId(), loginDTO.getDevice());

        // 检查并升级密码（登录成功后）
        if (argon2PasswordEncoder.needUpgrade(userPO.getPassword())) {
            log.info("检测到用户 [{}] 密码参数需要升级", userPO.getUsername());
            upgradeUserPassword(userPO, loginDTO.getPassword());
        }

        return assembleLoginVO(userPO, StpUtil.getTokenInfo());
    }

    /**
     * 独立修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordDTO userPasswordDTO) {
        UserPO userPO = userMapper.selectById(userPasswordDTO.getUserId());
        if (userPO == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 校验旧密码是否正确
        if (!checkpw(userPasswordDTO.getOldPassword(), userPO.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 2. 加密新密码
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(userPasswordDTO.getNewPassword()));

        userMapper.updateById(userPO);

        // 3. (可选) 修改密码后，是否需要踢掉所有在线设备让其重新登录？
        StpUtil.logout(userPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 加密新密码
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(defaultPassword));

        // 2. 更新数据库
        userMapper.updateById(userPO);

        // 3. 【关键安全步骤】管理员强制重置密码后，必须踢该用户下线！
        // 否则该用户凭旧 Token 还能继续操作，存在安全隐患
        StpUtil.logout(userPO.getId());
        log.info("管理员重置了用户 [{}] 的密码，并强制踢其下线", userPO.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // 1. 查询用户信息
        LambdaQueryWrapperX<UserPO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(UserPO::getUsername, forgotPasswordDTO.getUsername());
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("修改密码失败，账号不存在：{}", forgotPasswordDTO.getUsername());
            throw new BusinessException("business.user.not.found");
        }

        // 2. 检查账户状态
        if (Integer.valueOf(0).equals(userPO.getStatus())) {
            throw new BusinessException("user.account.disabled");
        }

        // 3. 验证验证码（这里需要对接您的验证码服务，暂时模拟验证）
        // TODO: 替换为实际的验证码验证逻辑（例如从 Redis 获取验证码进行比对）
        if (!verifyVerificationCode(forgotPasswordDTO.getUsername(), forgotPasswordDTO.getVerificationCode())) {
            throw new BusinessException("invalid.verification.code");
        }

        // 4. 加密新密码
        String encodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(forgotPasswordDTO.getNewPassword());

        // 5. 更新数据库
        userPO.setPassword(encodedPassword);
        userMapper.updateById(userPO);

        // 6. 【关键安全步骤】密码重置后，必须踢该用户下线！
        // 防止旧 Token 继续使用
        StpUtil.logout(userPO.getId());

        log.info("用户 [{}] 通过验证码成功重置密码，并强制踢其下线", userPO.getUsername());
    }

    /**
     * 验证验证码
     *
     * @param username         用户名
     * @param verificationCode 验证码
     * @return 是否验证通过
     */
    private boolean verifyVerificationCode(String username, String verificationCode) {
        // TODO: 这里需要从 Redis 或其他存储中获取验证码进行比对
        // 示例代码：
        // String storedCode = redisTemplate.opsForValue().get("captcha:" + username);
        // return verificationCode.equals(storedCode);

        // 临时实现：假设验证码总是正确（仅用于测试）
        // 生产环境必须替换为真实验证逻辑
        log.warn("【警告】验证码验证功能尚未实现，当前始终返回 true。请尽快接入实际验证码服务！");
        return true;
    }

    /**
     * 升级用户密码
     */
    private void upgradeUserPassword(UserPO userPO, String rawPassword) {
        try {
            // 使用新参数重新加密
            String newEncodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword);

            // 更新数据库
            userPO.setPassword(newEncodedPassword);
            userMapper.updateById(userPO);

            log.info("用户 [{}] 密码升级成功", userPO.getUsername());
        } catch (Exception e) {
            log.error("用户 [{}] 密码升级失败", userPO.getUsername(), e);
        }
    }

    /**
     * 密码校验
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的哈希字符串
     * @return 是否校验通过，true表示校验通过
     */
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
