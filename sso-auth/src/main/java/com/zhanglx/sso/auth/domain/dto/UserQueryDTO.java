package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.common.base.BasePageQuery;

import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:27
 * @ClassName: UserQueryDTO
 * @Description:
 */
public class UserQueryDTO extends BasePageQuery {

    private String username;
    private String deptId;

    public UserQueryDTO() {
    }

    public UserQueryDTO(String username, String deptId) {
        this.username = username;
        this.deptId = deptId;
    }

    public UserQueryDTO(Integer pageNum, Integer pageSize, String username, String deptId) {
        super(pageNum, pageSize);
        this.username = username;
        this.deptId = deptId;
    }

    public String getUsername() {
        return username;
    }

    public UserQueryDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getDeptId() {
        return deptId;
    }

    public UserQueryDTO setDeptId(String deptId) {
        this.deptId = deptId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserQueryDTO that = (UserQueryDTO) o;
        return Objects.equals(username, that.username) && Objects.equals(deptId, that.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, deptId);
    }

    @Override
    public String toString() {
        return "UserQueryDTO{" +
                "username='" + username + '\'' +
                ", deptId='" + deptId + '\'' +
                '}';
    }
}
