package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.MemberManageActionTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 会员后台管理记录持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_member_manage_record", autoResultMap = true)
public class MemberManageRecordPO extends BasePO {

    /**
     * 会员 ID。
     */
    @TableField(value = "member_id", jdbcType = JdbcType.BIGINT)
    private Long memberId;

    /**
     * 动作类型。
     */
    @TableField(value = "action_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private MemberManageActionTypeEnum actionType;

    /**
     * 变更前状态。
     */
    @TableField(value = "before_status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum beforeStatus;

    /**
     * 变更后状态。
     */
    @TableField(value = "after_status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum afterStatus;

    /**
     * 原因。
     */
    @TableField(value = "reason", jdbcType = JdbcType.VARCHAR)
    private String reason;

    /**
     * 备注。
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;

    /**
     * 到期时间。
     */
    @TableField(value = "expire_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime expireTime;

    /**
     * 操作人 ID。
     */
    @TableField(value = "operator_id", jdbcType = JdbcType.BIGINT)
    private Long operatorId;

    /**
     * 操作人名称。
     */
    @TableField(value = "operator_name", jdbcType = JdbcType.VARCHAR)
    private String operatorName;

    /**
     * 审批人。
     */
    @TableField(value = "approve_by", jdbcType = JdbcType.BIGINT)
    private Long approveBy;

    /**
     * 审批时间。
     */
    @TableField(value = "approve_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime approveTime;
}