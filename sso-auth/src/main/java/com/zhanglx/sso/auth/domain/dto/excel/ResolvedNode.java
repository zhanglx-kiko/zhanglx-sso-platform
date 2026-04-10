package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/27 10:02
 * 类名：ResolvedNode
 * 说明：ResolvedNode 类型定义。
 */
@Data
@AllArgsConstructor
public class ResolvedNode {

    /**
     * 标识。
     */
    private Long id;
    /**
     * 父级标识。
     */
    private Long parentId;
    /**
     * 血缘。
     */
    private String lineage;

}