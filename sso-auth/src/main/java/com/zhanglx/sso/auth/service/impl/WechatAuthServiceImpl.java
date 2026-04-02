package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import tools.jackson.databind.JsonNode;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.properties.WechatProperties;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WechatProperties wechatProperties;
    private final UserService userService;
    private final RoleService roleService;
    private final RestClient restClient = RestClient.create();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByWechatCode(String code) {
        String openId = fetchOpenIdFromWechat(code);
        UserDTO user = userService.getUserByOpenId(openId);

        if (user == null) {
            user = new UserDTO();
            user.setOpenId(openId);
            user.setNickname("用户_" + System.currentTimeMillis() % 10000);
            user = userService.addWxUser(user);

            // TODO 为微信新用户绑定默认角色
            log.info("检测到新客访问，已静默注册成功，用户ID: {}", user.getId());
        }

        StpUtil.login(user.getId());
        return assembleLoginVO(user, StpUtil.getTokenInfo());
    }

    /**
     * 调用微信接口，通过授权码换取 OpenID。
     *
     * <p>当微信返回业务失败时，会转换为 400 级业务异常；当 HTTP 调用或网络链路异常时，
     * 会转换为 502 级异常，便于前端区分业务失败与上游服务故障。</p>
     *
     * @param code 微信小程序登录授权码
     * @return 微信 OpenID
     */
    private String fetchOpenIdFromWechat(String code) {
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
                return response.get("openid").asText();
            }

            String errorMsg = response != null && response.has("errmsg") ? response.get("errmsg").asText() : "未知错误";
            log.warn("微信登录凭证校验失败: {}", errorMsg);
            throw BusinessException.badRequest("微信登录失败: " + errorMsg);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口网络异常", e);
            throw BusinessException.badGateway("调用微信服务异常", e);
        }
    }

    /**
     * 组装微信登录成功后的返回对象。
     *
     * @param userDTO 登录用户信息
     * @param tokenInfo Sa-Token 生成的令牌信息
     * @return 返回给前端的登录视图对象
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

}
