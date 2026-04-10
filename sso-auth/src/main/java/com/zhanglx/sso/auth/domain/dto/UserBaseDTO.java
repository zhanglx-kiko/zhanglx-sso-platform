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

/**
 * 系统用户基础数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserBaseDTO", description = "后台用户基础信息")
public class UserBaseDTO extends BaseDTO {

    /**
     * 用户昵称。
     */
    @Size(min = 1, max = 50, message = "用户昵称长度不能超过50个字符")
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 头像URL。
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 手机号。
     */
    @Schema(description = "手机号")
    private String phoneNumber;

    /**
     * 性别。
     */
    @Schema(description = "性别")
    private GenderEnum sex;

    /**
     * 生日。
     */
    @Schema(description = "生日")
    private LocalDate birthday;

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 是否允许并发登录。
     */
    @Schema(description = "是否允许并发登录")
    private YesNoEnum allowConcurrentLogin;

    /**
     * 部门标识。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门ID", type = "String")
    private Long deptId;

    /**
     * 部门名称。
     */
    @Schema(description = "部门名称")
    private String deptName;
}