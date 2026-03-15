package com.example.repair.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * MCP错误信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpError {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误详情
     */
    private Object data;

    /**
     * 解析错误（请求格式错误）
     */
    public static final int PARSE_ERROR = -32700;

    /**
     * 无效请求
     */
    public static final int INVALID_REQUEST = -32600;

    /**
     * 方法不存在
     */
    public static final int METHOD_NOT_FOUND = -32601;

    /**
     * 参数无效
     */
    public static final int INVALID_PARAMS = -32602;

    /**
     * 内部错误
     */
    public static final int INTERNAL_ERROR = -32603;

    public McpError() {
    }

    public McpError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public McpError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
