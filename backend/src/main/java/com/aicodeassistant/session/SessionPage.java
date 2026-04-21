package com.aicodeassistant.session;

import com.aicodeassistant.model.SessionSummary;

import java.util.List;

/**
 * 会话分页结果 — 游标分页。
 *
 */
public record SessionPage(
        List<SessionSummary> sessions,
        boolean hasMore,
        String oldestId
) {}
