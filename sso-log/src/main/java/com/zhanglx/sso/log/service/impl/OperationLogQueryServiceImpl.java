package com.zhanglx.sso.log.service.impl;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.log.config.OperationLogProperties;
import com.zhanglx.sso.log.domain.query.OperationLogQueryDTO;
import com.zhanglx.sso.log.domain.vo.OperationLogPageVO;
import com.zhanglx.sso.log.domain.vo.OperationLogVO;
import com.zhanglx.sso.log.infrastructure.es.OperationLogElasticsearchClient;
import com.zhanglx.sso.log.service.OperationLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志查询服务。
 */
@Service
@RequiredArgsConstructor
public class OperationLogQueryServiceImpl implements OperationLogQueryService {

    private final OperationLogProperties properties;
    private final OperationLogElasticsearchClient elasticsearchClient;

    @Override
    public OperationLogPageVO pageQuery(OperationLogQueryDTO queryDTO) {
        if (queryDTO == null) {
            return OperationLogPageVO.builder().build();
        }
        int from = Math.max(0, (queryDTO.getPageNum() - 1) * queryDTO.getPageSize());
        if (!StringUtils.hasText(queryDTO.getSearchAfterToken()) && from + queryDTO.getPageSize() > properties.getSearchMaxWindow()) {
            throw BusinessException.badRequest("深分页请改用 searchAfterToken");
        }
        return elasticsearchClient.pageQuery(queryDTO);
    }

    @Override
    public OperationLogVO getDetail(String logId, OperationLogQueryDTO queryDTO) {
        return elasticsearchClient.getDetail(logId, queryDTO == null ? new OperationLogQueryDTO() : queryDTO);
    }
}
