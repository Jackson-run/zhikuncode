package com.aicodeassistant.plugin;

import com.aicodeassistant.command.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 插件重载命令 — 独立 @Component，避免循环依赖。
 * 替代 ExtensionCommands 中的 @Bean reloadPluginsCommand。
 * <p>
 * @Lazy PluginManager 用于打破循环:
 * PluginManager → CommandRegistry → List<Command> → ReloadPluginsCommand → PluginManager
 */
@Component
public class ReloadPluginsCommand implements Command {
    private final PluginManager pluginManager;

    public ReloadPluginsCommand(@Lazy PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override public String getName() { return "reload-plugins"; }
    @Override public String getDescription() { return "重新加载所有插件"; }
    @Override public CommandType getType() { return CommandType.LOCAL; }

    @Override
    public CommandResult execute(String args, CommandContext context) {
        try {
            pluginManager.reloadPlugins();
            int count = pluginManager.getPluginCount();
            long enabled = pluginManager.getEnabledPlugins().size();
            return CommandResult.text("✅ Plugins reloaded: " + count
                    + " plugins (" + enabled + " enabled)");
        } catch (Exception e) {
            return CommandResult.text("❌ Plugin reload failed: " + e.getMessage());
        }
    }
}
