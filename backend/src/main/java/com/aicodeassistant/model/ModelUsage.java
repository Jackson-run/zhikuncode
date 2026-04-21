package com.aicodeassistant.model;

/**
 * 单个模型的用量统计。
 *
 */
public record ModelUsage(
        int inputTokens,
        int outputTokens,
        int cacheReadTokens,
        int cacheCreationTokens,
        int apiCalls,
        double costUSD
) {}
