package com.aicodeassistant.model;

/**
 * MCP 资源定义。
 *
 */
public record McpResource(
        String uri,
        String name,
        String description,
        String mimeType
) {}
