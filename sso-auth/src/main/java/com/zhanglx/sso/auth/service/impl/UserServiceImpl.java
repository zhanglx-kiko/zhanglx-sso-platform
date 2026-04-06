package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.domain.po.SysUserSocialPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.SysUserSocialMapper;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String WECHAT_OPEN_IDENTITY_TYPE = "WECHAT_OPEN";

    private final UserMapper userMapper;
    private final SysUserSocialMapper sysUserSocialMapper;
    private final Argon2PasswordEncoder argon2PasswordEncoder;

    @Value("${default.password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO user) {
        checkUsernameUnique(user.getUsername(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);
        fillDefaultUserFields(userPO, 1);
        userMapper.insert(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO addWxUser(UserDTO user, String openId) {
        AssertUtils.notBlank(openId, "user.openId.cannot.be.blank");
        checkUsernameUnique(user.getUsername(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);
        fillDefaultUserFields(userPO, 2);
        userMapper.insert(userPO);

        SysUserSocialPO existSocial = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, WECHAT_OPEN_IDENTITY_TYPE,
                SysUserSocialPO::getIdentifier, openId
        );
        if (existSocial == null) {
            SysUserSocialPO userSocialPO = SysUserSocialPO.builder()
                    .userId(userPO.getId())
                    .identityType(WECHAT_OPEN_IDENTITY_TYPE)
                    .identifier(openId)
                    .build();
            sysUserSocialMapper.insert(userSocialPO);
        } else {
            existSocial.setUserId(userPO.getId());
            sysUserSocialMapper.updateById(existSocial);
        }

        UserDTO result = IUserDomainMapper.INSTANCE.toDTO(userPO);
        result.setOpenId(openId);
        return result;
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        AssertUtils.notBlank(username, "user.username.cannot.be.blank");
        UserPO userPO = userMapper.selectOne(UserPO::getUsername, username);
        if (userPO == null) {
            return null;
        }

        UserDTO userInfo = IUserDomainMapper.INSTANCE.toDTO(userPO);
        userInfo.setDeptName("部门信息待补充");
        return userInfo;
    }

    @Override
    public void disableUser(Long userId) {
        AssertUtils.notNull(userId, UserErrorCode.USER_INFO_NOT_FOUND);
        UserPO userPO = userMapper.selectById(userId);
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userId);

        switch (userPO.getStatus()) {
            case 0 -> userPO.setStatus(1);
            default -> {
                userPO.setStatus(0);
                StpUtil.logout(userPO.getId());
            }
        }

        userMapper.updateById(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserBaseDTO userInfo) {
        AssertUtils.notNull(userInfo.getId(), "business.data.invalid");

        UserPO userPO = userMapper.selectById(userInfo.getId());
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userInfo.getId());

        userPO.setNickname(userInfo.getNickname());
        userPO.setAvatar(userInfo.getAvatar());
        userPO.setPhoneNumber(userInfo.getPhoneNumber());
        userPO.setSex(userInfo.getSex());
        userPO.setEmail(userInfo.getEmail());
        userPO.setDeptId(userInfo.getDeptId());
        userPO.setAllowConcurrentLogin(userInfo.getAllowConcurrentLogin());
        userMapper.updateById(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserById(Long userId) {
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        sysUserSocialMapper.deleteByUserId(userId);
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

        List<UserDTO> dtoList = IUserDomainMapper.INSTANCE.toDTOList(page.getRecords());
        dtoList.forEach(dto -> dto.setDeptName("部门信息待补充"));
        result.setRecords(dtoList);
        return result;
    }

    @Override
    public UserDTO getUserByOpenId(String openId) {
        AssertUtils.notBlank(openId, "user.openId.cannot.be.blank");
        SysUserSocialPO userSocialPO = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, WECHAT_OPEN_IDENTITY_TYPE,
                SysUserSocialPO::getIdentifier, openId
        );
        if (userSocialPO == null) {
            return null;
        }

        UserPO user = userMapper.selectById(userSocialPO.getUserId());
        if (user == null) {
            return null;
        }

        UserDTO result = IUserDomainMapper.INSTANCE.toDTO(user);
        result.setOpenId(openId);
        return result;
    }

    private void fillDefaultUserFields(UserPO userPO, Integer userType) {
        String rawPassword = StrUtil.isBlank(userPO.getPassword()) ? defaultPassword : userPO.getPassword();
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword));

        if (userPO.getStatus() == null) {
            userPO.setStatus(1);
        }
        if (userPO.getAllowConcurrentLogin() == null) {
            userPO.setAllowConcurrentLogin(0);
        }
        if (userPO.getUserType() == null) {
            userPO.setUserType(userType);
        }
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        AssertUtils.notBlank(username, "user.username.cannot.be.blank");

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(UserPO::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }
}
