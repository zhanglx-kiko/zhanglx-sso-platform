package com.zhanglx.sso.auth.domain.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.page.BasePageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:27
 * @ClassName: UserQueryDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends BasePageQuery {

    private String username;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long deptId;

}
