package com.zhanglx.sso.auth.domain.dto.excel;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/27 16:22
 * @ClassName: ExportProgressDTO
 * @Description:
 */
@Data
@Builder
public class ExportProgressDTO {

    private String taskId;
    private Long totalRows;      // 导出总行数 (方便前端计算百分比)
    private Long processedRows;  // 已处理行数
    private String status;       // 状态: PROCESSING(生成中), COMPLETED(已完成), FAILED(失败)
    private String fileUrl;      // 最终生成的 Excel 下载地址
    private String errorMsg;     // 失败时的错误信息

}
