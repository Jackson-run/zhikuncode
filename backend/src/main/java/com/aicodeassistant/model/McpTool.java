package com.aicodeassistant.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * MCP 工具定义。
 *
 */
public record McpTool(
        String name,
        String description,
        JsonNode inputSchema
) {}
