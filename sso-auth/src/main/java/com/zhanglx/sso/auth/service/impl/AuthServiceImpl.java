package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserQueryDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.common.exception.BusinessException;
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

    @Value("default.password")
    private String defaultPassword;

    /**
     * 用户登录实现
     * 完整逻辑：校验 -> 查询 -> 比对 -> 互顶判断 -> 登录 -> 组装结果
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("账号或密码不能为空");
        }

        LambdaQueryWrapper<UserPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPO::getUsername, loginDTO.getUsername());
        UserPO userPO = userMapper.selectOne(queryWrapper);

        if (userPO == null) {
            log.warn("登录失败，账号不存在: {}", loginDTO.getUsername());
            throw new BusinessException("账号或密码错误");
        }

        // 【修复】取反！如果checkpw返回false，说明密码错误
        if (!BCrypt.checkpw(loginDTO.getPassword(), userPO.getPassword())) {
            log.warn("登录失败，密码错误. 用户: {}", loginDTO.getUsername());
            throw new BusinessException("账号或密码错误");
        }

        if (Integer.valueOf(0).equals(userPO.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
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

        return assembleLoginVO(userPO, StpUtil.getTokenInfo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(UserDTO user) {
        // 1. 校验用户名是否存在
        checkUsernameUnique(user.getUsername(), null);

        UserPO userPO = BeanUtil.copyProperties(user, UserPO.class);

        // 2. 处理密码 (如果没有传密码，给一个默认初始密码，例如 123456)
        String rawPassword = StrUtil.isBlank(user.getPassword()) ? defaultPassword : user.getPassword();
        userPO.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));

        // 3. 设置默认值 (如果前端没传)
        if (userPO.getStatus() == null) userPO.setStatus(1); // 默认启用
        if (userPO.getAllowConcurrentLogin() == null) userPO.setAllowConcurrentLogin(1); // 默认允许并发

        userMapper.insert(userPO);
    }

    /**
     * 修改用户基本信息 (不包含密码)
     * 1. 校验用户是否存在
     * 2. 如果改了用户名，要校验唯一性
     * 3. 忽略密码字段的更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserDTO userinfo) {
        if (userinfo.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        UserPO oldUser = userMapper.selectById(userinfo.getId());
        if (oldUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 如果修改了用户名，需要校验唯一性 (排除自己)
        if (StrUtil.isNotBlank(userinfo.getUsername()) && !userinfo.getUsername().equals(oldUser.getUsername())) {
            checkUsernameUnique(userinfo.getUsername(), userinfo.getId());
        }

        // 强制忽略 password 字段，防止被意外修改
        BeanUtil.copyProperties(userinfo, oldUser, "password");

        userMapper.updateById(oldUser);
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
        if (!BCrypt.checkpw(userPasswordDTO.getOldPassword(), userPO.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 2. 加密新密码
        String newHash = BCrypt.hashpw(userPasswordDTO.getNewPassword(), BCrypt.gensalt());
        userPO.setPassword(newHash);

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
        String hashPassword = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        userPO.setPassword(hashPassword);

        // 2. 更新数据库
        userMapper.updateById(userPO);

        // 3. 【关键安全步骤】管理员强制重置密码后，必须踢该用户下线！
        // 否则该用户凭旧 Token 还能继续操作，存在安全隐患
        StpUtil.logout(userPO.getId());
        log.info("管理员重置了用户 [{}] 的密码，并强制踢其下线", userPO.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserById(Long userId) {
        UserPO user = userMapper.selectById(userId);
        if (user == null) return;

        // 逻辑删除前改名，释放唯一索引
        String newName = user.getUsername() + "_del_" + System.currentTimeMillis();
        user.setUsername(newName);
        userMapper.updateById(user);

        userMapper.deleteById(userId);
    }

    @Override
    public Page<UserDTO> pageQuery(UserQueryDTO query) {
        Page<UserPO> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getUsername()), UserPO::getUsername, query.getUsername())
                .eq(query.getDeptId() != null, UserPO::getDeptId, query.getDeptId())
                .orderByDesc(UserPO::getCreateTime);

        userMapper.selectPage(page, wrapper);

        // 使用 Convert 转换泛型
        return (Page<UserDTO>) page.convert(po -> {
            return BeanUtil.copyProperties(po, UserDTO.class);
        });
    }

    /**
     * 校验用户名唯一性
     *
     * @param username  用户名
     * @param excludeId 排除的ID (修改时使用，新增传null)
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        if (StrUtil.isBlank(username)) return;

        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名 [" + username + "] 已存在");
        }
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
