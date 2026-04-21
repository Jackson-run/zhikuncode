package com.aicodeassistant.config.database;

/**
 * 数据库迁移接口 — 所有迁移脚本必须实现。
 * 迁移应为幂等操作（使用 IF NOT EXISTS）。
 *
 */
public interface Migration {
    void execute();
}
