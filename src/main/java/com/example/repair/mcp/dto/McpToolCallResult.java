package com.example.repair.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP工具调用结果
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolCallResult {

    /**
     * 工具调用结果内容
     */
    private List<Content> content;

    /**
     * 是否为结构化结果
     */
    private Boolean isError;

    public McpToolCallResult() {
    }

    public McpToolCallResult(List<Content> content) {
        this.content = content;
        this.isError = false;
    }

    /**
     * 创建文本内容结果
     */
    public static McpToolCallResult text(String text) {
        Content content = new Content();
        content.setType("text");
        content.setText(text);
        return new McpToolCallResult(List.of(content));
    }

    /**
     * 创建错误结果
     */
    public static McpToolCallResult error(String errorMessage) {
        Content content = new Content();
        content.setType("text");
        content.setText(errorMessage);
        McpToolCallResult result = new McpToolCallResult(List.of(content));
        result.setIsError(true);
        return result;
    }

    /**
     * 内容项
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        /**
         * 内容类型：text, image, resource
         */
        private String type;

        /**
         * 文本内容
         */
        private String text;

        /**
         * 图片数据（base64）
         */
        private String data;

        /**
         * MIME类型
         */
        private String mimeType;

        /**
         * 资源URI
         */
        private String uri;

        /**
         * 资源blob
         */
        private String blob;
    }
}
