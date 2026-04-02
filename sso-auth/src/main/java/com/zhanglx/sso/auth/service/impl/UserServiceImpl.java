package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.utils.IUserDomainMapper;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:40
 * @ClassName: UserServiceImpl
 * @Description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final Argon2PasswordEncoder argon2PasswordEncoder;

    @Value("${default.password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO user) {
        // 1. 校验用户名是否存在
        checkUsernameUnique(user.getUsername(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);

        // 2. 处理密码 (如果没有传密码，给一个默认初始密码，例如 123456)
        String rawPassword = StrUtil.isBlank(user.getPassword()) ? defaultPassword : user.getPassword();
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword));

        // 3. 设置默认值 (如果前端没传)
        if (userPO.getStatus() == null) userPO.setStatus(1); // 默认启用
        if (userPO.getAllowConcurrentLogin() == null) userPO.setAllowConcurrentLogin(0); // 默认不允许并发

        userMapper.insert(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO addWxUser(UserDTO user) {
        // 1. 校验用户名是否存在
        checkUsernameUnique(user.getUsername(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);

        // 2. 处理密码 (如果没有传密码，给一个默认初始密码，例如 123456)
        String rawPassword = StrUtil.isBlank(user.getPassword()) ? defaultPassword : user.getPassword();
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword));

        // 3. 设置默认值 (如果前端没传)
        if (userPO.getStatus() == null) userPO.setStatus(1); // 默认启用
        if (userPO.getAllowConcurrentLogin() == null) userPO.setAllowConcurrentLogin(0); // 默认不允许并发

        userMapper.insert(userPO);
        return IUserDomainMapper.INSTANCE.toDTO(userPO);
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        AssertUtils.notBlank(username, "user.username.cannot.be.blank");
        return IUserDomainMapper.INSTANCE.toDTO(userMapper.selectOne(UserPO::getUsername, username));
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
        AssertUtils.notNull(userinfo.getId(), "business.data.invalid");

        UserPO oldUser = userMapper.selectById(userinfo.getId());
        if (oldUser == null) {
            throw new BusinessException("用户不存在");
        }

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(userinfo);
        // 1. 如果修改了用户名，需要校验唯一性 (排除自己)
        if (StrUtil.isNotBlank(userinfo.getUsername()) && !userinfo.getUsername().equals(oldUser.getUsername())) {
            checkUsernameUnique(userinfo.getUsername(), userPO.getId());
        }

        userPO.setPassword(oldUser.getPassword());
        // 修改用户基本信息接口不能修改用户原来的状态
        userPO.setStatus(oldUser.getStatus());

        userMapper.updateById(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserById(Long userId) {
        UserPO user = userMapper.selectById(userId);
        if (user == null) return;

        userMapper.deleteByIdWithFill(userId);
    }

    @Override
    public Page<UserDTO> pageQuery(UserPageQueryDTO query) {
        Page<UserPO> page = Page.of(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.like(StrUtil.isNotBlank(query.getUsername()), UserPO::getUsername, query.getUsername())
                .eq(query.getDeptId() != null, UserPO::getDeptId, query.getDeptId())
                .orderByDesc(UserPO::getCreateTime);

        userMapper.selectPage(page, wrapper);

        Page<UserDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(IUserDomainMapper.INSTANCE.toDTOList(page.getRecords()));
        return result;
    }

    @Override
    public UserDTO getUserByOpenId(String openId) {
        AssertUtils.notBlank(openId, "user.openId.cannot.be.blank");
        UserPO user = userMapper.selectOne(UserPO::getOpenId, openId);
        return IUserDomainMapper.INSTANCE.toDTO(user);
    }

    /**
     * 校验用户名在当前有效数据范围内是否唯一。
     *
     * <p>新增用户时 {@code excludeId} 传 {@code null}；修改用户时传入当前用户 ID，
     * 以便在查询重复用户名时排除自身记录。</p>
     *
     * @param username 待校验的用户名
     * @param excludeId 需要排除的用户 ID
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        AssertUtils.notBlank(username, "user.username.cannot.be.blank");

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(UserPO::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名 [" + username + "] 已存在");
        }
    }

}
