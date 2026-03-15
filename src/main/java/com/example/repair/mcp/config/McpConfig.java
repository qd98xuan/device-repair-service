package com.example.repair.mcp.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP服务配置类
 */
@Configuration
public class McpConfig {

    /**
     * 配置MCP认证过滤器
     */
    @Bean
    public FilterRegistrationBean<McpAuthFilter> mcpAuthFilterRegistration(McpAuthFilter filter) {
        FilterRegistrationBean<McpAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/mcp");
        registration.addUrlPatterns("/mcp/*");
        registration.setName("mcpAuthFilter");
        registration.setOrder(1);
        return registration;
    }
}
