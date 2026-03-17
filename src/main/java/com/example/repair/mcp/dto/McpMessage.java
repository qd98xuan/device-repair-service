package com.example.repair.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP消息请求/响应基类
 */
@Data
@Schema(description = "MCP JSON-RPC 消息")
public class McpMessage {

    /**
     * JSON-RPC版本
     */
    @JsonProperty("jsonrpc")
    @Schema(description = "JSON-RPC版本", example = "2.0")
    private String jsonrpc = "2.0";

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "init-1")
    private String id;

    /**
     * 方法名
     */
    @Schema(description = "调用方法，例如 initialize、tools/list、tools/call", example = "initialize")
    private String method;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数对象，不同 method 的结构不同")
    private Map<String, Object> params;

    /**
     * 响应结果
     */
    @Schema(description = "成功响应结果")
    private Object result;

    /**
     * 错误信息
     */
    @Schema(description = "失败时返回的错误信息")
    private McpError error;
}
