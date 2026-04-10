package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.Builder;
import lombok.Data;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/26 17:19
 * 类名：ImportProgressDTO
 * 说明：进度缓存对象 (保存到 Redis)
 */
@Data
@Builder
public class ImportProgressDTO {

    /**
     * 任务标识。
     */
    private String taskId;
    /**
     * 已处理总行数。
     */
    private Integer processedRows; // 已处理总行数 (包含成功和失败)
    /**
     * 成功入库数量。
     */
    private Integer successCount;  // 成功入库数
    /**
     * 失败数量。
     */
    private Integer failCount;     // 失败数
    /**
     * 当前任务状态。
     */
    private String status;         // 状态: 处理中、已完成、致命失败
    /**
     * 错误文件下载地址。
     */
    private String errorExcelUrl;  // 最终生成的错误文件下载地址 (如果有错误数据)

}
