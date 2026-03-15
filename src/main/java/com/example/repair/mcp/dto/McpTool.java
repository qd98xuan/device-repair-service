package com.example.repair.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP工具定义
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpTool {

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 输入模式（JSON Schema）
     */
    private Map<String, Object> inputSchema;

    public McpTool() {
    }

    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
}
