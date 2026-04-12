package com.zhanglx.sso.horticulturalplants.service.support.impl;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.common.result.Result;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.horticulturalplants.remote.auth.AuthMemberBasicBatchQueryDTO;
import com.zhanglx.sso.horticulturalplants.remote.auth.AuthMemberBasicVO;
import com.zhanglx.sso.horticulturalplants.remote.auth.AuthMemberFeignClient;
import com.zhanglx.sso.horticulturalplants.service.support.PlantMemberRemoteService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantMemberRemoteServiceImpl implements PlantMemberRemoteService {

    private final AuthMemberFeignClient authMemberFeignClient;

    @Override
    public AuthMemberBasicVO getCurrentMemberBasic() {
        try {
            return unwrapRequired(authMemberFeignClient.getCurrentMemberBasic());
        } catch (FeignException.Unauthorized e) {
            throw BusinessException.unauthorized("login.required");
        } catch (FeignException.NotFound e) {
            throw BusinessException.notFound("plant.member.not.found");
        } catch (FeignException e) {
            log.error("Query current member basic info from sso-auth failed: status={}, body={}", e.status(), e.contentUTF8(), e);
            throw BusinessException.badGateway("plant.member.remote.failed");
        }
    }

    @Override
    public Map<Long, AuthMemberBasicVO> queryMemberBasicMap(Collection<Long> memberIds) {
        List<Long> normalizedIds = memberIds == null ? List.of() : memberIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<AuthMemberBasicVO> members = unwrapList(authMemberFeignClient.queryMemberBasics(
                    AuthMemberBasicBatchQueryDTO.builder().memberIds(normalizedIds).build()));
            if (CollectionUtils.isEmpty(members)) {
                return Collections.emptyMap();
            }
            Map<Long, AuthMemberBasicVO> result = new LinkedHashMap<>();
            for (AuthMemberBasicVO member : members) {
                if (member != null && member.getId() != null) {
                    result.put(member.getId(), member);
                }
            }
            return result;
        } catch (FeignException e) {
            log.error("Batch query member basics from sso-auth failed: status={}, body={}", e.status(), e.contentUTF8(), e);
            throw BusinessException.badGateway("plant.member.remote.failed");
        }
    }

    private AuthMemberBasicVO unwrapRequired(Result<AuthMemberBasicVO> result) {
        AuthMemberBasicVO data = unwrapData(result);
        if (data == null) {
            throw BusinessException.notFound("plant.member.not.found");
        }
        return data;
    }

    private List<AuthMemberBasicVO> unwrapList(Result<List<AuthMemberBasicVO>> result) {
        return Optional.ofNullable(unwrapData(result)).orElse(Collections.emptyList());
    }

    private <T> T unwrapData(Result<T> result) {
        if (result == null) {
            throw BusinessException.badGateway("plant.member.remote.failed");
        }
        if (!ResultCode.SUCCESS.getCode().equals(result.getCode())) {
            log.warn("sso-auth remote result returned non-success: code={}, msg={}", result.getCode(), result.getMsg());
            if (ResultCode.UNAUTHORIZED.getCode().equals(result.getCode())) {
                throw BusinessException.unauthorized("login.required");
            }
            if (ResultCode.NOT_FOUND.getCode().equals(result.getCode())) {
                throw BusinessException.notFound("plant.member.not.found");
            }
            throw BusinessException.badGateway("plant.member.remote.failed");
        }
        return result.getData();
    }
}
