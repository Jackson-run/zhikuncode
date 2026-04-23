package com.aicodeassistant.plugin;

import com.aicodeassistant.command.CommandContext;
import com.aicodeassistant.command.CommandResult;
import com.aicodeassistant.command.CommandType;
import com.aicodeassistant.state.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ReloadPluginsCommand 单元测试 (P0)
 */
class ReloadPluginsCommandTest {

    private PluginManager pluginManager;
    private ReloadPluginsCommand command;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        command = new ReloadPluginsCommand(pluginManager);
    }

    @Test
    @DisplayName("execute 应调用 pluginManager.reloadPlugins()")
    void shouldCallPluginManagerReload() {
        when(pluginManager.getPluginCount()).thenReturn(0);
        when(pluginManager.getEnabledPlugins()).thenReturn(List.of());

        CommandContext ctx = CommandContext.of("session-1", "/tmp", "test-model", AppState.defaultState());
        command.execute("", ctx);

        verify(pluginManager).reloadPlugins();
    }

    @Test
    @DisplayName("execute 应返回包含插件数量的成功消息")
    void shouldReturnSuccessMessageWithPluginCount() {
        when(pluginManager.getPluginCount()).thenReturn(3);
        when(pluginManager.getEnabledPlugins()).thenReturn(List.of(
                mock(LoadedPlugin.class), mock(LoadedPlugin.class)));

        CommandContext ctx = CommandContext.of("session-1", "/tmp", "test-model", AppState.defaultState());
        CommandResult result = command.execute("", ctx);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.value().contains("3"));
        assertTrue(result.value().contains("2 enabled"));
    }

    @Test
    @DisplayName("getName 应返回 'reload-plugins'")
    void shouldReturnCommandName() {
        assertEquals("reload-plugins", command.getName());
    }

    @Test
    @DisplayName("getDescription 应返回非空描述")
    void shouldReturnCommandDescription() {
        assertNotNull(command.getDescription());
        assertFalse(command.getDescription().isEmpty());
    }

    @Test
    @DisplayName("getType 应返回 LOCAL")
    void shouldReturnLocalType() {
        assertEquals(CommandType.LOCAL, command.getType());
    }

    @Test
    @DisplayName("reloadPlugins 异常时返回错误消息")
    void shouldReturnErrorMessageOnException() {
        doThrow(new RuntimeException("reload failed")).when(pluginManager).reloadPlugins();

        CommandContext ctx = CommandContext.of("session-1", "/tmp", "test-model", AppState.defaultState());
        CommandResult result = command.execute("", ctx);

        assertNotNull(result);
        assertTrue(result.value().contains("reload failed"));
    }
}
