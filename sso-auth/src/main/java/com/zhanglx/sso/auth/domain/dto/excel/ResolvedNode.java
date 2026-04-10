package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/27 10:02
 * @ClassName: ResolvedNode
 * @Description: ResolvedNode 类型定义。
 */
@Data
@AllArgsConstructor
public class ResolvedNode {

    /**
     * ID。
     */
    private Long id;
    /**
     * 父级ID。
     */
    private Long parentId;
    /**
     * 血缘。
     */
    private String lineage;

}
