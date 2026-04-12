package com.zhanglx.sso.horticulturalplants.remote.auth;

import com.zhanglx.sso.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "authMemberFeignClient", url = "${sso.remote.auth-base-url:http://localhost:21900}")
public interface AuthMemberFeignClient {

    @GetMapping("/apis/v1/auth/internal/members/current/basic")
    Result<AuthMemberBasicVO> getCurrentMemberBasic();

    @PostMapping("/apis/v1/auth/internal/members/basic/query")
    Result<List<AuthMemberBasicVO>> queryMemberBasics(@RequestBody AuthMemberBasicBatchQueryDTO queryDTO);
}
