package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.domain.po.DeptPO;
import com.zhanglx.sso.auth.domain.po.SysUserSocialPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.enums.*;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.*;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.runtime.AuthSecurityConfigService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.IUserDomainMapper;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * 用户映射器。
     */
    private final UserMapper userMapper;
    /**
     * 部门映射器。
     */
    private final DeptMapper deptMapper;
    /**
     * 系统用户社交账号映射器。
     */
    private final SysUserSocialMapper sysUserSocialMapper;
    /**
     * 用户应用映射器。
     */
    private final UserAppMapper userAppMapper;
    /**
     * 用户岗位映射器。
     */
    private final UserPostMapper userPostMapper;
    /**
     * 用户角色关系映射器。
     */
    private final UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;
    /**
     * Argon2 密码编码器。
     */
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    /**
     * 操作保护组件。
     */
    private final AuthOperationGuard authOperationGuard;
    /**
     * 认证安全配置服务。
     */
    private final AuthSecurityConfigService authSecurityConfigService;

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
        AssertUtils.notBlank(openId, UserErrorCode.USER_OPEN_ID_REQUIRED);
        validateDept(user.getDeptId());
        checkUsernameUnique(user.getUsername(), null);
        checkPhoneUnique(user.getPhoneNumber(), null);

        UserPO userPO = IUserDomainMapper.INSTANCE.toPO(user);
        fillDefaultUserFields(userPO, UserTypeEnum.MEMBER);
        userMapper.insert(userPO);

        SysUserSocialPO existSocial = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, SocialIdentityTypeEnum.WECHAT_OPEN,
                SysUserSocialPO::getIdentifier, openId
        );
        if (existSocial == null) {
            SysUserSocialPO userSocialPO = SysUserSocialPO.builder()
                    .userId(userPO.getId())
                    .identityType(SocialIdentityTypeEnum.WECHAT_OPEN)
                    .identifier(openId)
                    .build();
            sysUserSocialMapper.insert(userSocialPO);
        } else {
            sysUserSocialMapper.update(
                    null,
                    new LambdaUpdateWrapper<SysUserSocialPO>()
                            .eq(SysUserSocialPO::getId, existSocial.getId())
                            .set(SysUserSocialPO::getUserId, userPO.getId())
            );
        }

        UserDTO result = IUserDomainMapper.INSTANCE.toDTO(userPO);
        result.setOpenId(openId);
        applyDeptName(result, buildDeptNameMap(Collections.singleton(userPO.getDeptId())));
        return result;
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        AssertUtils.notBlank(username, UserErrorCode.USERNAME_REQUIRED);
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
        AssertUtils.notNull(userInfo.getId(), AuthManageErrorCode.USER_ID_REQUIRED);
        validateDept(userInfo.getDeptId());
        checkPhoneUnique(userInfo.getPhoneNumber(), userInfo.getId());

        UserPO userPO = userMapper.selectById(userInfo.getId());
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userInfo.getId());

        userPO.setNickname(userInfo.getNickname());
        userPO.setAvatar(userInfo.getAvatar());
        userPO.setPhoneNumber(userInfo.getPhoneNumber());
        userPO.setSex(userInfo.getSex());
        userPO.setBirthday(userInfo.getBirthday());
        userPO.setEmail(userInfo.getEmail());
        userPO.setDeptId(userInfo.getDeptId());
        userPO.setAllowConcurrentLogin(userInfo.getAllowConcurrentLogin());
        UserPO updatePO = new UserPO();
        updatePO.setId(userInfo.getId());
        updatePO.setNickname(userInfo.getNickname());
        updatePO.setAvatar(userInfo.getAvatar());
        updatePO.setPhoneNumber(userInfo.getPhoneNumber());
        updatePO.setSex(userInfo.getSex());
        updatePO.setBirthday(userInfo.getBirthday());
        updatePO.setEmail(userInfo.getEmail());
        updatePO.setDeptId(userInfo.getDeptId());
        updatePO.setAllowConcurrentLogin(userInfo.getAllowConcurrentLogin());
        userMapper.updateById(updatePO);
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
        AssertUtils.notEmpty(userIds, AuthManageErrorCode.USER_IDS_EMPTY);
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
        AssertUtils.notBlank(openId, UserErrorCode.USER_OPEN_ID_REQUIRED);
        SysUserSocialPO userSocialPO = sysUserSocialMapper.selectOne(
                SysUserSocialPO::getIdentityType, SocialIdentityTypeEnum.WECHAT_OPEN,
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
    public void updateStatus(Long userId, UserStatusEnum status) {
        AssertUtils.notNull(userId, UserErrorCode.USER_INFO_NOT_FOUND);
        AssertUtils.notNull(status, AuthManageErrorCode.USER_STATUS_REQUIRED);
        if (UserStatusEnum.DISABLED.matches(status)) {
            authOperationGuard.checkDisableUserNotSelf(userId);
        }

        UserPO userPO = userMapper.selectById(userId);
        AssertUtils.notNull(userPO, UserErrorCode.USER_NOT_FOUND, userId);
        if (!userPO.getStatus().equals(status)) {
            userMapper.update(
                    null,
                    new LambdaUpdateWrapper<UserPO>()
                            .eq(UserPO::getId, userId)
                            .set(UserPO::getStatus, status)
            );
        }

        if (UserStatusEnum.DISABLED.matches(status)) {
            StpUtil.logout(userPO.getId());
        }
    }

    /**
     * 填充默认字段值。
     */
    private void fillDefaultUserFields(UserPO userPO, UserTypeEnum userType) {
        String rawPassword = StrUtil.isBlank(userPO.getPassword()) ? authSecurityConfigService.getDefaultPassword() : userPO.getPassword();
        userPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword));

        if (userPO.getStatus() == null) {
            userPO.setStatus(UserStatusEnum.NORMAL);
        }
        if (userPO.getAllowConcurrentLogin() == null) {
            userPO.setAllowConcurrentLogin(YesNoEnum.YES);
        }
        if (userPO.getUserType() == null) {
            userPO.setUserType(userType);
        }
    }

    /**
     * 校验部门是否存在且可用。
     */
    private void validateDept(Long deptId) {
        if (deptId == null) {
            return;
        }
        DeptPO deptPO = deptMapper.selectById(deptId);
        AssertUtils.notNull(deptPO, AuthManageErrorCode.USER_DEPT_NOT_FOUND, deptId);
        AssertUtils.isTrue(EnableStatusEnum.isEnabled(deptPO.getStatus()), AuthManageErrorCode.USER_DEPT_DISABLED, deptPO.getDeptName());
    }

