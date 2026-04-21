package com.aicodeassistant.plugin;

import java.util.List;

/**
 * 插件加载结果 — 包含已启用、已禁用插件和错误列表。
 *
 * @param enabled  已启用的插件
 * @param disabled 已禁用的插件
 * @param errors   加载错误
 */
public record PluginLoadResult(
        List<LoadedPlugin> enabled,
        List<LoadedPlugin> disabled,
        List<PluginError> errors
) {

    /** 空结果 */
    public static PluginLoadResult empty() {
        return new PluginLoadResult(List.of(), List.of(), List.of());
    }

    /** 总插件数 */
    public int totalCount() {
        return enabled.size() + disabled.size();
    }
}
