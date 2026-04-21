package com.aicodeassistant.model;

/**
 * 权限规则值 — 工具名 + 可选匹配条件。
 *
 */
public record PermissionRuleValue(
        String toolName,
        String ruleContent
) {}
