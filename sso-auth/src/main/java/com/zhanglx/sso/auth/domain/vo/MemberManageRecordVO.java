package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.MemberManageActionTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 会员管理记录展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberManageRecordVO", description = "会员管理记录展示对象")
public class MemberManageRecordVO {

    /**
     * 记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long memberId;

    /**
     * 管理动作。
     */
    private MemberManageActionTypeEnum actionType;

    /**
     * 变更前状态。
     */
    private UserStatusEnum beforeStatus;

    /**
     * 变更后状态。
     */
    private UserStatusEnum afterStatus;

    /**
     * 原因。
     */
    private String reason;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 到期时间。
     */
    private LocalDateTime expireTime;

    /**
     * 操作人 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long operatorId;

    /**
     * 操作人名称。
     */
    private String operatorName;

    /**
     * 审批人 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long approveBy;

    /**
     * 审批时间。
     */
    private LocalDateTime approveTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}