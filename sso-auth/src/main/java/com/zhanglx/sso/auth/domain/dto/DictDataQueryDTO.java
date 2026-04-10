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
 * 字典数据分页查询参数对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DictDataQueryDTO", description = "DictData分页查询参数")
public class DictDataQueryDTO extends PageQuery {

    /**
     * 字典类型编码。
     */
    @Schema(description = "字典类型编码")
    private String dictType;

    /**
     * 字典标签。
     */
    @Schema(description = "字典标签")
    private String dictLabel;

    /**
     * 字典值。
     */
    @Schema(description = "字典值")
    private String dictValue;

    /**
     * 状态：1-启用，0-停用。
     */
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;
}