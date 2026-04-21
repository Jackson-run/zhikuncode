package com.aicodeassistant.bridge;

/**
 * 桥接传输协议版本。
 *
 */
public enum BridgeProtocolVersion {
    /** WebSocket(读) + HTTP(写) 混合传输 */
    V1_HYBRID,
    /** SSE(读) + HTTP(写) 云端远程传输 */
    V2_SSE_CCR
}
