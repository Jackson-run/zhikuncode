package com.aicodeassistant.command;

/**
 * 命令类型枚举 — 三种命令执行模型。
 *
 */
public enum CommandType {
    /** 本地执行，直接返回结果 */
    LOCAL,
    /** 生成提示词，发送给 LLM */
    PROMPT,
    /** 本地执行，返回需前端渲染的内容 */
    LOCAL_JSX
}
