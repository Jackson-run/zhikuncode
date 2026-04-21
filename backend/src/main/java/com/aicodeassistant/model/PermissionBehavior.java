package com.aicodeassistant.model;

/**
 * 权限行为枚举。
 *
 */
public enum PermissionBehavior {
    ALLOW,
    DENY,
    ASK,
    /** 透传 — 中间状态，等待管线后续步骤处理 */
    PASSTHROUGH
}
