package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.YesNoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台会员详情展示对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AdminMemberDetailVO", description = "后台会员详情展示对象")
public class AdminMemberDetailVO extends MemberInfoVO {

    /**
     * 是否已绑定微信。
     */
    private Boolean wechatBound;

    /**
     * 状态原因。
     */
    private String statusReason;

    /**
     * 状态到期时间。
     */
    private LocalDateTime statusExpireTime;

    /**
     * 是否已注销。
     */
    private Boolean cancelled;

    /**
     * 注销时间。
     */
    private LocalDateTime cancelTime;

    /**
     * 禁用时间。
     */
    private LocalDateTime disabledTime;

    /**
     * 注册来源。
     */
    private String registerSource;

    /**
     * 注册设备。
     */
    private String registerDevice;

    /**
     * 风险等级。
     */
    private Integer riskLevel;

    /**
     * 黑名单标记。
     */
    private YesNoEnum blacklistFlag;

    /**
     * 社交绑定信息。
     */
    private List<MemberSocialBindingVO> socialBindings;

    /**
     * 管理记录摘要。
     */
    private List<MemberManageRecordVO> manageRecordSummary;

    /**
     * 登录记录摘要。
     */
    private List<MemberLoginAuditVO> loginAuditSummary;
}