package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.GenderEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserBaseDTO", description = "用户基础信息")
public class UserBaseDTO extends BaseDTO {

    @Size(min = 1, max = 50, message = "用户昵称长度不能超过50个字符")
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "性别")
    private GenderEnum sex;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "是否允许并发登录")
    private YesNoEnum allowConcurrentLogin;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门ID", type = "String")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;
}