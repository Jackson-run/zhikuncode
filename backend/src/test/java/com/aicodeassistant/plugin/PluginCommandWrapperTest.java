package com.aicodeassistant.plugin;

import com.aicodeassistant.command.Command;
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
 * PluginCommandWrapper 单元测试 (P1)
 */
class PluginCommandWrapperTest {

    private Command delegate;
    private PluginCommandWrapper wrapper;

    @BeforeEach
    void setUp() {
        delegate = mock(Command.class);
        when(delegate.getName()).thenReturn("greet");
        when(delegate.getDescription()).thenReturn("Say hello");
        when(delegate.getType()).thenReturn(CommandType.LOCAL);
        when(delegate.getAliases()).thenReturn(List.of("hi"));
        when(delegate.isHidden()).thenReturn(false);
        when(delegate.isUserInvocable()).thenReturn(true);

        wrapper = new PluginCommandWrapper("hello:greet", delegate);
    }

    @Test
    @DisplayName("getName 应返回带插件前缀的命令名 'pluginName:commandName'")
    void shouldPrefixCommandName() {
        assertEquals("hello:greet", wrapper.getName());
    }

    @Test
    @DisplayName("execute 应委托给原命令")
    void shouldDelegateExecutionToWrappedCommand() {
        CommandResult expectedResult = CommandResult.text("Hello!");
        CommandContext ctx = CommandContext.of("s1", "/tmp", "model", AppState.defaultState());
        when(delegate.execute("world", ctx)).thenReturn(expectedResult);

        CommandResult result = wrapper.execute("world", ctx);

        assertEquals(expectedResult, result);
        verify(delegate).execute("world", ctx);
    }

    @Test
    @DisplayName("getDescription 应委托给原命令")
    void shouldReturnWrappedDescription() {
        assertEquals("Say hello", wrapper.getDescription());
        verify(delegate).getDescription();
    }

    @Test
    @DisplayName("getLoadedFrom 应返回 'plugin'")
    void shouldPreserveLoadedFrom() {
        assertEquals("plugin", wrapper.getLoadedFrom());
    }

    @Test
    @DisplayName("getType 应委托给原命令")
    void shouldDelegateGetType() {
        assertEquals(CommandType.LOCAL, wrapper.getType());
        verify(delegate).getType();
    }

    @Test
    @DisplayName("getAliases 应委托给原命令")
    void shouldDelegateGetAliases() {
        assertEquals(List.of("hi"), wrapper.getAliases());
        verify(delegate).getAliases();
    }

    @Test
    @DisplayName("isHidden 应委托给原命令")
    void shouldDelegateIsHidden() {
        assertFalse(wrapper.isHidden());
        verify(delegate).isHidden();
    }

    @Test
    @DisplayName("isUserInvocable 应委托给原命令")
    void shouldDelegateIsUserInvocable() {
        assertTrue(wrapper.isUserInvocable());
        verify(delegate).isUserInvocable();
    }
}
