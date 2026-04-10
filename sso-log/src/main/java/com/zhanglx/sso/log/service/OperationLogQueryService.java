package com.zhanglx.sso.log.service;

import com.zhanglx.sso.log.domain.query.OperationLogQueryDTO;
import com.zhanglx.sso.log.domain.vo.OperationLogPageVO;
import com.zhanglx.sso.log.domain.vo.OperationLogVO;

/**
 * 操作日志查询服务接口。
 */
public interface OperationLogQueryService {

    OperationLogPageVO pageQuery(OperationLogQueryDTO queryDTO);

    OperationLogVO getDetail(String logId, OperationLogQueryDTO queryDTO);
}