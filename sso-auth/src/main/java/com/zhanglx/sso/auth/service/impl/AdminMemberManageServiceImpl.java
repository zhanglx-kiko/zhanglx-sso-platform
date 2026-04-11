package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AdminMemberForceLogoutDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberQueryDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.po.AuthLoginLogPO;
import com.zhanglx.sso.auth.domain.po.MemberManageRecordPO;
import com.zhanglx.sso.auth.domain.po.MemberSocialPO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberDetailVO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberListVO;
import com.zhanglx.sso.auth.domain.vo.MemberLoginAuditVO;
import com.zhanglx.sso.auth.domain.vo.MemberManageRecordVO;
import com.zhanglx.sso.auth.domain.vo.MemberSocialBindingVO;
import com.zhanglx.sso.auth.enums.MemberManageActionTypeEnum;
import com.zhanglx.sso.auth.enums.SocialIdentityTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.auth.exception.MemberErrorCode;
import com.zhanglx.sso.auth.mapper.AuthLoginLogMapper;
import com.zhanglx.sso.auth.mapper.MemberManageRecordMapper;
import com.zhanglx.sso.auth.mapper.MemberSocialMapper;
import com.zhanglx.sso.auth.mapper.MemberUserMapper;
import com.zhanglx.sso.auth.service.AdminMemberManageService;
import com.zhanglx.sso.auth.service.support.AuthLoginAuditSupport;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会员后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminMemberManageServiceImpl implements AdminMemberManageService {

    private static final List<SocialIdentityTypeEnum> WECHAT_IDENTITY_TYPES = List.of(
            SocialIdentityTypeEnum.WECHAT_OPEN,
            SocialIdentityTypeEnum.WX_MINI
    );
    private static final List<String> MEMBER_CLIENT_TYPES = List.of(
            AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD,
            AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_WECHAT
    );
    private static final String WECHAT_BIND_MEMBER_SQL = "select distinct member_id from t_member_social where del_flag = 0 and identity_type in ('WECHAT_OPEN','WX_MINI')";
    private static final int DETAIL_SUMMARY_SIZE = 5;

    /**
     * 会员用户映射器。
     */
    private final MemberUserMapper memberUserMapper;
    /**
     * 会员社交绑定映射器。
     */
    private final MemberSocialMapper memberSocialMapper;
    /**
     * 会员管理记录映射器。
     */
    private final MemberManageRecordMapper memberManageRecordMapper;
    /**
     * 登录审计映射器。
     */
    private final AuthLoginLogMapper authLoginLogMapper;
    /**
     * 登录审计辅助组件。
     */
    private final AuthLoginAuditSupport authLoginAuditSupport;

    @Override
    public Page<AdminMemberListVO> pageQuery(AdminMemberQueryDTO queryDTO) {
        AdminMemberQueryDTO actualQuery = queryDTO == null ? AdminMemberQueryDTO.builder().build() : queryDTO;
        Page<MemberUserPO> page = Page.of(actualQuery.getPageNum(), actualQuery.getPageSize());
        LambdaQueryWrapperX<MemberUserPO> wrapper = buildMemberPageWrapper(actualQuery);
        memberUserMapper.selectPage(page, wrapper);

        Set<Long> wechatBoundMemberIds = listWechatBoundMemberIds(page.getRecords().stream().map(MemberUserPO::getId).toList());
        List<AdminMemberListVO> records = page.getRecords().stream()
                .map(item -> toListVO(item, wechatBoundMemberIds.contains(item.getId())))
                .toList();
        return buildPageResult(page, records);
    }

    @Override
    public AdminMemberDetailVO getDetail(Long memberId) {
        MemberUserPO memberUserPO = getMemberById(memberId);
        List<MemberSocialBindingVO> socialBindings = querySocialBindings(memberId);
        List<MemberManageRecordVO> manageRecordSummary = listLatestManageRecords(memberId, DETAIL_SUMMARY_SIZE);
        List<MemberLoginAuditVO> loginAuditSummary = listLatestLoginAudits(memberId, DETAIL_SUMMARY_SIZE);
        return toDetailVO(memberUserPO, socialBindings, manageRecordSummary, loginAuditSummary);
    }

    @Override
    public List<MemberSocialBindingVO> listSocialBindings(Long memberId) {
        getMemberById(memberId);
        return querySocialBindings(memberId);
    }

    @Override
    public Page<MemberLoginAuditVO> pageLoginAudits(Long memberId, AuthLoginLogQueryDTO queryDTO) {
        getMemberById(memberId);
        AuthLoginLogQueryDTO actualQuery = queryDTO == null ? AuthLoginLogQueryDTO.builder().build() : queryDTO;
        Page<AuthLoginLogPO> page = Page.of(actualQuery.getPageNum(), actualQuery.getPageSize());
        LambdaQueryWrapper<AuthLoginLogPO> wrapper = new LambdaQueryWrapper<AuthLoginLogPO>()
                .eq(AuthLoginLogPO::getUserId, memberId)
                .in(AuthLoginLogPO::getClientType, MEMBER_CLIENT_TYPES)
                .like(StringUtils.hasText(actualQuery.getUsername()), AuthLoginLogPO::getUsername, actualQuery.getUsername())
                .eq(StringUtils.hasText(actualQuery.getEventType()), AuthLoginLogPO::getEventType, trim(actualQuery.getEventType()))
                .eq(StringUtils.hasText(actualQuery.getLoginResult()), AuthLoginLogPO::getLoginResult, trim(actualQuery.getLoginResult()))
                .like(StringUtils.hasText(actualQuery.getLoginIp()), AuthLoginLogPO::getLoginIp, actualQuery.getLoginIp())
                .ge(actualQuery.getStartTime() != null, AuthLoginLogPO::getCreateTime, actualQuery.getStartTime())
                .le(actualQuery.getEndTime() != null, AuthLoginLogPO::getCreateTime, actualQuery.getEndTime())
                .orderByDesc(AuthLoginLogPO::getCreateTime);
        authLoginLogMapper.selectPage(page, wrapper);
        return buildPageResult(page, page.getRecords().stream().map(this::toLoginAuditVO).toList());
    }

    @Override
    public Page<MemberManageRecordVO> pageManageRecords(Long memberId, PageQuery pageQuery) {
        getMemberById(memberId);
        PageQuery actualQuery = pageQuery == null ? new PageQuery() : pageQuery;
        Page<MemberManageRecordPO> page = Page.of(actualQuery.getPageNum(), actualQuery.getPageSize());
        LambdaQueryWrapperX<MemberManageRecordPO> wrapper = new LambdaQueryWrapperX<MemberManageRecordPO>()
                .eq(MemberManageRecordPO::getMemberId, memberId)
                .orderByDesc(MemberManageRecordPO::getCreateTime);
        memberManageRecordMapper.selectPage(page, wrapper);
        return buildPageResult(page, page.getRecords().stream().map(this::toManageRecordVO).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(AdminMemberStatusUpdateDTO dto) {
        prepareStatusCommand(dto, MemberManageActionTypeEnum.DISABLE, UserStatusEnum.DISABLED);
        MemberUserPO memberUserPO = getMemberById(dto.getMemberId());
        UserStatusEnum beforeStatus = currentStatus(memberUserPO);
        AssertUtils.isTrue(beforeStatus.isNormal() || beforeStatus.isFrozen(),
                MemberErrorCode.MEMBER_DISABLE_ONLY_NORMAL_OR_FROZEN,
                beforeStatus.getDescription());

        LocalDateTime now = LocalDateTime.now();
        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberUserPO.getId())
                        .set(MemberUserPO::getStatus, UserStatusEnum.DISABLED)
                        .set(MemberUserPO::getStatusReason, dto.getReason())
                        .set(MemberUserPO::getStatusExpireTime, dto.getExpireTime())
                        .set(MemberUserPO::getDisabledTime, now)
        );
        recordManageAction(memberUserPO.getId(), dto.getActionType(), beforeStatus, dto.getTargetStatus(), dto.getReason(), dto.getRemark(), dto.getExpireTime());
        StpMemberUtil.logout(memberUserPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(AdminMemberStatusUpdateDTO dto) {
        prepareStatusCommand(dto, MemberManageActionTypeEnum.ENABLE, UserStatusEnum.NORMAL);
        MemberUserPO memberUserPO = getMemberById(dto.getMemberId());
        UserStatusEnum beforeStatus = currentStatus(memberUserPO);
        AssertUtils.isTrue(beforeStatus.isDisabled(), MemberErrorCode.MEMBER_ENABLE_ONLY_DISABLED, beforeStatus.getDescription());

        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberUserPO.getId())
                        .set(MemberUserPO::getStatus, UserStatusEnum.NORMAL)
                        .set(MemberUserPO::getStatusReason, null)
                        .set(MemberUserPO::getStatusExpireTime, null)
                        .set(MemberUserPO::getDisabledTime, null)
        );
        recordManageAction(memberUserPO.getId(), dto.getActionType(), beforeStatus, dto.getTargetStatus(), dto.getReason(), dto.getRemark(), dto.getExpireTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(AdminMemberStatusUpdateDTO dto) {
        prepareStatusCommand(dto, MemberManageActionTypeEnum.FREEZE, UserStatusEnum.FROZEN);
        MemberUserPO memberUserPO = getMemberById(dto.getMemberId());
        UserStatusEnum beforeStatus = currentStatus(memberUserPO);
        AssertUtils.isTrue(beforeStatus.isNormal(), MemberErrorCode.MEMBER_FREEZE_ONLY_NORMAL, beforeStatus.getDescription());

        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberUserPO.getId())
                        .set(MemberUserPO::getStatus, UserStatusEnum.FROZEN)
                        .set(MemberUserPO::getStatusReason, dto.getReason())
                        .set(MemberUserPO::getStatusExpireTime, dto.getExpireTime())
        );
        recordManageAction(memberUserPO.getId(), dto.getActionType(), beforeStatus, dto.getTargetStatus(), dto.getReason(), dto.getRemark(), dto.getExpireTime());
        StpMemberUtil.logout(memberUserPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(AdminMemberStatusUpdateDTO dto) {
        prepareStatusCommand(dto, MemberManageActionTypeEnum.UNFREEZE, UserStatusEnum.NORMAL);
        MemberUserPO memberUserPO = getMemberById(dto.getMemberId());
        UserStatusEnum beforeStatus = currentStatus(memberUserPO);
        AssertUtils.isTrue(beforeStatus.isFrozen(), MemberErrorCode.MEMBER_UNFREEZE_ONLY_FROZEN, beforeStatus.getDescription());

        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberUserPO.getId())
                        .set(MemberUserPO::getStatus, UserStatusEnum.NORMAL)
                        .set(MemberUserPO::getStatusReason, null)
                        .set(MemberUserPO::getStatusExpireTime, null)
        );
        recordManageAction(memberUserPO.getId(), dto.getActionType(), beforeStatus, dto.getTargetStatus(), dto.getReason(), dto.getRemark(), dto.getExpireTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceLogout(AdminMemberForceLogoutDTO dto) {
        AssertUtils.notNull(dto, MemberErrorCode.MEMBER_MANAGE_ACTION_TYPE_REQUIRED);
        AssertUtils.notBlank(dto.getReason(), MemberErrorCode.MEMBER_MANAGE_REASON_REQUIRED);
        MemberUserPO memberUserPO = getMemberById(dto.getMemberId());
        UserStatusEnum beforeStatus = currentStatus(memberUserPO);
        StpMemberUtil.logout(memberUserPO.getId());
        recordManageAction(memberUserPO.getId(), MemberManageActionTypeEnum.FORCE_LOGOUT, beforeStatus, beforeStatus, dto.getReason(), dto.getRemark(), null);
    }

    /**
     * 构建后台会员分页查询条件。
     */
    private LambdaQueryWrapperX<MemberUserPO> buildMemberPageWrapper(AdminMemberQueryDTO queryDTO) {
        LambdaQueryWrapperX<MemberUserPO> wrapper = new LambdaQueryWrapperX<MemberUserPO>()
                .eqIfPresent(MemberUserPO::getId, queryDTO.getMemberId())
                .likeIfPresent(MemberUserPO::getPhoneNumber, queryDTO.getPhoneNumber())
                .likeIfPresent(MemberUserPO::getNickname, queryDTO.getNickname())
                .likeIfPresent(MemberUserPO::getEmail, queryDTO.getEmail())
                .eqIfPresent(MemberUserPO::getStatus, queryDTO.getStatus())
                .eqIfPresent(MemberUserPO::getRealNameStatus, queryDTO.getRealNameStatus())
                .eqIfPresent(MemberUserPO::getMemberType, queryDTO.getMemberType())
                .eqIfPresent(MemberUserPO::getUserLevel, queryDTO.getUserLevel())
                .betweenIfPresent(MemberUserPO::getCreateTime, queryDTO.getRegisterStartTime(), queryDTO.getRegisterEndTime())
                .betweenIfPresent(MemberUserPO::getLastLoginTime, queryDTO.getLastLoginStartTime(), queryDTO.getLastLoginEndTime())
                .likeIfPresent(MemberUserPO::getRegisterIp, queryDTO.getRegisterIp())
                .likeIfPresent(MemberUserPO::getLastLoginIp, queryDTO.getLastLoginIp())
                .orderByDesc(MemberUserPO::getCreateTime);

        if (StringUtils.hasText(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(MemberUserPO::getPhoneNumber, queryDTO.getSearchKey())
                    .or()
                    .like(MemberUserPO::getNickname, queryDTO.getSearchKey())
                    .or()
                    .like(MemberUserPO::getEmail, queryDTO.getSearchKey()));
        }

        if (queryDTO.getPhoneBound() != null) {
            if (YesNoEnum.YES.matches(queryDTO.getPhoneBound())) {
                wrapper.and(w -> w.eq(MemberUserPO::getPhoneBound, YesNoEnum.YES)
                        .or()
                        .isNotNull(MemberUserPO::getPhoneNumber));
            } else {
                wrapper.and(w -> w.eq(MemberUserPO::getPhoneBound, YesNoEnum.NO)
                        .or()
                        .isNull(MemberUserPO::getPhoneNumber));
            }
        }

        if (queryDTO.getHasWechatBind() != null) {
            if (YesNoEnum.YES.matches(queryDTO.getHasWechatBind())) {
                wrapper.inSql(MemberUserPO::getId, WECHAT_BIND_MEMBER_SQL);
            } else {
                wrapper.notInSql(MemberUserPO::getId, WECHAT_BIND_MEMBER_SQL);
            }
        }
        return wrapper;
    }

    /**
     * 状态变更前统一补齐动作信息，避免控制器和服务层出现双重来源。
     */
    private void prepareStatusCommand(AdminMemberStatusUpdateDTO dto,
                                      MemberManageActionTypeEnum actionType,
                                      UserStatusEnum targetStatus) {
        AssertUtils.notNull(dto, MemberErrorCode.MEMBER_MANAGE_ACTION_TYPE_REQUIRED);
        AssertUtils.notBlank(dto.getReason(), MemberErrorCode.MEMBER_MANAGE_REASON_REQUIRED);
        validateExpireTime(dto.getExpireTime());
        dto.setActionType(actionType);
        dto.setTargetStatus(targetStatus);
    }

    /**
     * 校验状态截止时间，避免保存已经过期的冻结或禁用窗口。
     */
    private void validateExpireTime(LocalDateTime expireTime) {
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            throw new com.zhanglx.sso.core.exception.BusinessException(MemberErrorCode.MEMBER_STATUS_EXPIRE_TIME_INVALID);
        }
    }

    /**
     * 查询会员基础信息，不存在时抛出标准业务异常。
     */
    private MemberUserPO getMemberById(Long memberId) {
        AssertUtils.notNull(memberId, MemberErrorCode.MEMBER_NOT_FOUND);
        MemberUserPO memberUserPO = memberUserMapper.selectById(memberId);
        AssertUtils.notNull(memberUserPO, MemberErrorCode.MEMBER_NOT_FOUND);
        return memberUserPO;
    }

    /**
     * 统一归一化会员状态，兼容旧数据中可能存在的空值。
     */
    private UserStatusEnum currentStatus(MemberUserPO memberUserPO) {
        return UserStatusEnum.normalize(memberUserPO == null ? null : memberUserPO.getStatus());
    }

    /**
     * 查询会员社交绑定信息。
     */
    private List<MemberSocialBindingVO> querySocialBindings(Long memberId) {
        return memberSocialMapper.selectList(new LambdaQueryWrapperX<MemberSocialPO>()
                        .eq(MemberSocialPO::getMemberId, memberId)
                        .orderByDesc(MemberSocialPO::getCreateTime))
                .stream()
                .map(this::toSocialBindingVO)
                .toList();
    }

    /**
     * 查询当前分页记录里已绑定微信的会员集合，避免逐行查库。
     */
    private Set<Long> listWechatBoundMemberIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptySet();
        }
        return memberSocialMapper.selectList(new LambdaQueryWrapperX<MemberSocialPO>()
                        .inIfPresent(MemberSocialPO::getMemberId, memberIds)
                        .in(MemberSocialPO::getIdentityType, WECHAT_IDENTITY_TYPES))
                .stream()
                .map(MemberSocialPO::getMemberId)
                .collect(Collectors.toSet());
    }

    /**
     * 查询最近的管理记录摘要。
     */
    private List<MemberManageRecordVO> listLatestManageRecords(Long memberId, int limit) {
        return memberManageRecordMapper.selectList(new LambdaQueryWrapperX<MemberManageRecordPO>()
                        .eq(MemberManageRecordPO::getMemberId, memberId)
                        .orderByDesc(MemberManageRecordPO::getCreateTime)
                        .last("limit " + limit))
                .stream()
                .map(this::toManageRecordVO)
                .toList();
    }

    /**
     * 查询最近的登录记录摘要。
     */
    private List<MemberLoginAuditVO> listLatestLoginAudits(Long memberId, int limit) {
        return authLoginLogMapper.selectList(new LambdaQueryWrapper<AuthLoginLogPO>()
                        .eq(AuthLoginLogPO::getUserId, memberId)
                        .in(AuthLoginLogPO::getClientType, MEMBER_CLIENT_TYPES)
                        .orderByDesc(AuthLoginLogPO::getCreateTime)
                        .last("limit " + limit))
                .stream()
                .map(this::toLoginAuditVO)
                .toList();
    }

    /**
     * 写入会员管理记录，确保每次后台干预都有独立审计链路。
     */
    private void recordManageAction(Long memberId,
                                    MemberManageActionTypeEnum actionType,
                                    UserStatusEnum beforeStatus,
                                    UserStatusEnum afterStatus,
                                    String reason,
                                    String remark,
                                    LocalDateTime expireTime) {
        OperatorSnapshot operatorSnapshot = currentOperator();
        LocalDateTime now = LocalDateTime.now();
        MemberManageRecordPO recordPO = MemberManageRecordPO.builder()
                .memberId(memberId)
                .actionType(actionType)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .reason(reason)
                .remark(remark)
                .expireTime(expireTime)
                .operatorId(operatorSnapshot.operatorId())
                .operatorName(operatorSnapshot.operatorName())
                .approveBy(operatorSnapshot.operatorId())
                .approveTime(now)
                .build();
        memberManageRecordMapper.insert(recordPO);
    }

    /**
     * 解析当前后台操作人信息，避免记录表中只留下孤立 ID。
     */
    private OperatorSnapshot currentOperator() {
        Long operatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        AuthLoginAuditSupport.SessionSnapshot snapshot = authLoginAuditSupport.currentAdminSnapshot();
        String operatorName = null;
        if (snapshot != null) {
            operatorName = StringUtils.hasText(snapshot.displayName()) ? snapshot.displayName() : snapshot.username();
            if (operatorId == null) {
                operatorId = snapshot.userId();
            }
        }
        if (!StringUtils.hasText(operatorName) && operatorId != null) {
            operatorName = "admin_" + operatorId;
        }
        return new OperatorSnapshot(operatorId, operatorName);
    }

    /**
     * 转换列表展示对象。
     */
    private AdminMemberListVO toListVO(MemberUserPO memberUserPO, boolean wechatBound) {
        return AdminMemberListVO.builder()
                .id(memberUserPO.getId())
                .phoneNumber(memberUserPO.getPhoneNumber())
                .nickname(memberUserPO.getNickname())
                .avatar(memberUserPO.getAvatar())
                .status(currentStatus(memberUserPO))
                .realNameStatus(memberUserPO.getRealNameStatus())
                .memberType(memberUserPO.getMemberType())
                .userLevel(memberUserPO.getUserLevel())
                .points(memberUserPO.getPoints())
                .phoneBound(resolvePhoneBound(memberUserPO))
                .wechatBound(wechatBound)
                .createTime(memberUserPO.getCreateTime())
                .lastLoginTime(memberUserPO.getLastLoginTime())
                .registerIp(memberUserPO.getRegisterIp())
                .lastLoginIp(memberUserPO.getLastLoginIp())
                .build();
    }

    /**
     * 转换后台会员详情对象。
     */
    private AdminMemberDetailVO toDetailVO(MemberUserPO memberUserPO,
                                           List<MemberSocialBindingVO> socialBindings,
                                           List<MemberManageRecordVO> manageRecordSummary,
                                           List<MemberLoginAuditVO> loginAuditSummary) {
        UserStatusEnum currentStatus = currentStatus(memberUserPO);
        return AdminMemberDetailVO.builder()
                .id(memberUserPO.getId())
                .createBy(memberUserPO.getCreateBy())
                .createTime(memberUserPO.getCreateTime())
                .updateBy(memberUserPO.getUpdateBy())
                .updateTime(memberUserPO.getUpdateTime())
                .phoneNumber(memberUserPO.getPhoneNumber())
                .nickname(memberUserPO.getNickname())
                .avatar(memberUserPO.getAvatar())
                .sex(memberUserPO.getSex())
                .birthday(memberUserPO.getBirthday())
                .email(memberUserPO.getEmail())
                .phoneBound(resolvePhoneBound(memberUserPO))
                .userLevel(memberUserPO.getUserLevel())
                .points(memberUserPO.getPoints())
                .memberType(memberUserPO.getMemberType())
                .realNameStatus(memberUserPO.getRealNameStatus())
                .status(currentStatus)
                .registerIp(memberUserPO.getRegisterIp())
                .lastLoginTime(memberUserPO.getLastLoginTime())
                .lastLoginIp(memberUserPO.getLastLoginIp())
                .profileExtra(memberUserPO.getProfileExtra())
                .wechatBound(socialBindings != null && !socialBindings.isEmpty())
                .statusReason(memberUserPO.getStatusReason())
                .statusExpireTime(memberUserPO.getStatusExpireTime())
                .cancelled(currentStatus.isCancelled() || currentStatus.isCancelling() || memberUserPO.getCancelTime() != null)
                .cancelTime(memberUserPO.getCancelTime())
                .disabledTime(memberUserPO.getDisabledTime())
                .registerSource(memberUserPO.getRegisterSource())
                .registerDevice(memberUserPO.getRegisterDevice())
                .riskLevel(memberUserPO.getRiskLevel())
                .blacklistFlag(memberUserPO.getBlacklistFlag())
                .socialBindings(socialBindings)
                .manageRecordSummary(manageRecordSummary)
                .loginAuditSummary(loginAuditSummary)
                .build();
    }

    /**
     * 转换会员社交绑定对象。
     */
    private MemberSocialBindingVO toSocialBindingVO(MemberSocialPO socialPO) {
        return MemberSocialBindingVO.builder()
                .id(socialPO.getId())
                .memberId(socialPO.getMemberId())
                .identityType(socialPO.getIdentityType())
                .identifier(socialPO.getIdentifier())
                .unionId(socialPO.getUnionId())
                .createTime(socialPO.getCreateTime())
                .updateTime(socialPO.getUpdateTime())
                .build();
    }

    /**
     * 转换会员管理记录对象。
     */
    private MemberManageRecordVO toManageRecordVO(MemberManageRecordPO recordPO) {
        return MemberManageRecordVO.builder()
                .id(recordPO.getId())
                .memberId(recordPO.getMemberId())
                .actionType(recordPO.getActionType())
                .beforeStatus(recordPO.getBeforeStatus())
                .afterStatus(recordPO.getAfterStatus())
                .reason(recordPO.getReason())
                .remark(recordPO.getRemark())
                .expireTime(recordPO.getExpireTime())
                .operatorId(recordPO.getOperatorId())
                .operatorName(recordPO.getOperatorName())
                .approveBy(recordPO.getApproveBy())
                .approveTime(recordPO.getApproveTime())
                .createTime(recordPO.getCreateTime())
                .build();
    }

    /**
     * 转换会员登录审计对象。
     */
    private MemberLoginAuditVO toLoginAuditVO(AuthLoginLogPO entity) {
        return MemberLoginAuditVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .eventType(entity.getEventType())
                .loginResult(entity.getLoginResult())
                .failReason(entity.getFailReason())
                .loginIp(entity.getLoginIp())
                .deviceType(entity.getDeviceType())
                .clientType(entity.getClientType())
                .loginTime(entity.getLoginTime())
                .logoutTime(entity.getLogoutTime())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 统一处理手机号绑定标记，兼容旧数据里还没回填新字段的情况。
     */
    private Boolean resolvePhoneBound(MemberUserPO memberUserPO) {
        if (memberUserPO.getPhoneBound() != null) {
            return YesNoEnum.YES.matches(memberUserPO.getPhoneBound());
        }
        return StringUtils.hasText(memberUserPO.getPhoneNumber());
    }

    /**
     * 组装分页结果，保持与现有项目统一的返回风格。
     */
    private <T> Page<T> buildPageResult(Page<?> source, List<T> records) {
        Page<T> result = new Page<>();
        result.setCurrent(source.getCurrent());
        result.setSize(source.getSize());
        result.setTotal(source.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 裁剪查询字符串，避免把空白字符写进 SQL 条件。
     */
    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record OperatorSnapshot(Long operatorId, String operatorName) {
    }
}