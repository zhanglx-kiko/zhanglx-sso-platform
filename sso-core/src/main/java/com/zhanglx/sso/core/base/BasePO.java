package com.zhanglx.sso.core.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:08
 * @ClassName: BasePO
 * @Description: 数据库实体基类 包含：雪花ID、审计字段、逻辑删除
 */
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
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间 (自动填充)
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人ID (自动填充)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 修改时间 (自动填充)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 (0-未删, 1-已删)
     * 这里的 select=false 意味着默认查询不返回该字段给前端，保护数据结构
     */
    @TableLogic(value = "0", delval = "1")
    @TableField(select = false)
    @JsonIgnore // JSON序列化时忽略
    private Integer delFlag;

    public BasePO() {
    }

    public BasePO(Long id, Long createBy, LocalDateTime createTime, Long updateBy, LocalDateTime updateTime, Integer delFlag) {
        this.id = id;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateBy = updateBy;
        this.updateTime = updateTime;
        this.delFlag = delFlag;
    }

    public Long getId() {
        return id;
    }

    public BasePO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public BasePO setCreateBy(Long createBy) {
        this.createBy = createBy;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public BasePO setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public BasePO setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public BasePO setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public BasePO setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BasePO basePO = (BasePO) o;
        return Objects.equals(id, basePO.id) && Objects.equals(createBy, basePO.createBy) && Objects.equals(createTime, basePO.createTime) && Objects.equals(updateBy, basePO.updateBy) && Objects.equals(updateTime, basePO.updateTime) && Objects.equals(delFlag, basePO.delFlag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createBy, createTime, updateBy, updateTime, delFlag);
    }

    @Override
    public String toString() {
        return "BasePO{" +
                "id=" + id +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                '}';
    }

}
