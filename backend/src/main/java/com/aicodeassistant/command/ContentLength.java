package com.aicodeassistant.command;

/**
 * 内容长度枚举 — PromptCommand 期望的输出长度范围。
 *
 */
public enum ContentLength {
    /** 简短输出 */
    SHORT,
    /** 正常输出 */
    NORMAL,
    /** 长输出（如 /review, /commit） */
    LONG
}
