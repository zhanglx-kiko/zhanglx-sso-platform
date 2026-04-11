package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.SocialIdentityTypeEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 会员社交绑定展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberSocialBindingVO", description = "会员社交绑定展示对象")
public class MemberSocialBindingVO {

    /**
     * 绑定记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long memberId;

    /**
     * 身份类型。
     */
    private SocialIdentityTypeEnum identityType;

    /**
     * 第三方标识。
     */
    private String identifier;

    /**
     * 联合标识。
     */
    private String unionId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}