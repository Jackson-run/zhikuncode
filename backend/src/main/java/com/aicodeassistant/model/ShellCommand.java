package com.aicodeassistant.model;

import java.util.List;

/**
 * 解析后的 Shell 命令结构体（占位 — 完整实现见 Bash 解析器模块）。
 *
 */
public record ShellCommand(
        String raw,
        List<String> parts,
        boolean isPiped
) {}
