package com.example.repair.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP消息请求/响应基类
 */
@Data
public class McpMessage {

    /**
     * JSON-RPC版本
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    /**
     * 消息ID
     */
    private String id;

    /**
     * 方法名
     */
    private String method;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 响应结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private McpError error;
}
