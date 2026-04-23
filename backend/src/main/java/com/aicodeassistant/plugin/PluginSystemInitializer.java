package com.aicodeassistant.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PluginSystemInitializer {

    private static final Logger log = LoggerFactory.getLogger(PluginSystemInitializer.class);
    private final PluginManager pluginManager;
    private final Environment environment;

    public PluginSystemInitializer(PluginManager pluginManager, Environment environment) {
        this.pluginManager = pluginManager;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(100) // 在 MCP (phase=2) 和其他服务之后
    public void onApplicationReady() {
        boolean pluginEnabled = environment.getProperty(
                "plugin.enabled", Boolean.class, true);
        if (!pluginEnabled) {
            log.info("Plugin system disabled by configuration (plugin.enabled=false)");
            return;
        }
        log.info("Initializing plugin system on application ready...");
        try {
            pluginManager.initializePlugins();
        } catch (Exception e) {
            // 插件系统初始化失败不阻止主应用启动
            log.error("Plugin system initialization failed (non-fatal): {}", e.getMessage(), e);
        }
    }
}
