package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/26 17:19
 * @ClassName: ImportProgressDTO
 * @Description: 进度缓存对象 (保存到 Redis)
 */

@Data
@Builder
public class ImportProgressDTO {

    /**
     * 任务ID。
     */
    private String taskId;
    private Integer processedRows; // 已处理总行数 (包含成功和失败)
    private Integer successCount;  // 成功入库数
    private Integer failCount;     // 失败数
    private String status;         // 状态: PROCESSING(处理中), COMPLETED(已完成), FAILED(致命失败)
    private String errorExcelUrl;  // 最终生成的错误文件下载地址 (如果有错误数据)

}
