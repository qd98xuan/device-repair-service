package com.example.repair.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

/**
 * Swagger/OpenAPI 配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("设备报修工单服务 API")
                        .description("设备报修工单服务后端 API 文档，包含业务 REST 接口与供 AI Agent 调用的 MCP 接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // 安全认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .addSecurityItem(new SecurityRequirement().addList("API Key Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("通过 Authorization: Bearer <token> 传入 MCP 访问令牌"))
                        .addSecuritySchemes("API Key Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("通过 X-API-Key 请求头传入 MCP 访问令牌")));
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/api/**")
                .packagesToScan("com.example.repair.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi mcpApi() {
        return GroupedOpenApi.builder()
                .group("mcp")
                .pathsToMatch("/mcp", "/mcp/**")
                .packagesToScan("com.example.repair.mcp.controller")
                .build();
    }
}
