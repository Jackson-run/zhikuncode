package com.aicodeassistant.exception;

/**
 * 会话未找到异常 — 特化的 ResourceNotFoundException。
 *
 */
public class SessionNotFoundException extends ResourceNotFoundException {

    private final String sessionId;

    public SessionNotFoundException(String sessionId) {
        super("SESSION_NOT_FOUND", "Session with id '" + sessionId + "' not found");
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
