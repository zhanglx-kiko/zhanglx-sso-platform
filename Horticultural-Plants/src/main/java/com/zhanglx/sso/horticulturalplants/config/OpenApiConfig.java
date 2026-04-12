package com.zhanglx.sso.horticulturalplants.config;

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

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("花草苗木建议零售价服务 API")
                        .version("1.0.0")
                        .description("提供花草苗木建议零售价内容发布、浏览、管理等接口")
                        .contact(new Contact()
                                .name("ZhangLX")
                                .email("zhanglx@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:22000").description("本地环境"),
                        new Server().url("http://dev-server:22000").description("开发环境"),
                        new Server().url("http://test-server:22000").description("测试环境"),
                        new Server().url("http://prod-server:22000").description("生产环境")
                ))
                .schemaRequirement("token", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .name("token")
                        .in(SecurityScheme.In.HEADER)
                        .description("请在 Header 中填写会员 Token"))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("horticultural-plants-api")
                .displayName("花草苗木接口")
                .packagesToScan("com.zhanglx.sso.horticulturalplants")
                .build();
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
        };
    }
}
