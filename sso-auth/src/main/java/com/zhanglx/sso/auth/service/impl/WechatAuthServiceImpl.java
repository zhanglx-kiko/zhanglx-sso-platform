package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.po.MemberSocialPO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.properties.WechatProperties;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.enums.SocialIdentityTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.MemberSocialMapper;
import com.zhanglx.sso.auth.mapper.MemberUserMapper;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import com.zhanglx.sso.auth.service.support.AuthLoginAuditSupport;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;

/**
 * WechatAuth服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthServiceImpl implements WechatAuthService {
    /**
     * wechatProperties。
     */
    private final WechatProperties wechatProperties;
    /**
     * 用户服务。
     */
    private final UserService userService;
    /**
     * 会员用户服务。
     */
    private final MemberUserService memberUserService;
    /**
     * 会员用户映射器。
     */
    private final MemberUserMapper memberUserMapper;
    /**
     * 会员社交账号映射器。
     */
    private final MemberSocialMapper memberSocialMapper;
    /**
     * 登录审计支持组件。
     */
    private final AuthLoginAuditSupport authLoginAuditSupport;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;
    /**
     * 请求客户端。
     */
    private final RestClient restClient = RestClient.create();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByWechatCode(String code) {
        JsonNode response = fetchWechatSession(code);
        String openId = response.get("openid").asText();
        UserDTO user = userService.getUserByOpenId(openId);

        if (user == null) {
            user = new UserDTO();
            user.setOpenId(openId);
            user.setUsername(openId);
            user.setNickname("user_" + System.currentTimeMillis() % 10000);
            user = userService.addWxUser(user, openId);
            log.info("Created back-office WeChat account, userId={}", user.getId());
        }

        StpUtil.login(user.getId());
        authLoginAuditSupport.storeAdminSession(
                user.getUsername(),
                StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername(),
                AuthLoginAuditSupport.CLIENT_TYPE_SYS_WECHAT
        );
        return assembleLoginVO(user, StpUtil.getTokenInfo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginMemberByWechatCode(String code) {
        JsonNode response = fetchWechatSession(code);
        String openId = response.get("openid").asText();
        String unionId = response.has("unionid") ? response.get("unionid").asText() : null;

        MemberSocialPO socialPO = memberSocialMapper.selectOne(
                MemberSocialPO::getIdentityType, SocialIdentityTypeEnum.WX_MINI,
                MemberSocialPO::getIdentifier, openId
        );

        MemberUserPO memberUserPO;
        if (socialPO == null) {
            memberUserPO = createWechatMember(openId, unionId);
        } else {
            memberUserPO = memberUserMapper.selectById(socialPO.getMemberId());
            if (memberUserPO == null) {
                memberUserPO = buildDefaultMember();
                memberUserMapper.insert(memberUserPO);

                memberSocialMapper.update(
                        null,
                        new LambdaUpdateWrapper<MemberSocialPO>()
                                .eq(MemberSocialPO::getId, socialPO.getId())
                                .set(MemberSocialPO::getMemberId, memberUserPO.getId())
                                .set(MemberSocialPO::getUnionId, unionId)
                );
            }
        }

        if (UserStatusEnum.DISABLED.matches(memberUserPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        StpMemberUtil.login(memberUserPO.getId());
        String displayName = resolveDisplayName(memberUserPO);
        authLoginAuditSupport.storeMemberSession(
                displayName,
                displayName,
                AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_WECHAT
        );
        memberUserService.touchLastLoginInfo(memberUserPO.getId());
        return assembleMemberLoginVO(memberUserPO);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private JsonNode fetchWechatSession(String code) {
        if (!StringUtils.hasText(code)) {
            throw BusinessException.badRequest("wechat.code.cannot.be.blank");
        }

        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatProperties.getAppId(), wechatProperties.getSecret(), code
        );

        try {
            JsonNode response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("openid")) {
                return response;
            }

            String errorMsg = response != null && response.has("errmsg") ? response.get("errmsg").asText() : "unknown error";
            log.warn("WeChat login validation failed, errmsg={}", errorMsg);
            throw BusinessException.badRequest("wechat.login.failed");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call WeChat login API", e);
            throw BusinessException.badGateway("technical.wechat.service.error", e);
        }
    }

    /**
     * 创建微信注册会员。
     */
    private MemberUserPO createWechatMember(String openId, String unionId) {
        MemberUserPO memberUserPO = buildDefaultMember();
        memberUserMapper.insert(memberUserPO);

        MemberSocialPO memberSocialPO = MemberSocialPO.builder()
                .memberId(memberUserPO.getId())
                .identityType(SocialIdentityTypeEnum.WX_MINI)
                .identifier(openId)
                .unionId(unionId)
                .build();
        memberSocialMapper.insert(memberSocialPO);

        log.info("Created member WeChat account, memberId={}", memberUserPO.getId());
        return memberUserPO;
    }

    /**
     * 组装返回对象。
     */
    private LoginVO assembleLoginVO(UserDTO userDTO, SaTokenInfo tokenInfo) {
        LoginVO loginVO = new LoginVO();
        loginVO.setId(userDTO.getId());
        loginVO.setUsername(userDTO.getUsername());
        loginVO.setNickname(userDTO.getNickname());
        loginVO.setAvatar(userDTO.getAvatar());
        loginVO.setDeptId(userDTO.getDeptId());
        loginVO.setTokenName(tokenInfo.getTokenName());
        loginVO.setTokenValue(tokenInfo.getTokenValue());
        return loginVO;
    }

    /**
     * 组装返回对象。
     */
    private LoginVO assembleMemberLoginVO(MemberUserPO memberUserPO) {
        LoginVO loginVO = new LoginVO();
        loginVO.setId(memberUserPO.getId());
        String displayName = resolveDisplayName(memberUserPO);
        loginVO.setUsername(displayName);
        loginVO.setNickname(displayName);
        loginVO.setTokenName(StpMemberUtil.getStpLogic().getTokenName());
        loginVO.setTokenValue(StpMemberUtil.getTokenValue());
        return loginVO;
    }

    /**
     * 构建默认会员对象。
     */
    private MemberUserPO buildDefaultMember() {
        MemberUserPO memberUserPO = MemberUserPO.builder()
                .status(UserStatusEnum.NORMAL)
                .build();
        memberUserPO.setUserLevel(1);
        memberUserPO.setPoints(0L);
        memberUserPO.setMemberType(0);
        memberUserPO.setRealNameStatus(0);
        memberUserPO.setRegisterIp(resolveCurrentClientIp());
        return memberUserPO;
    }

    /**
     * 解析用于展示的名称。
     */
    private String resolveDisplayName(MemberUserPO memberUserPO) {
        if (StringUtils.hasText(memberUserPO.getNickname())) {
            return memberUserPO.getNickname();
        }
        if (StringUtils.hasText(memberUserPO.getPhoneNumber())) {
            return memberUserPO.getPhoneNumber();
        }
        return "member_" + memberUserPO.getId();
    }

    /**
     * 解析当前请求的客户端 IP。
     */
    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }
}