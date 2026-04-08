package com.zhanglx.sso.log.service;

import com.zhanglx.sso.log.domain.command.OperationLogCommand;

/**
 * 统一日志接入客户端。
 * 其他平台可以直接依赖该接口，以代码方式接入操作日志体系。
 */
public interface OperationLogClient {

    void record(OperationLogCommand command);
}
