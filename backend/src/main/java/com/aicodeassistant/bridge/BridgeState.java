package com.aicodeassistant.bridge;

/**
 * 桥接状态机 — 4 种状态。
 *
 */
public enum BridgeState {
    /** 就绪，等待连接 */
    READY,
    /** 已连接 */
    CONNECTED,
    /** 正在重连 */
    RECONNECTING,
    /** 连接失败 */
    FAILED
}
