package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 字典类型分页查询参数对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DictTypeQueryDTO", description = "字典类型分页查询参数")
public class DictTypeQueryDTO extends PageQuery {

    /**
     * 字典名称。
     */
    @Schema(description = "字典名称")
    private String dictName;

    /**
     * 字典类型编码。
     */
    @Schema(description = "字典类型编码")
    private String dictType;

    /**
     * 状态：1-启用，0-停用。
     */
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;
}
