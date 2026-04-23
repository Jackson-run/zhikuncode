package com.aicodeassistant.plugin.builtin;

import com.aicodeassistant.command.*;
import com.aicodeassistant.plugin.*;
import com.aicodeassistant.tool.*;

import java.util.List;
import java.util.Map;

/**
 * Hello 示例插件 — 验证插件系统端到端链路。
 * <p>
 * 提供:
 * - /hello:greet 命令
 * - hello_echo 工具 (LLM 可调用)
 * - PostToolUse 日志钩子
 */
public class HelloPlugin implements PluginExtension {

    @Override public String name() { return "hello"; }
    @Override public String version() { return "1.0.0"; }
    @Override public String description() { return "示例插件 — 验证插件系统端到端链路"; }
    @Override public int priority() { return 50; }

    @Override
    public void onLoad(PluginContext ctx) {
        ctx.getLogger().info("Hello plugin loaded! API version: {}",
                ctx.getHostApiVersion());
    }

    @Override
    public List<Command> getCommands() {
        return List.of(new Command() {
            @Override public String getName() { return "greet"; }
            @Override public String getDescription() { return "向用户打招呼"; }
            @Override public CommandType getType() { return CommandType.LOCAL; }
            @Override
            public CommandResult execute(String args, CommandContext context) {
                String name = (args == null || args.isBlank()) ? "World" : args.trim();
                return CommandResult.text("Hello, " + name + "! (from hello plugin)");
            }
        });
    }

    @Override
    public List<Tool> getTools() {
        return List.of(new Tool() {
            @Override public String getName() { return "hello_echo"; }
            @Override public String getDescription() {
                return "Echo tool from hello plugin — 回显输入内容";
            }
            @Override public Map<String, Object> getInputSchema() {
                return Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "message", Map.of("type", "string",
                                "description", "要回显的消息")
                    ),
                    "required", List.of("message")
                );
            }
            @Override
            public ToolResult call(ToolInput input, ToolUseContext context) {
                String message = input.getString("message");
                return ToolResult.success("[hello_echo] " + message);
            }
            @Override public boolean isReadOnly(ToolInput input) { return true; }
        });
    }

    @Override
    public List<HookHandler> getHooks() {
        return List.of(
            HookHandler.postToolUse("hello_echo", 999, ctx -> {
                // 低优先级日志钩子 — 不修改结果，仅记录
                return HookHandler.HookResult.allow();
            })
        );
    }
}
