package com.aicodeassistant.model;

import java.util.List;

/**
 * 梦境任务特有字段 (DREAM 类型)。
 *
 */
public record DreamTaskState(
        DreamPhase phase,
        int sessionsReviewing,
        List<String> filesTouched,
        int turns,
        Long priorMtime
) {}
