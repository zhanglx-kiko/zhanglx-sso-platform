package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.config.LoginLogProperties;
import com.zhanglx.sso.auth.domain.command.AuthLoginLogRecordCommand;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.po.AuthLoginLogPO;
import com.zhanglx.sso.auth.domain.vo.AuthLoginLogVO;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.mapper.AuthLoginLogMapper;
import com.zhanglx.sso.auth.service.AuthLoginLogService;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;

/**
 * 登录日志服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLoginLogServiceImpl implements AuthLoginLogService {

    private final LoginLogProperties properties;
    /**
     * 登录日志映射器。
     */
    private final AuthLoginLogMapper authLoginLogMapper;
    /**
     * loginLogTaskExecutor。
     */
    private final ThreadPoolTaskExecutor loginLogTaskExecutor;
    /**
     * lastRejectLogAt。
     */
    private volatile long lastRejectLogAt;
    /**
     * 最后失败记录时间。
     */
    private volatile long lastFailureLogAt;

    @Override
    public void recordAsync(AuthLoginLogRecordCommand command) {
        if (!properties.isEnabled() || command == null) {
            return;
        }

        try {
            loginLogTaskExecutor.execute(() -> save(command));
        } catch (RejectedExecutionException e) {
            long now = System.currentTimeMillis();
            if (now - lastRejectLogAt > 30_000L) {
                lastRejectLogAt = now;
                log.warn("登录日志线程池已满，当前日志被丢弃，不影响主登录链路");
            }
        }
    }

    @Override
    public Page<AuthLoginLogVO> pageQuery(AuthLoginLogQueryDTO queryDTO) {
        queryDTO = queryDTO == null ? AuthLoginLogQueryDTO.builder().build() : queryDTO;
        Page<AuthLoginLogPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<AuthLoginLogPO> wrapper = new LambdaQueryWrapper<AuthLoginLogPO>()
                .eq(queryDTO.getUserId() != null, AuthLoginLogPO::getUserId, queryDTO.getUserId())
                .like(StringUtils.hasText(queryDTO.getUsername()), AuthLoginLogPO::getUsername, queryDTO.getUsername())
                .eq(StringUtils.hasText(queryDTO.getEventType()), AuthLoginLogPO::getEventType, trim(queryDTO.getEventType()))
                .eq(StringUtils.hasText(queryDTO.getLoginResult()), AuthLoginLogPO::getLoginResult, trim(queryDTO.getLoginResult()))
                .like(StringUtils.hasText(queryDTO.getLoginIp()), AuthLoginLogPO::getLoginIp, queryDTO.getLoginIp())
                .ge(queryDTO.getStartTime() != null, AuthLoginLogPO::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, AuthLoginLogPO::getCreateTime, queryDTO.getEndTime())
                .orderByDesc(AuthLoginLogPO::getCreateTime);
        authLoginLogMapper.selectPage(page, wrapper);

        Page<AuthLoginLogVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    @Override
    public AuthLoginLogVO getDetail(Long id) {
        AuthLoginLogPO loginLogPO = authLoginLogMapper.selectById(id);
        if (loginLogPO == null) {
            throw BusinessException.of(AuthManageErrorCode.LOGIN_LOG_NOT_FOUND);
        }
        return toVO(loginLogPO);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void save(AuthLoginLogRecordCommand command) {
        try {
            AuthLoginLogPO entity = AuthLoginLogPO.builder()
                    .userId(command.getUserId())
                    .username(command.getUsername())
                    .displayName(command.getDisplayName())
                    .eventType(command.getEventType())
                    .loginResult(command.getLoginResult())
                    .failReason(command.getFailReason())
                    .loginIp(command.getLoginIp())
                    .userAgent(command.getUserAgent())
                    .deviceType(command.getDeviceType())
                    .traceId(command.getTraceId())
                    .requestId(command.getRequestId())
                    .clientType(command.getClientType())
                    .appCode(command.getAppCode())
                    .loginTime(command.getLoginTime())
                    .logoutTime(command.getLogoutTime())
                    .createTime(LocalDateTime.now())
                    .extJson(command.getExtJson())
                    .build();
            authLoginLogMapper.insert(entity);
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            if (now - lastFailureLogAt > 30_000L) {
                lastFailureLogAt = now;
                log.error("登录日志落库失败，已忽略对主流程的影响", e);
            }
        }
    }

    /**
     * 转换为视图对象。
     */
    private AuthLoginLogVO toVO(AuthLoginLogPO entity) {
        return AuthLoginLogVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .eventType(entity.getEventType())
                .loginResult(entity.getLoginResult())
                .failReason(entity.getFailReason())
                .loginIp(entity.getLoginIp())
                .userAgent(entity.getUserAgent())
                .deviceType(entity.getDeviceType())
                .traceId(entity.getTraceId())
                .requestId(entity.getRequestId())
                .clientType(entity.getClientType())
                .appCode(entity.getAppCode())
                .loginTime(entity.getLoginTime())
                .logoutTime(entity.getLogoutTime())
                .createTime(entity.getCreateTime())
                .extJson(entity.getExtJson())
                .build();
    }

    /**
     * 裁剪字符串长度，避免超出存储限制。
     */
    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
