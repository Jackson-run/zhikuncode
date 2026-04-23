package com.aicodeassistant.plugin;

import com.aicodeassistant.controller.PluginController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PluginController 纯单元测试 (P0) — 直接调用方法验证返回值。
 */
class PluginControllerTest {

    private PluginManager pluginManager;
    private PluginLoader pluginLoader;
    private PluginController controller;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        pluginLoader = mock(PluginLoader.class);
        controller = new PluginController(pluginManager, pluginLoader);
    }

    // ==================== listPlugins ====================

    @Test
    @DisplayName("listPlugins 应返回插件列表")
    void shouldListPlugins() {
        PluginExtension ext = new PluginExtension() {
            @Override public String name() { return "hello"; }
            @Override public String version() { return "1.0.0"; }
            @Override public String description() { return "Hello plugin"; }
        };
        LoadedPlugin plugin = LoadedPlugin.builtin("hello", ext);
        when(pluginManager.getLoadedPlugins()).thenReturn(List.of(plugin));

        ResponseEntity<Map<String, List<PluginController.PluginInfo>>> response =
                controller.listPlugins();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<PluginController.PluginInfo> plugins = response.getBody().get("plugins");
        assertEquals(1, plugins.size());
        assertEquals("hello", plugins.get(0).name());
        assertEquals("1.0.0", plugins.get(0).version());
        assertTrue(plugins.get(0).isBuiltin());
        assertTrue(plugins.get(0).enabled());
    }

    @Test
    @DisplayName("listPlugins 无插件时返回空列表")
    void shouldReturnEmptyListWhenNoPlugins() {
        when(pluginManager.getLoadedPlugins()).thenReturn(List.of());

        ResponseEntity<Map<String, List<PluginController.PluginInfo>>> response =
                controller.listPlugins();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("plugins").isEmpty());
    }

    // ==================== reloadPlugins ====================

    @Test
    @DisplayName("reload 应返回 enabled/disabled 统计")
    void shouldReloadAndReturnStats() {
        PluginExtension ext1 = new PluginExtension() {
            @Override public String name() { return "p1"; }
            @Override public String version() { return "1.0.0"; }
        };
        PluginExtension ext2 = new PluginExtension() {
            @Override public String name() { return "p2"; }
            @Override public String version() { return "1.0.0"; }
        };
        LoadedPlugin enabled = LoadedPlugin.builtin("p1", ext1);
        LoadedPlugin disabled = LoadedPlugin.builtin("p2", ext2).withDisabled();

        when(pluginManager.getLoadedPlugins()).thenReturn(List.of(enabled, disabled));
        when(pluginManager.getPluginCount()).thenReturn(2);

        ResponseEntity<Map<String, Object>> response = controller.reloadPlugins();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(pluginManager).reloadPlugins();

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.get("loaded"));
        assertEquals(1L, body.get("enabled"));
        assertEquals(1L, body.get("disabled"));
    }

    // ==================== deletePlugin ====================

    @Test
    @DisplayName("删除内置插件应返回 400")
    void shouldRejectDeleteBuiltinPlugin() {
        PluginExtension ext = new PluginExtension() {
            @Override public String name() { return "builtin-plugin"; }
            @Override public String version() { return "1.0.0"; }
        };
        LoadedPlugin builtinPlugin = LoadedPlugin.builtin("builtin-plugin", ext);
        when(pluginManager.getPlugin("builtin-plugin")).thenReturn(Optional.of(builtinPlugin));

        ResponseEntity<Map<String, Object>> response = controller.deletePlugin("builtin-plugin");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cannot delete builtin plugin", response.getBody().get("error"));
    }

    @Test
    @DisplayName("删除不存在的插件应返回 404")
    void shouldReturn404WhenPluginNotFound() {
        when(pluginManager.getPlugin("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.deletePlugin("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== listPlugins 多插件场景 ====================

    @Test
    @DisplayName("listPlugins 多插件时返回正确数量和统计")
    void shouldListMultiplePlugins() {
        PluginExtension ext1 = new PluginExtension() {
            @Override public String name() { return "a"; }
            @Override public String version() { return "1.0.0"; }
        };
        PluginExtension ext2 = new PluginExtension() {
            @Override public String name() { return "b"; }
            @Override public String version() { return "2.0.0"; }
        };
        LoadedPlugin p1 = LoadedPlugin.builtin("a", ext1);
        LoadedPlugin p2 = LoadedPlugin.local("b", "/jar/b.jar", ext2, true);
        when(pluginManager.getLoadedPlugins()).thenReturn(List.of(p1, p2));

        ResponseEntity<Map<String, List<PluginController.PluginInfo>>> response =
                controller.listPlugins();

        assertEquals(2, response.getBody().get("plugins").size());
    }
}
