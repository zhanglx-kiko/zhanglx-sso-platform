package com.zhanglx.sso.mybatis.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/11 16:08
 * 类名：BasePO
 * 说明：数据库实体基类 包含：雪花ID、审计字段、逻辑删除
 */
// 在 Lombok 的规范中，如果子类（UserDTO）使用了 @SuperBuilder，那么它的所有父类（BaseDTO）必须、绝对也只能使用 @SuperBuilder。
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BasePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID (雪花算法)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人ID (自动填充)
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间 (自动填充)
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人ID (自动填充)
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 修改时间 (自动填充)
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0 表示未删除，主键值表示已删除）
     * 仅保留 value = "0"，利用 MP 的查询自动注入 WHERE del_flag = 0
     */
    @TableLogic(value = "0")
    @TableField(value = "del_flag", fill = FieldFill.INSERT, select = false)
    private Long delFlag;

}