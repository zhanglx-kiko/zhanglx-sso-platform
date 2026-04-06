package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDate;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:31
 * @ClassName: UserDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserBaseDTO", description = "用户基础信息（可修改）")
public class UserBaseDTO extends BaseDTO {

    @Size(min = 1, max = 50, message = "用户名称长度不能超过50个字符")
    @Schema(description = "用户名称", name = "nickname", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String nickname;

    @Schema(description = "头像URL", name = "avatar", accessMode = Schema.AccessMode.READ_WRITE)
    private String avatar;

    @Schema(description = "手机号", name = "phoneNumber", accessMode = Schema.AccessMode.READ_WRITE)
    private String phoneNumber;

    @Min(value = 0, message = "性别只能为0(未知)、1(男)、2(女)")
    @Max(value = 2, message = "性别只能为0(未知)、1(男)、2(女)")
    @Schema(description = "性别", name = "sex", example = "0未知 1男 2女", defaultValue = "0", allowableValues = {"0", "1", "2"}, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer sex;

    @Schema(description = "生日", name = "birthday", type = "LocalDate", accessMode = Schema.AccessMode.READ_WRITE)
    private LocalDate birthday;

    @Schema(description = "邮箱", name = "email", accessMode = Schema.AccessMode.READ_WRITE)
    private String email;

    @Min(value = 0, message = "并发登录配置只能为0(禁止)、1(允许)")
    @Max(value = 1, message = "并发登录配置只能为0(禁止)、1(允许)")
    @Schema(description = "是否允许并发登录", name = "allowConcurrentLogin", example = "0禁止(会顶号) 1允许(默认)", defaultValue = "1", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer allowConcurrentLogin;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门ID", name = "deptId", type = "String", accessMode = Schema.AccessMode.READ_ONLY)
    private Long deptId;

    @Schema(description = "部门名称", name = "deptName", accessMode = Schema.AccessMode.READ_ONLY)
    private String deptName;

}
