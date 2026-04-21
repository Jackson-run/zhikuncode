package com.aicodeassistant.tool;

/**
 * 搜索/读取信息 — isSearchOrReadCommand 的返回类型。
 * 用于 UI 折叠展示和自动批准策略判断。
 *
 */
public record SearchReadInfo(
        boolean isSearch,
        boolean isRead,
        boolean isList
) {
    public static final SearchReadInfo NONE = new SearchReadInfo(false, false, false);
}
