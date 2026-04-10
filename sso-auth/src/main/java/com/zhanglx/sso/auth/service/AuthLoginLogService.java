package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.command.AuthLoginLogRecordCommand;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.vo.AuthLoginLogVO;

/**
 * 认证登录日志服务接口。
 */
public interface AuthLoginLogService {

    void recordAsync(AuthLoginLogRecordCommand command);

    Page<AuthLoginLogVO> pageQuery(AuthLoginLogQueryDTO queryDTO);

    AuthLoginLogVO getDetail(Long id);
}