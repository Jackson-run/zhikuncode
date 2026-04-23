package com.aicodeassistant.controller;

import com.aicodeassistant.plugin.LoadedPlugin;
import com.aicodeassistant.plugin.PluginLoader;
import com.aicodeassistant.plugin.PluginManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 插件管理 Controller — 管理插件的安装、卸载和查询。
 *
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private final PluginManager pluginManager;
    private final PluginLoader pluginLoader;

    public PluginController(PluginManager pluginManager, PluginLoader pluginLoader) {
        this.pluginManager = pluginManager;
        this.pluginLoader = pluginLoader;
    }

    /** 列出已安装的插件 */
    @GetMapping
    public ResponseEntity<Map<String, List<PluginInfo>>> listPlugins() {
        List<PluginInfo> plugins = pluginManager.getLoadedPlugins().stream()
                .map(p -> new PluginInfo(
                        p.name(),
                        p.manifest() != null ? p.manifest().version() : "unknown",
                        p.manifest() != null ? p.manifest().description() : "",
                        p.enabled(),
                        p.isBuiltin(),
                        p.sourceType().name(),
                        p.commands().size(),
                        p.tools().size(),
                        p.hooks().size()))
                .toList();
        return ResponseEntity.ok(Map.of("plugins", plugins));
    }

    /** 安装插件 — 上传 JAR 文件安装到本地插件目录 */
    @PostMapping("/install")
    public ResponseEntity<Map<String, Object>> installPlugin(
            @RequestParam("file") MultipartFile file) {
        // 1. 文件名安全检查
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.endsWith(".jar")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only .jar files are accepted"));
        }
        // 路径穿越防护
        String safeName = Path.of(originalName).getFileName().toString();
        if (!safeName.equals(originalName)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid file name"));
        }
        // 2. 大小限制
        long maxSize = pluginLoader.getMaxJarSizeBytes();
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "File too large, max " + (maxSize / 1024 / 1024) + "MB"));
        }
        // 3. 保存到插件目录
        try {
            String home = System.getProperty("user.home", ".");
            Path pluginDir = Path.of(home, ".zhikun", "plugins");
            Files.createDirectories(pluginDir);
            Path target = pluginDir.resolve(safeName);
            file.transferTo(target);
            // 4. 重载以加载新插件
            pluginManager.reloadPlugins();
            return ResponseEntity.ok(Map.of(
                    "installed", safeName,
                    "loaded", pluginManager.getPluginCount()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Installation failed: " + e.getMessage()));
        }
    }

    /** 删除插件 */
    @DeleteMapping("/{pluginId}")
    public ResponseEntity<Map<String, Object>> deletePlugin(@PathVariable String pluginId) {
        // 查找插件
        var plugin = pluginManager.getPlugin(pluginId);
        if (plugin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // 不允许删除内置插件
        if (plugin.get().isBuiltin()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot delete builtin plugin"));
        }
        // 获取 JAR 路径并删除
        String pluginPath = plugin.get().path();
        if (pluginPath != null) {
            try {
                Path path = Path.of(pluginPath);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Failed to delete JAR: " + e.getMessage()));
            }
        }
        // 重载
        pluginManager.reloadPlugins();
        return ResponseEntity.ok(Map.of("deleted", pluginId));
    }

    /** 重新加载所有插件 */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadPlugins() {
        pluginManager.reloadPlugins();
        var plugins = pluginManager.getLoadedPlugins();
        long enabledCount = plugins.stream().filter(LoadedPlugin::enabled).count();
        long disabledCount = plugins.stream().filter(p -> !p.enabled()).count();
        return ResponseEntity.ok(Map.of(
                "loaded", pluginManager.getPluginCount(),
                "enabled", enabledCount,
                "disabled", disabledCount));
    }

    // ═══ DTO Records ═══
    public record PluginInfo(String name, String version, String description,
                             boolean enabled, boolean isBuiltin, String sourceType,
                             int commandCount, int toolCount, int hookCount) {}

}
