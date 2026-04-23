package com.aicodeassistant.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

/**
 * PluginClassLoader 沙箱安全测试。
 * 验证插件类加载器正确隔离宿主类，仅允许白名单框架类通过。
 */
class PluginClassLoaderSecurityTest {

    private PluginClassLoader createLoader() {
        return new PluginClassLoader(new URL[0], getClass().getClassLoader());
    }

    // ==================== 允许加载的类 ====================

    @Test
    @DisplayName("允许加载 Spring Framework 类（框架白名单）")
    void shouldAllowSpringClasses() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("org.springframework.stereotype.Component"));
    }

    @Test
    @DisplayName("允许加载 SLF4J 类（JDK 委托路径）")
    void shouldAllowSlf4jClasses() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("org.slf4j.Logger"));
    }

    @Test
    @DisplayName("允许加载 Jackson 类（框架白名单）")
    void shouldAllowJacksonClasses() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("com.fasterxml.jackson.databind.ObjectMapper"));
    }

    @Test
    @DisplayName("允许加载 Jakarta 类（框架白名单）")
    void shouldAllowJakartaClasses() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("jakarta.annotation.PreDestroy"));
    }

    @Test
    @DisplayName("允许加载 Java 标准库类")
    void shouldAllowJavaBaseClasses() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("java.util.List"));
    }

    // ==================== 允许的宿主 API 包 ====================

    @Test
    @DisplayName("允许加载插件 API 包 — com.aicodeassistant.plugin")
    void shouldAllowPluginApiPackage() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("com.aicodeassistant.plugin.PluginExtension"));
    }

    @Test
    @DisplayName("允许加载工具 API 包 — com.aicodeassistant.tool")
    void shouldAllowToolApiPackage() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("com.aicodeassistant.tool.Tool"));
    }

    @Test
    @DisplayName("允许加载命令 API 包 — com.aicodeassistant.command")
    void shouldAllowCommandApiPackage() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("com.aicodeassistant.command.Command"));
    }

    @Test
    @DisplayName("允许加载 MCP API 包 — com.aicodeassistant.mcp")
    void shouldAllowMcpApiPackage() {
        PluginClassLoader loader = createLoader();
        assertDoesNotThrow(() -> loader.loadClass("com.aicodeassistant.mcp.McpServerConfig"));
    }

    // ==================== 拒绝加载的宿主类 ====================

    @Test
    @DisplayName("拒绝加载宿主安全相关类")
    void shouldBlockHostSecurityClasses() {
        PluginClassLoader loader = createLoader();
        assertThrows(ClassNotFoundException.class,
            () -> loader.loadClass("com.aicodeassistant.security.PathSecurityService"));
    }

    @Test
    @DisplayName("拒绝加载宿主存储相关类")
    void shouldBlockHostStorageClasses() {
        PluginClassLoader loader = createLoader();
        assertThrows(ClassNotFoundException.class,
            () -> loader.loadClass("com.aicodeassistant.storage.SomeStorageService"));
    }

    @Test
    @DisplayName("拒绝加载宿主 hook 包类")
    void shouldBlockHostHookClasses() {
        PluginClassLoader loader = createLoader();
        assertThrows(ClassNotFoundException.class,
            () -> loader.loadClass("com.aicodeassistant.hook.HookRegistry"));
    }

    @Test
    @DisplayName("拒绝加载宿主 agent 包类")
    void shouldBlockHostAgentClasses() {
        PluginClassLoader loader = createLoader();
        assertThrows(ClassNotFoundException.class,
            () -> loader.loadClass("com.aicodeassistant.agent.SomeAgentService"));
    }
}
