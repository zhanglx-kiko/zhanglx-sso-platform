package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:38
 * @ClassName: UserService
 * @Description: 用户管理服务接口
 * <p>
 * 主要职责：
 * 1. 用户基本信息管理（增删改查）
 * 2. 微信用户管理
 * 3. 用户分页查询
 */
public interface UserService {

    /**
     * 新增用户
     * <p>
     * 业务逻辑：
     * 1. 校验用户名唯一性
     * 2. 加密密码（使用 Argon2 算法）
     * 3. 设置默认状态（启用、不允许并发登录）
     * 4. 保存到数据库
     *
     * @param user 用户信息对象
     */
    void addUser(UserDTO user);

    /**
     * 更新用户基本信息
     * <p>
     * 业务逻辑：
     * 1. 校验用户是否存在
     * 2. 如果修改了用户名，校验新用户名唯一性（排除自身）
     * 3. 忽略密码字段（密码修改有独立接口）
     * 4. 保留原用户状态
     *
     * @param userinfo 用户信息对象（包含要更新的字段）
     */
    void updateUserInfo(UserDTO userinfo);

    /**
     * 根据 id 删除用户
     * <p>
     * 业务逻辑：
     * 1. 逻辑删除：先修改用户名为唯一值（释放唯一索引）
     * 2. 物理删除用户记录
     *
     * @param userId 用户 id
     */
    void removeUserById(Long userId);

    /**
     * 分页查询用户列表
     * <p>
     * 业务逻辑：
     * 1. 构建动态查询条件（用户名模糊、部门精确）
     * 2. 按创建时间倒序排序
     * 3. 转换为 DTO 返回
     *
     * @param query 查询参数（分页、用户名、部门 ID）
     * @return Page<UserDTO> 分页用户信息
     */
    Page<UserDTO> pageQuery(UserPageQueryDTO query);

    /**
     * 根据 OpenID 查询用户
     * <p>
     * 用于微信授权登录后查找关联用户
     *
     * @param openId 微信 OpenID
     * @return UserDTO 用户信息
     */
    UserDTO getUserByOpenId(String openId);

    /**
     * 保存微信用户信息
     * <p>
     * 业务逻辑：
     * 1. 校验用户名唯一性
     * 2. 加密密码（设置默认密码）
     * 3. 设置默认状态
     * 4. 保存到数据库
     *
     * @param user 微信用户信息
     * @return UserDTO 保存后的用户信息
     */
    UserDTO addWxUser(UserDTO user);

    /**
     * 根据账号查询用户信息
     * <p>
     * 用于登录时查询用户
     *
     * @param username 账号
     * @return UserDTO 用户信息
     */
    UserDTO findUserByUsername(String username);

}
