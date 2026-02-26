package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.core.base.BasePO;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:45
 * @ClassName: UserPO
 * @Description: 用户持久化对象 (对应数据库表)
 */
@TableName("sys_user")
public class UserPO extends BasePO {

    private String username;

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
    private Long deptId;

    private Integer status;

    public UserPO() {
    }

    public UserPO(String username, String password, String nickname, String avatar, Integer allowConcurrentLogin, Long deptId, Integer status) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.allowConcurrentLogin = allowConcurrentLogin;
        this.deptId = deptId;
        this.status = status;
    }

    public UserPO(Long id, Long createBy, LocalDateTime createTime, Long updateBy, LocalDateTime updateTime, Integer delFlag, String username, String password, String nickname, String avatar, Integer allowConcurrentLogin, Long deptId, Integer status) {
        super(id, createBy, createTime, updateBy, updateTime, delFlag);
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

    public UserPO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserPO setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public UserPO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserPO setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Integer getAllowConcurrentLogin() {
        return allowConcurrentLogin;
    }

    public UserPO setAllowConcurrentLogin(Integer allowConcurrentLogin) {
        this.allowConcurrentLogin = allowConcurrentLogin;
        return this;
    }

    public Long getDeptId() {
        return deptId;
    }

    public UserPO setDeptId(Long deptId) {
        this.deptId = deptId;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public UserPO setStatus(Integer status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserPO userPO = (UserPO) o;
        return Objects.equals(username, userPO.username) && Objects.equals(password, userPO.password) && Objects.equals(nickname, userPO.nickname) && Objects.equals(avatar, userPO.avatar) && Objects.equals(allowConcurrentLogin, userPO.allowConcurrentLogin) && Objects.equals(deptId, userPO.deptId) && Objects.equals(status, userPO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, password, nickname, avatar, allowConcurrentLogin, deptId, status);
    }

    @Override
    public String toString() {
        return "UserPO{" +
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