/**
 * 执行内部校验逻辑。
 */
    /**
     * 校验用户名是否唯一。
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        AssertUtils.notBlank(username, UserErrorCode.USERNAME_REQUIRED);

        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<UserPO>();
        wrapper.eq(UserPO::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

/**
 * 执行内部校验逻辑。
 */
    /**
     * 校验手机号是否唯一。
     */
    private void checkPhoneUnique(String phoneNumber, Long excludeId) {
        if (StrUtil.isBlank(phoneNumber)) {
            return;
        }
        LambdaQueryWrapperX<UserPO> wrapper = new LambdaQueryWrapperX<UserPO>()
                .eq(UserPO::getPhoneNumber, phoneNumber);
        if (excludeId != null) {
            wrapper.ne(UserPO::getId, excludeId);
        }
        AssertUtils.isTrue(userMapper.selectCount(wrapper) == 0, AuthManageErrorCode.USER_PHONE_ALREADY_EXISTS, phoneNumber);
    }

    /**
     * 处理内部辅助逻辑。
     */
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

    /**
     * 将计算结果应用到目标对象。
     */
    private void applyDeptName(UserDTO userDTO, Map<Long, String> deptNameMap) {
        if (userDTO == null || userDTO.getDeptId() == null || deptNameMap == null) {
            return;
        }

        userDTO.setDeptName(deptNameMap.get(userDTO.getDeptId()));
    }
}
