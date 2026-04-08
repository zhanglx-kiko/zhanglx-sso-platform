package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.log.domain.query.OperationLogQueryDTO;
import com.zhanglx.sso.log.domain.vo.OperationLogPageVO;
import com.zhanglx.sso.log.domain.vo.OperationLogVO;
import com.zhanglx.sso.log.service.OperationLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志后台接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "操作日志")
@RequestMapping("/apis/v1/auth/s/operation-logs")
public class AuthOperationLogController {

    private final OperationLogQueryService operationLogQueryService;

    @Operation(summary = "分页查询操作日志")
    @PostMapping("/page")
    @SaCheckPermission("operation-log:list")
    public OperationLogPageVO pageQuery(@RequestBody OperationLogQueryDTO queryDTO) {
        return operationLogQueryService.pageQuery(queryDTO);
    }

    @Operation(summary = "查看操作日志详情")
    @GetMapping("/{logId}")
    @SaCheckPermission("operation-log:view")
    public OperationLogVO detail(@PathVariable String logId, OperationLogQueryDTO queryDTO) {
        return operationLogQueryService.getDetail(logId, queryDTO);
    }
}
