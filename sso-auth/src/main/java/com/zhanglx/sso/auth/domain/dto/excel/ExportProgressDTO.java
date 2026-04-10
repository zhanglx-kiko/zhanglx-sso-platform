package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.Builder;
import lombok.Data;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/27 16:22
 * 类名：ExportProgressDTO
 * 说明：导出任务进度对象。
 */
@Data
@Builder
public class ExportProgressDTO {

    /**
     * 任务标识。
     */
    private String taskId;
    /**
     * 导出总行数。
     */
    private Long totalRows;      // 导出总行数 (方便前端计算百分比)
    /**
     * 已处理行数。
     */
    private Long processedRows;  // 已处理行数
    /**
     * 当前任务状态。
     */
    private String status;       // 状态: 生成中、已完成、失败
    /**
     * 导出文件下载地址。
     */
    private String fileUrl;      // 最终生成的 Excel 下载地址
    /**
     * 失败提示信息。
     */
    private String errorMsg;     // 失败时的错误信息

}
