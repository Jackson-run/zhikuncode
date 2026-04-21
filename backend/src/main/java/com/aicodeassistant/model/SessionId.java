package com.aicodeassistant.model;

import java.util.Objects;

/**
 * 品牌化会话 ID — 标识一个完整的对话会话。
 * 编译期类型安全的 ID 包装，防止不同 ID 类型的误用。
 *
 */
public record SessionId(String value) {

    public SessionId {
        Objects.requireNonNull(value, "SessionId cannot be null");
    }

    public static SessionId of(String raw) {
        return new SessionId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
