package com.aicodeassistant.model;

import java.util.List;

/**
 * Shell 任务特有字段 (SHELL 类型)。
 *
 */
public record LocalShellTaskState(
        String command,
        String result,
        boolean completionStatusSentInAttachment,
        ShellCommand shellCommand,
        boolean isBackgrounded,
        String agentId,
        String kind
) {}
