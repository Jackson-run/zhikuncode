package com.aicodeassistant.model;

import java.time.Instant;

/**
 * OAuth Token。
 *
 */
public record OAuthToken(
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        String accountId
) {}
