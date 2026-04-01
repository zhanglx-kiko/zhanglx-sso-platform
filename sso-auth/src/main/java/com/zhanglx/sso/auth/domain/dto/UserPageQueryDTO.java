package com.zhanglx.sso.auth.domain.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:27
 * @ClassName: UserPageQueryDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserPageQueryDTO", description = "用户查询对象")
public class UserPageQueryDTO extends PageQuery {

    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门id", name = "deptId", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long deptId;

}
