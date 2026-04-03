package com.zhanglx.sso.core.domain.dto;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:08
 * @ClassName: BasePO
 * @Description: 数据库实体基类 包含：雪花ID、审计字段、逻辑删除
 */
// 在 Lombok 的规范中，如果子类（UserDTO）使用了 @SuperBuilder，那么它的所有父类（BaseDTO）必须、绝对也只能使用 @SuperBuilder。
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(name = "BaseDTO", description = "数据库实体基类")
public class BaseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "ID", name = "id", type = "String", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "创建人ID", name = "createBy", type = "String", accessMode = Schema.AccessMode.READ_ONLY)
    private Long createBy;

    @Schema(description = "创建时间", name = "createTime", type = "LocalDateTime", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "修改人ID", name = "updateBy", type = "String", accessMode = Schema.AccessMode.READ_ONLY)
    private Long updateBy;

    @Schema(description = "更新时间", name = "updateTime", type = "LocalDateTime", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateTime;

}
