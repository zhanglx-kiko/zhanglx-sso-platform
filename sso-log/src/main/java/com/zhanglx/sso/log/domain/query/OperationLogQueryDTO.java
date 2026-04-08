package com.zhanglx.sso.log.domain.query;

import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

/**
 * 操作日志后台查询条件。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "OperationLogQueryDTO", description = "操作日志查询条件")
public class OperationLogQueryDTO extends PageQuery {

    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "平台编码")
    private String platformCode;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "功能")
    private String feature;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "结果状态")
    private String resultStatus;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "关键字，模糊匹配 operationDesc / requestPath")
    private String keyword;

    @Schema(description = "开始时间")
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    @Schema(description = "排序方向，默认 desc")
    private String sortOrder;

    @Schema(description = "深分页令牌，命中大页翻页时优先使用")
    private String searchAfterToken;
}
