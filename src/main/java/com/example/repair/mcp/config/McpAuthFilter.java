package com.example.repair.mcp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * MCP认证过滤器
 * 用于验证MCP请求的Token
 */
@Component
public class McpAuthFilter extends OncePerRequestFilter {

    /**
     * MCP认证Token
     */
    @Value("${mcp.auth.token:}")
    private String mcpToken;

    /**
     * 跳过认证的URL路径
     */
    private static final String[] EXCLUDE_PATHS = {
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/health",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 只对MCP相关路径进行认证
        String requestUri = request.getRequestURI();

        // 如果不是MCP请求路径，直接放行
        if (!requestUri.startsWith("/mcp")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否为排除路径
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestUri.startsWith(excludePath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");

        // 检查Token是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        // 提取Token
        String token = authHeader.substring(7);

        // 验证Token
        if (mcpToken == null || mcpToken.isEmpty()) {
            // 如果未配置Token，直接放行（开发环境）
            filterChain.doFilter(request, response);
            return;
        }

        if (!mcpToken.equals(token)) {
            sendUnauthorizedResponse(response, "Invalid token");
            return;
        }

        // Token验证通过，继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"%s\"},\"id\":null}",
                message
        );
        response.getWriter().write(jsonResponse);
    }
}
