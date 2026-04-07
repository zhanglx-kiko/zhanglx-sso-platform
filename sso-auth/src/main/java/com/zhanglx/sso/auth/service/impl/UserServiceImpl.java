package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.domain.po.DeptPO;
import com.zhanglx.sso.auth.domain.po.SysUserSocialPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.SocialIdentityTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.DeptMapper;
import com.zhanglx.sso.auth.mapper.SysUserSocialMapper;
import com.zhanglx.sso.auth.mapper.UserAppMapper;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.mapper.UserPostMapper;
import com.zhanglx.sso.auth.mapper.UserRoleRelationshipMappingMapper;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.IUserDomainMapper;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final SysUserSocialMapper sysUserSocialMapper;
    private final UserAppMapper userAppMapper;
    private final UserPostMapper userPostMapper;
    private final UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    private final AuthOperationGuard authOperationGuard;

    @Value("${default.password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO user) {
        validateDept(user.getDeptId());
        checkUsernameUnique(user.getUsername(), null);
        checkPhoneUnique(user.getPhoneNumber(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);
        fillDefaultUserFields(userPO, UserTypeEnum.SYSTEM);
        userMapper.insert(userPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO addWxUser(UserDTO user, String openId) {
        AssertUtils.notBlank(openId, "user.openId.cannot.be.blank");
        validateDept(user.getDeptId());
        checkUsernameUnique(user.getUsername(), null);
        checkPhoneUnique(user.getPhoneNumber(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);
        fillDefaultUserFields(userPO, UserTypeEnum.MEMBER);
        userMapper.insert(userPO);

        SysUserSocialPO existSocial = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, SocialIdentityTypeEnum.WECHAT_OPEN.getCode(),
                SysUserSocialPO::getIdentifier, openId
        );
        if (existSocial == null) {
            SysUserSocialPO userSocialPO = SysUserSocialPO.builder()
                    .userId(userPO.getId())
                    .identityType(SocialIdentityTypeEnum.WECHAT_OPEN.getCode())
                    .identifier(openId)
                    .build();
            sysUserSocialMapper.insert(userSocialPO);
        } else {
            existSocial.setUserId(userPO.getId());
            sysUserSocialMapper.updateById(existSocial);
        }

        UserDTO result = IUserDomainMapper.INSTANCE.toDTO(userPO);
        result.setOpenId(openId);
        applyDeptName(result, buildDeptNameMap(Collections.singleton(userPO.getDeptId())));
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
        applyDeptName(userInfo, buildDeptNameMap(Collections.singleton(userPO.getDeptId())));
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserBaseDTO userInfo) {
        AssertUtils.notNull(userInfo.getId(), "business.data.invalid");
        validateDept(userInfo.getDeptId());
        checkPhoneUnique(userInfo.getPhoneNumber(), userInfo.getId());

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
        authOperationGuard.checkDeleteUserNotSelf(userId);
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        sysUserSocialMapper.deleteByUserId(userId);
        userAppMapper.deleteByUserId(userId);
        userPostMapper.deleteByUserId(userId);
        userRoleRelationshipMappingMapper.deleteByUserId(userId);
        StpUtil.logout(userId);
        userMapper.deleteByIdWithFill(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveUsers(List<Long> userIds) {
        AssertUtils.notEmpty(userIds, "user ids cannot be empty");
        List<Long> normalizedUserIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        authOperationGuard.checkDeleteUsersNotContainsSelf(normalizedUserIds);
        normalizedUserIds.forEach(this::removeUserById);
    }

    @Override
    public Page<UserDTO> pageQuery(UserPageQueryDTO query) {
        Page<UserPO> page = Page.of(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<UserPO>()
                .likeIfPresent(UserPO::getUsername, query.getUsername())
                .eqIfPresent(UserPO::getDeptId, query.getDeptId())
                .orderByDesc(UserPO::getCreateTime);

        if (StrUtil.isNotBlank(query.getSearchKey())) {
            wrapper.and(w -> w.like(UserPO::getUsername, query.getSearchKey())
                    .or()
                    .like(UserPO::getPhoneNumber, query.getSearchKey())
                    .or()
                    .like(UserPO::getNickname, query.getSearchKey()));
        }

        userMapper.selectPage(page, wrapper);

        Page<UserDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());

        List<UserDTO> dtoList = IUserDomainMapper.INSTANCE.toDTOList(page.getRecords());
        Map<Long, String> deptNameMap = buildDeptNameMap(page.getRecords().stream().map(UserPO::getDeptId).toList());
        dtoList.forEach(dto -> applyDeptName(dto, deptNameMap));
        result.setRecords(dtoList);
        return result;
    }

    @Override
    public UserDTO getUserByOpenId(String openId) {
        AssertUtils.notBlank(openId, "user.openId.cannot.be.blank");
        SysUserSocialPO userSocialPO = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, SocialIdentityTypeEnum.WECHAT_OPEN.getCode(),
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
        applyDeptName(result, buildDeptNameMap(Collections.singleton(user.getDeptId())));
        return result;
    }

    @Override
    public UserDTO getUserDetail(Long userId) {
        UserPO userPO = userMapper.selectById(userId);
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userId);
        UserDTO userDTO = IUserDomainMapper.INSTANCE.toDTO(userPO);
        applyDeptName(userDTO, buildDeptNameMap(Collections.singleton(userPO.getDeptId())));
        return userDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        if (UserStatusEnum.DISABLED.matches(status)) {
            authOperationGuard.checkDisableUserNotSelf(userId);
        }
        AssertUtils.notNull(userId, UserErrorCode.USER_INFO_NOT_FOUND);
        AssertUtils.notNull(status, "status cannot be null");
        UserPO userPO = userMapper.selectById(userId);
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userId);
        userPO.setStatus(status);
        userMapper.updateById(userPO);
        if (UserStatusEnum.DISABLED.matches(status)) {
            StpUtil.logout(userPO.getId());
        }
    }

    private void fillDefaultUserFields(UserPO userPO, UserTypeEnum userType) {
        String rawPassword = StrUtil.isBlank(userPO.getPassword()) ? defaultPassword : userPO.getPassword();
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword));

        if (userPO.getStatus() == null) {
            userPO.setStatus(UserStatusEnum.NORMAL.getCode());
        }
        if (userPO.getAllowConcurrentLogin() == null) {
            userPO.setAllowConcurrentLogin(YesNoEnum.YES.getCode());
        }
        if (userPO.getUserType() == null) {
            userPO.setUserType(userType.getCode());
        }
    }

    private void validateDept(Long deptId) {
        if (deptId == null) {
            return;
        }
        DeptPO deptPO = deptMapper.selectById(deptId);
        AssertUtils.notNull(deptPO, "department does not exist");
        AssertUtils.isTrue(EnableStatusEnum.isEnabled(deptPO.getStatus()), "department is disabled");
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        AssertUtils.notBlank(username, "user.username.cannot.be.blank");

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<UserPO>();
        wrapper.eq(UserPO::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    private void checkPhoneUnique(String phoneNumber, Long excludeId) {
        if (StrUtil.isBlank(phoneNumber)) {
            return;
        }
        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<UserPO>()
                .eq(UserPO::getPhoneNumber, phoneNumber);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }
        AssertUtils.isTrue(userMapper.selectCount(wrapper) == 0, "phone number already exists");
    }

    private Map<Long, String> buildDeptNameMap(Collection<Long> deptIds) {
        List<Long> validDeptIds = deptIds == null ? List.of() : deptIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (validDeptIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<DeptPO> deptList = deptMapper.selectByIds(validDeptIds);
        Map<Long, String> result = new HashMap<>();
        deptList.forEach(item -> result.put(item.getId(), item.getDeptName()));
        return result;
    }

    private void applyDeptName(UserDTO userDTO, Map<Long, String> deptNameMap) {
        if (userDTO == null || userDTO.getDeptId() == null || deptNameMap == null) {
            return;
        }
        userDTO.setDeptName(deptNameMap.get(userDTO.getDeptId()));
    }
}