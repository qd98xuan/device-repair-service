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

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_KEY_HEADER = "X-API-Key";

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

        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            sendUnauthorizedResponse(response, "Missing or invalid token");
            return;
        }

        // 验证Token
        if (mcpToken == null || mcpToken.isBlank()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"MCP auth token is not configured\"},\"id\":null}"
            );
            return;
        }

        if (!mcpToken.equals(token)) {
            sendUnauthorizedResponse(response, "Invalid token");
            return;
        }

        // Token验证通过，继续处理请求
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        return null;
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("WWW-Authenticate", "Bearer");

        String jsonResponse = String.format(
                "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"%s\"},\"id\":null}",
                message
        );
        response.getWriter().write(jsonResponse);
    }
}
