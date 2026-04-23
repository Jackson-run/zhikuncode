package com.aicodeassistant.plugin;

import com.aicodeassistant.command.Command;
import com.aicodeassistant.mcp.McpServerConfig;
import com.aicodeassistant.tool.Tool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoadedPlugin 扩展场景测试 (P1) — toMcpMap 和工厂方法。
 */
class LoadedPluginExtendedTest {

    // ==================== toMcpMap 测试（通过工厂方法间接测试） ====================

    @Nested
    @DisplayName("toMcpMap 转换逻辑")
    class ToMcpMapTests {

        @Test
        @DisplayName("getMcpServers 返回 null 时应返回空 map")
        void shouldReturnEmptyMapForNullMcpServers() {
            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "null-mcp"; }
                @Override public String version() { return "1.0.0"; }
                @Override public List<McpServerConfig> getMcpServers() { return null; }
            };

            LoadedPlugin plugin = LoadedPlugin.builtin("null-mcp", ext);
            assertNotNull(plugin.mcpServers());
            assertTrue(plugin.mcpServers().isEmpty());
        }

        @Test
        @DisplayName("getMcpServers 返回空列表时应返回空 map")
        void shouldReturnEmptyMapForEmptyMcpList() {
            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "empty-mcp"; }
                @Override public String version() { return "1.0.0"; }
                @Override public List<McpServerConfig> getMcpServers() { return List.of(); }
            };

            LoadedPlugin plugin = LoadedPlugin.builtin("empty-mcp", ext);
            assertTrue(plugin.mcpServers().isEmpty());
        }

        @Test
        @DisplayName("正确按 name 映射为 key-value")
        void shouldMapServersByName() {
            McpServerConfig server1 = McpServerConfig.stdio("server-a", "cmd1", List.of());
            McpServerConfig server2 = McpServerConfig.sse("server-b", "http://localhost:8080");

            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "multi-mcp"; }
                @Override public String version() { return "1.0.0"; }
                @Override public List<McpServerConfig> getMcpServers() {
                    return List.of(server1, server2);
                }
            };

            LoadedPlugin plugin = LoadedPlugin.builtin("multi-mcp", ext);
            Map<String, McpServerConfig> map = plugin.mcpServers();

            assertEquals(2, map.size());
            assertEquals(server1, map.get("server-a"));
            assertEquals(server2, map.get("server-b"));
        }

        @Test
        @DisplayName("重名时后者覆盖前者 (last-writer-wins)")
        void shouldApplyLastWriterWinsForDuplicateNames() {
            McpServerConfig first = McpServerConfig.stdio("dup", "cmd-first", List.of());
            McpServerConfig second = McpServerConfig.sse("dup", "http://second");

            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "dup-mcp"; }
                @Override public String version() { return "1.0.0"; }
                @Override public List<McpServerConfig> getMcpServers() {
                    return List.of(first, second);
                }
            };

            LoadedPlugin plugin = LoadedPlugin.builtin("dup-mcp", ext);
            Map<String, McpServerConfig> map = plugin.mcpServers();

            assertEquals(1, map.size());
            // 后者（second）应覆盖前者
            assertEquals(second, map.get("dup"));
        }
    }

    // ==================== 工厂方法扩展场景 ====================

    @Nested
    @DisplayName("工厂方法扩展场景")
    class FactoryMethodTests {

        @Test
        @DisplayName("builtin 工厂方法 — 从 extension 正确提取组件")
        void builtinShouldExtractComponents() {
            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "full"; }
                @Override public String version() { return "2.0.0"; }
                @Override public String description() { return "Full plugin"; }
                @Override public List<Command> getCommands() { return List.of(); }
                @Override public List<Tool> getTools() { return List.of(); }
                @Override public List<HookHandler> getHooks() {
                    return List.of(HookHandler.preToolUse(".*", 10,
                            ctx -> HookHandler.HookResult.allow()));
                }
            };

            LoadedPlugin plugin = LoadedPlugin.builtin("full", ext);

            assertEquals("full", plugin.name());
            assertTrue(plugin.isBuiltin());
            assertTrue(plugin.enabled());
            assertEquals(PluginSourceType.BUILTIN, plugin.sourceType());
            assertNull(plugin.path());
            assertNotNull(plugin.manifest());
            assertEquals("2.0.0", plugin.manifest().version());
            assertEquals(0, plugin.commands().size());
            assertEquals(0, plugin.tools().size());
            assertEquals(1, plugin.hooks().size());
        }

        @Test
        @DisplayName("local 工厂方法 — enabled=false 时插件禁用")
        void localDisabledPlugin() {
            PluginExtension ext = new PluginExtension() {
                @Override public String name() { return "disabled-plugin"; }
                @Override public String version() { return "1.0.0"; }
            };

            LoadedPlugin plugin = LoadedPlugin.local("disabled-plugin",
                    "/path/to/plugin.jar", ext, false);

            assertFalse(plugin.enabled());
            assertFalse(plugin.isBuiltin());
            assertEquals(PluginSourceType.LOCAL, plugin.sourceType());
            assertEquals("/path/to/plugin.jar", plugin.path());
        }
    }
}
