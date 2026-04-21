package com.aicodeassistant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;

/**
 * Admin 路由安全过滤器 — 为 /admin/** 路径提供额外的 admin 权限验证。
 * <p>
 * 认证流程:
 * <ol>
 *   <li>检查请求路径是否以 /admin 开头 — 不是 → 直接放行</li>
 *   <li>检查 Admin Cookie 是否有效 — 有 → 放行</li>
 *   <li>检查 Authorization: Bearer {admin_token} 头 — 有且匹配 → 签发 Cookie + 放行</li>
 *   <li>以上都不满足 → 返回 401</li>
 * </ol>
 */
@Component
@Order(2)
public class AdminSecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdminSecurityFilter.class);

    /** Admin 密码的 SHA-256 哈希值 */
    private final String adminPasswordHash;

    /** Admin Session Cookie 名称 */
    private static final String ADMIN_COOKIE_NAME = "ai-coder-admin-session";

    /** Admin Cookie 有效期 */
    private static final Duration ADMIN_COOKIE_MAX_AGE = Duration.ofHours(8);

    /** 需要 admin 权限的路径前缀 */
    private static final String ADMIN_PATH_PREFIX = "/admin";

    public AdminSecurityFilter(@Value("${admin.password:}") String adminPassword) {
        if (adminPassword != null && !adminPassword.isBlank()) {
            this.adminPasswordHash = hashPassword(adminPassword);
            log.info("Admin authentication enabled");
        } else {
            this.adminPasswordHash = null;
            log.info("Admin authentication disabled (no admin.password configured)");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 非 admin 路径 → 直接放行
        if (!path.startsWith(ADMIN_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // Admin 密码未配置 → 拒绝访问
        if (adminPasswordHash == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Admin access is not configured");
            return;
        }

        // 尝试 1: Admin Cookie
        String adminCookie = getCookieValue(request, ADMIN_COOKIE_NAME);
        if (adminCookie != null && adminPasswordHash.equals(adminCookie)) {
            chain.doFilter(request, response);
            return;
        }

        // 尝试 2: Authorization: Bearer {admin_token}
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tokenHash = hashPassword(token);
            if (adminPasswordHash.equals(tokenHash)) {
                issueAdminCookie(response, tokenHash);
                chain.doFilter(request, response);
                return;
            }
        }

        // 全部失败 → 401
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Admin authentication required");
    }

    // ───── Cookie 管理 ─────

    private void issueAdminCookie(HttpServletResponse response, String passwordHash) {
        ResponseCookie cookie = ResponseCookie.from(ADMIN_COOKIE_NAME, passwordHash)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(ADMIN_COOKIE_MAX_AGE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
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

    /** 静态资源路径不拦截 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/assets/") || path.startsWith("/icons/")
                || path.equals("/favicon.ico");
    }
}
