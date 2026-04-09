package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberUpdateDTO", description = "会员资料更新参数")
public class MemberUpdateDTO {

    @Size(max = 64, message = "会员昵称长度不能超过64个字符")
    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "小张")
    private String nickname;

    @Size(max = 255, message = "会员头像地址长度不能超过255个字符")
    @Schema(description = "头像地址", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "https://cdn.example.com/avatar.png")
    private String avatar;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private GenderEnum sex;

    @Schema(description = "生日", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2000-01-01")
    private LocalDate birthday;

    @Size(max = 128, message = "会员邮箱长度不能超过128个字符")
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "demo@example.com")
    private String email;

    @Size(max = 4096, message = "会员扩展资料长度不能超过4096个字符")
    @Schema(description = "扩展资料 JSON 字符串", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "{\"city\":\"Shanghai\"}")
    private String profileExtra;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{member.phone.invalid}")
    @Schema(
            description = "手机号字段仅保留兼容，实际修改请调用绑定手机号接口",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "13800138000"
    )
    private String phoneNumber;
}
