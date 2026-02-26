package com.zhanglx.sso.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.common.base.BaseDTO;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:31
 * @ClassName: UserDTO
 * @Description:
 */
public class UserDTO extends BaseDTO {

    @NotBlank(message = "姓名不能为空")
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String avatar;

    /**
     * 是否允许并发登录：0-禁止(会顶号)，1-允许(默认)
     */
    private Integer allowConcurrentLogin;

    /**
     * 部门ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private Integer status;

    public UserDTO() {
    }

    public UserDTO(String username, String password, String nickname, String avatar, Integer allowConcurrentLogin, Long deptId, Integer status) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.allowConcurrentLogin = allowConcurrentLogin;
        this.deptId = deptId;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public UserDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserDTO setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public UserDTO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserDTO setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Integer getAllowConcurrentLogin() {
        return allowConcurrentLogin;
    }

    public UserDTO setAllowConcurrentLogin(Integer allowConcurrentLogin) {
        this.allowConcurrentLogin = allowConcurrentLogin;
        return this;
    }

    public Long getDeptId() {
        return deptId;
    }

    public UserDTO setDeptId(Long deptId) {
        this.deptId = deptId;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public UserDTO setStatus(Integer status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(username, userDTO.username) && Objects.equals(password, userDTO.password) && Objects.equals(nickname, userDTO.nickname) && Objects.equals(avatar, userDTO.avatar) && Objects.equals(allowConcurrentLogin, userDTO.allowConcurrentLogin) && Objects.equals(deptId, userDTO.deptId) && Objects.equals(status, userDTO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, password, nickname, avatar, allowConcurrentLogin, deptId, status);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", allowConcurrentLogin=" + allowConcurrentLogin +
                ", deptId=" + deptId +
                ", status=" + status +
                '}';
    }

}
