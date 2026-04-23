package com.aicodeassistant.plugin;

import com.aicodeassistant.command.*;
import java.util.List;

/**
 * 插件命令包装器 — 将插件命令包装为带前缀名称的系统命令。
 * 命令名格式: "pluginName:commandName"
 */
public class PluginCommandWrapper implements Command {

    private final String prefixedName;
    private final Command delegate;

    public PluginCommandWrapper(String prefixedName, Command delegate) {
        this.prefixedName = prefixedName;
        this.delegate = delegate;
    }

    @Override public String getName() { return prefixedName; }
    @Override public String getDescription() { return delegate.getDescription(); }
    @Override public CommandType getType() { return delegate.getType(); }
    @Override public List<String> getAliases() { return delegate.getAliases(); }
    @Override public boolean isHidden() { return delegate.isHidden(); }
    @Override public boolean isUserInvocable() { return delegate.isUserInvocable(); }
    @Override public String getLoadedFrom() { return "plugin"; }

    @Override
    public CommandResult execute(String args, CommandContext context) {
        return delegate.execute(args, context);
    }
}
