package com.zhanglx.sso.log.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ES 分页结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperationLogPageVO", description = "操作日志分页结果")
public class OperationLogPageVO {

    @Schema(description = "当前页数据")
    private List<OperationLogVO> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页大小")
    private long size;

    @Schema(description = "下一页 search_after 令牌")
    private String nextSearchAfterToken;
}
