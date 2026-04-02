package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: AuthService
 * @Description: 认证服务接口
 * <p>
 * 主要职责：
 * 1. 用户登录认证
 * 2. 密码管理（修改、重置）
 */
public interface AuthService {

    /**
     * 用户登录
     * <p>
     * 完整业务逻辑：
     * 1. 校验账号密码是否为空
     * 2. 查询用户信息
     * 3. 比对密码（Argon2 加密）
     * 4. 检查并升级密码（如需要）
     * 5. 检查账户状态（是否禁用）
     * 6. 处理互顶/并发控制
     * 7. 执行 Sa-Token 登录
     * 8. 组装返回结果
     *
     * @param loginDTO 登录参数（用户名、密码、设备标识）
     * @return LoginVO 登录结果（用户信息 + Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 修改用户密码
     * <p>
     * 业务逻辑：
     * 1. 校验用户是否存在
     * 2. 验证旧密码是否正确
     * 3. 加密新密码
     * 4. 更新数据库
     * 5. 强制踢出所有在线设备（安全考虑）
     *
     * @param userPasswordDTO 用户密码对象（包含旧密码和新密码）
     */
    void updatePassword(UserPasswordDTO userPasswordDTO);

    /**
     * 管理员重置用户密码
     * <p>
     * 业务逻辑：
     * 1. 校验用户是否存在
     * 2. 重置为默认密码（从配置读取）
     * 3. 更新数据库
     * 4. 强制踢出该用户所有在线会话（安全必须）
     * 5. 记录操作日志
     *
     * @param userId 用户 id
     */
    void resetPassword(Long userId);

    /**
     * 忘记密码 - 通过验证码重置密码
     * <p>
     * 业务逻辑：
     * 1. 校验用户名和新密码是否为空
     * 2. 查询用户信息
     * 3. 验证验证码是否正确
     * 4. 加密新密码
     * 5. 更新数据库
     * 6. 强制踢出该用户所有在线会话（安全必须）
     *
     * @param forgotPasswordDTO 忘记密码参数（用户名、新密码、验证码）
     */
    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

}
