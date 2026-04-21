package com.aicodeassistant.command;

/**
 * 命令可用性枚举 — 控制命令在何种条件下可用。
 *
 */
public enum CommandAvailability {
    /** 始终可用 */
    ALWAYS,
    /** 需要认证 */
    REQUIRES_AUTH,
    /** 需要特定特性标志 */
    REQUIRES_FEATURE,
    /** 需要 Bridge 连接 */
    REQUIRES_BRIDGE
}
