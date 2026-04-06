package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * @Author: Zhang L X
 * @Create: 2026/4/3 12:21
 * @ClassName: UserUpdateDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserUpdateDTO", description = "用户修改信息对象")
public class UserUpdateDTO {

    @Size(min = 1, max = 50, message = "用户名称长度不能超过50个字符")
    @Schema(description = "用户名称", name = "nickname")
    private String nickname;

    @Schema(description = "头像URL", name = "avatar")
    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的中国大陆手机号")
    @Schema(description = "手机号", name = "phoneNumber")
    private String phoneNumber;

    @Min(value = 0, message = "性别只能为0(未知)、1(男)、2(女)")
    @Max(value = 2, message = "性别只能为0(未知)、1(男)、2(女)")
    @Schema(description = "性别", name = "sex", allowableValues = {"0", "1", "2"})
    private Integer sex;

    @Schema(description = "生日", name = "birthday", type = "LocalDate")
    private LocalDate birthday;

    @Schema(description = "邮箱", name = "email")
    private String email;

}
