package com.zhanglx.sso.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/16 17:59
 * @ClassName: OpenApiConfig
 * @Description:
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SSO 统一认证平台 API 文档")
                        .version("1.0.0")
                        .description("提供用户认证、OAuth2.0 授权等功能的 RESTful API 接口")
                        .contact(new Contact()
                                .name("ZhangLX")
                                .email("zhanglx@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:9000").description("本地环境"),
                        new Server().url("http://dev-server:9000").description("开发环境"),
                        new Server().url("http://test-server:9000").description("测试环境"),
                        new Server().url("http://prod-server:9000").description("生产环境")
                ))
                .schemaRequirement("token", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .name("token")
                        .in(SecurityScheme.In.HEADER)
                        .description("请在 Header 中填入 Token"))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }

    @Bean
    public GroupedOpenApi api() {
        // 创建了一个api接口的分组
        return GroupedOpenApi.builder()
                // 分组名称，使用英文，中文访问异常(使用displayName设置中文名，避免直接使用group设置中文时访问异常)
                .group("auth-api")
                .displayName("认证接口") // 使用displayName设置中文接口分组名时，group仍不可或缺
                .packagesToScan("com.zhanglx.sso.auth")
                .build();
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 全局自定义
        };
    }

}
