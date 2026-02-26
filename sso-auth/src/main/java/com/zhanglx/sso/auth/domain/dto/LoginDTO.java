package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.common.base.BaseDTO;

import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:48
 * @ClassName: LoginDTO
 * @Description:
 */
public class LoginDTO extends BaseDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 登录设备标识 (可选，例如: "PC", "APP", "H5")
     * 用于支持多端登录识别
     */
    private String device;

    public LoginDTO() {
    }

    public LoginDTO(String device, String password, String username) {
        this.device = device;
        this.password = password;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public LoginDTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginDTO setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDevice() {
        return device;
    }

    public LoginDTO setDevice(String device) {
        this.device = device;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LoginDTO loginDTO = (LoginDTO) o;
        return Objects.equals(username, loginDTO.username) && Objects.equals(password, loginDTO.password) && Objects.equals(device, loginDTO.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, password, device);
    }

    @Override
    public String toString() {
        return "LoginDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", device='" + device + '\'' +
                '}';
    }

}
