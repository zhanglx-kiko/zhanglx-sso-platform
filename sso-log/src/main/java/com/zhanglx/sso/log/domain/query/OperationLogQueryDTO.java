package com.zhanglx.sso.log.domain.query;

import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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

    /**
     * 应用编码。
     */
    @Schema(description = "应用编码")
    private String appCode;

    /**
     * 平台编码。
     */
    @Schema(description = "平台编码")
    private String platformCode;

    /**
     * 模块名称。
     */
    @Schema(description = "模块")
    private String module;

    /**
     * 功能名称。
     */
    @Schema(description = "功能")
    private String feature;

    /**
     * 用户标识。
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户名。
     */
    @Schema(description = "username")
    private String username;

    /**
     * 操作类型。
     */
    @Schema(description = "操作类型")
    private String operationType;

    /**
     * 结果状态。
     */
    @Schema(description = "结果状态")
    private String resultStatus;

    /**
     * 链路追踪标识。
     */
    @Schema(description = "链路追踪ID")
    private String traceId;

    /**
     * keyword。
     */
    @Schema(description = "关键字，模糊匹配 operationDesc / requestPath")
    private String keyword;

    /**
     * 开始时间。
     */
    @Schema(description = "开始时间")
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    @Schema(description = "结束时间")
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    /**
     * sortOrder。
     */
    @Schema(description = "排序方向，默认 desc")
    private String sortOrder;

    /**
     * searchAfterToken。
     */
    @Schema(description = "深分页令牌，命中大页翻页时优先使用")
    private String searchAfterToken;
}