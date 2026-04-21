package com.aicodeassistant.controller;

import com.aicodeassistant.config.AdminSecurityFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

/**
 * AdminController — Admin 管理面板的 REST API 端点。
 * <p>
 * 提供以下功能:
 * <ul>
 *   <li>POST /api/admin/login — Admin 登录认证</li>
 *   <li>GET /api/admin/status — 检查当前 admin 认证状态</li>
 *   <li>POST /api/admin/logout — 注销 admin 会话</li>
 * </ul>
 * <p>
 * 安全机制:
 * <ul>
 *   <li>密码通过 SHA-256 哈希验证</li>
 *   <li>登录成功后签发 HttpOnly Cookie (8 小时有效期)</li>
 *   <li>所有 admin 路由由 {@link AdminSecurityFilter} 保护</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    /** Admin 密码的 SHA-256 哈希值 (从配置文件读取) */
    private final String adminPasswordHash;

    /** Admin Session Cookie 名称 */
    private static final String ADMIN_COOKIE_NAME = "ai-coder-admin-session";

    public AdminController(@org.springframework.beans.factory.annotation.Value("${admin.password:}") String adminPassword) {
        if (adminPassword != null && !adminPassword.isBlank()) {
            this.adminPasswordHash = hashPassword(adminPassword);
            log.info("Admin authentication configured");
        } else {
            this.adminPasswordHash = null;
            log.warn("Admin authentication not configured (set admin.password in config)");
        }
    }

    /**
     * Admin 登录端点 — 验证密码并签发 session cookie。
     *
     * @param loginRequest 包含 password 字段的登录请求
     * @param response HTTP 响应，用于签发 Cookie
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        if (adminPasswordHash == null) {
            return ResponseEntity.status(503).body(
                    Map.of("success", false, "message", "Admin authentication not configured"));
        }

        if (loginRequest == null || loginRequest.password() == null || loginRequest.password().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Password is required"));
        }

        String providedHash = hashPassword(loginRequest.password());
        if (!adminPasswordHash.equals(providedHash)) {
            log.warn("Failed admin login attempt from {}", java.time.Instant.now());
            return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Invalid password"));
        }

        // 签发 admin session cookie
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie
                .from(ADMIN_COOKIE_NAME, providedHash)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(java.time.Duration.ofHours(8))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        log.info("Successful admin login at {}", java.time.Instant.now());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "admin", true,
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * 检查当前 admin 认证状态。
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = ADMIN_COOKIE_NAME, required = false) String adminCookie) {

        boolean isAuthenticated = false;

        // 检查 Cookie
        if (adminCookie != null && adminPasswordHash != null && adminPasswordHash.equals(adminCookie)) {
            isAuthenticated = true;
        }

        // 检查 Bearer Token
        if (!isAuthenticated && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tokenHash = hashPassword(token);
            if (adminPasswordHash != null && adminPasswordHash.equals(tokenHash)) {
                isAuthenticated = true;
            }
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", isAuthenticated,
                "configured", adminPasswordHash != null,
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Admin 注销端点 — 清除 session cookie。
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie
                .from(ADMIN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        log.info("Admin logout at {}", java.time.Instant.now());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    // ───── 密码哈希 ─────

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // ───── DTO Records ─────

    public record LoginRequest(String password) {}
}
