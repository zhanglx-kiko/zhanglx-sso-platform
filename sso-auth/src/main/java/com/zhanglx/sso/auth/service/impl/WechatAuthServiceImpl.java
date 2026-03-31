package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.properties.WechatProperties;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:28
 * @ClassName: WechatAuthServiceImpl
 * @Description:
 */
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
        // 1. 调用微信接口换取 OpenID
        String openId = fetchOpenIdFromWechat(code);

        // 2. 根据 OpenID 查询数据库用户
        UserDTO user = userService.getUserByOpenId(openId);

        // 3. 业务逻辑：如果是首次进入的新用户，执行静默注册
        if (user == null) {
            user = new UserDTO();
            user.setOpenId(openId);
            // 这里可以设置一些默认的昵称或头像（微信新规不再直接返回昵称头像，需后续引导用户授权）
            user.setNickname("用户_" + System.currentTimeMillis() % 10000);
            user = userService.addWxUser(user);

            // 4. 为新用户绑定默认的角色权限 (例如：角色 ID 为 2 代表普通消费者)
            // todo 为微信新用户绑定默认角色
//            roleService.bindDefaultRole(user.getId(), 2L);
            log.info("检测到新客访问，已静默注册成功，用户 ID: {}", user.getId());
        }

        // 5. 使用 Sa-Token 进行登录会话记录
        StpUtil.login(user.getId());

        // 6. 返回包含了 tokenName 和 tokenValue 的鉴权信息给小程序端
        return assembleLoginVO(user, StpUtil.getTokenInfo());
    }

    /**
     * 向微信服务器发请求获取 OpenID
     */
    private String fetchOpenIdFromWechat(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatProperties.getAppId(), wechatProperties.getSecret(), code
        );

        try {
            // 使用 RestClient 发起 GET 请求并解析 JSON
            JsonNode response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("openid")) {
                return response.get("openid").asText();
            } else {
                String errorMsg = response != null ? response.get("errmsg").asText() : "未知错误";
                log.error("微信登录凭证校验失败: {}", errorMsg);
                throw new RuntimeException("微信登录失败：" + errorMsg);
            }
        } catch (Exception e) {
            log.error("调用微信接口网络异常", e);
            throw new RuntimeException("调用微信服务异常");
        }
    }

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
