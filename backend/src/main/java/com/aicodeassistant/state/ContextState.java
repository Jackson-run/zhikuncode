package com.aicodeassistant.state;

import java.util.Map;

/**
 * 上下文管理状态 — 自动压缩、记忆、设置。
 *
 */
public record ContextState(
        AutoCompactTrackingState autoCompactTracking,
        String memoryContent,
        Map<String, Object> settings,
        boolean verbose
) {
    /**
     * 自动压缩追踪状态。
     */
    public record AutoCompactTrackingState(
            boolean enabled,
            double threshold,
            int compactCount
    ) {
        public static AutoCompactTrackingState defaultState() {
            return new AutoCompactTrackingState(true, 0.8, 0);
        }
    }

    public static ContextState empty() {
        return new ContextState(
                AutoCompactTrackingState.defaultState(),
                null, Map.of(), false
        );
    }

    public ContextState withMemoryContent(String content) {
        return new ContextState(autoCompactTracking, content, settings, verbose);
    }

    public ContextState withSettings(Map<String, Object> settings) {
        return new ContextState(autoCompactTracking, memoryContent, settings, verbose);
    }

    public ContextState withVerbose(boolean verbose) {
        return new ContextState(autoCompactTracking, memoryContent, settings, verbose);
    }
}
