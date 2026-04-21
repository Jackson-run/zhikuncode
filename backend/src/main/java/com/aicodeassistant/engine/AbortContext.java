package com.aicodeassistant.engine;

import java.util.concurrent.CompletableFuture;

/**
 * 中断上下文。
 * <p>
 * 技术栈适配:
 * - 默认使用 AbortController + signal.aborted + signal.reason
 * - Java 21 等价: volatile + CompletableFuture (不用 Thread.interrupt() 避免与 CF 冲突)
 * <p>
 * 注意: 可变状态，不适合用 record。
 *
 */
public class AbortContext {

    private volatile boolean aborted = false;
    private volatile AbortReason reason;
    private final CompletableFuture<AbortReason> abortFuture = new CompletableFuture<>();

    /**
     * 触发中断 — volatile 写保证 happens-before。
     */
    public void abort(AbortReason r) {
        this.reason = r;
        this.aborted = true;
        abortFuture.complete(r);
    }

    public boolean isAborted() {
        return aborted;
    }

    public AbortReason getReason() {
        return reason;
    }

    /**
     * 是否应注入用户中断消息。
     * submit-interrupt 不注入中断消息（保留用户新输入）。
     */
    public boolean shouldInjectInterruptMessage() {
        return reason != AbortReason.SUBMIT_INTERRUPT
                && reason != AbortReason.SESSION_DISCONNECTED;
    }

    /**
     * 等待中断事件。
     * 在 VirtualThread 上调用 join()/get() 不会阻塞平台线程。
     */
    public CompletableFuture<AbortReason> onAbort() {
        return abortFuture;
    }
}
