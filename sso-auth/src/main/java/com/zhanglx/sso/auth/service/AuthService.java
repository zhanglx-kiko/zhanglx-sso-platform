package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserQueryDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: AuthService
 * @Description:
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果 VO
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 新增用户
     *
     * @param user 用户信息
     */
    void saveUser(UserDTO user);

    /**
     * 更新用户基本信息
     *
     * @param userinfo 用户信息
     */
    void updateUserInfo(UserDTO userinfo);

    /**
     * 修改用户密码
     *
     * @param userPasswordDTO 用户密码对象
     */
    void updatePassword(UserPasswordDTO userPasswordDTO);

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户id
     */
    void resetPassword(Long userId);

    /**
     * 根据id删除用户
     *
     * @param userId 用户id
     * @return 是否成功
     */
    void removeUserById(Long userId);

    /**
     * 分页查询用户列表
     *
     * @param query 查询参数
     * @return 分页用户信息
     */
    Page<UserDTO> pageQuery(UserQueryDTO query);

}
