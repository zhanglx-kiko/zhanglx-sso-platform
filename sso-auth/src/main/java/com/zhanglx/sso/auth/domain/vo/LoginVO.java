package com.zhanglx.sso.auth.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 21:12
 * @ClassName: LoginVO
 * @Description:
 */
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户主键ID (String类型，防止前端精度丢失)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID (String类型)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * Token 名称 (例如: satoken)
     */
    private String tokenName;

    /**
     * Token 值
     */
    private String tokenValue;

    public LoginVO() {
    }

    public LoginVO(Long id, String username, String nickname, String avatar, Long deptId, String tokenName, String tokenValue) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.deptId = deptId;
        this.tokenName = tokenName;
        this.tokenValue = tokenValue;
    }

    public Long getId() {
        return id;
    }

    public LoginVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public LoginVO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public LoginVO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public LoginVO setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Long getDeptId() {
        return deptId;
    }

    public LoginVO setDeptId(Long deptId) {
        this.deptId = deptId;
        return this;
    }

    public String getTokenName() {
        return tokenName;
    }

    public LoginVO setTokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public LoginVO setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LoginVO loginVO = (LoginVO) o;
        return Objects.equals(id, loginVO.id) && Objects.equals(username, loginVO.username) && Objects.equals(nickname, loginVO.nickname) && Objects.equals(avatar, loginVO.avatar) && Objects.equals(deptId, loginVO.deptId) && Objects.equals(tokenName, loginVO.tokenName) && Objects.equals(tokenValue, loginVO.tokenValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, nickname, avatar, deptId, tokenName, tokenValue);
    }

    @Override
    public String toString() {
        return "LoginVO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", deptId=" + deptId +
                ", tokenName='" + tokenName + '\'' +
                ", tokenValue='" + tokenValue + '\'' +
                '}';
    }
}
