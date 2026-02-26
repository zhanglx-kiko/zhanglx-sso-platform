package com.zhanglx.sso.common.base;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
public class BaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    public BaseDTO() {
    }

    public BaseDTO(Long id, Long createBy, LocalDateTime createTime, Long updateBy, LocalDateTime updateTime) {
        this.id = id;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateBy = updateBy;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public BaseDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public BaseDTO setCreateBy(Long createBy) {
        this.createBy = createBy;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public BaseDTO setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public BaseDTO setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public BaseDTO setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseDTO baseDTO = (BaseDTO) o;
        return Objects.equals(id, baseDTO.id) && Objects.equals(createBy, baseDTO.createBy) && Objects.equals(createTime, baseDTO.createTime) && Objects.equals(updateBy, baseDTO.updateBy) && Objects.equals(updateTime, baseDTO.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createBy, createTime, updateBy, updateTime);
    }

    @Override
    public String toString() {
        return "BaseDTO{" +
                "id=" + id +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                '}';
    }
}
