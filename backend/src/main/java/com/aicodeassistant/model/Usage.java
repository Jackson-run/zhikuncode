package com.aicodeassistant.model;

/**
 * Token 使用量统计。
 *
 */
public record Usage(
        int inputTokens,
        int outputTokens,
        int cacheReadInputTokens,
        int cacheCreationInputTokens
) {
    public int totalTokens() {
        return inputTokens + outputTokens;
    }

    public static Usage zero() {
        return new Usage(0, 0, 0, 0);
    }

    public Usage add(Usage other) {
        return new Usage(
                inputTokens + other.inputTokens,
                outputTokens + other.outputTokens,
                cacheReadInputTokens + other.cacheReadInputTokens,
                cacheCreationInputTokens + other.cacheCreationInputTokens
        );
    }
}
