package com.aicodeassistant.memdir;

import com.aicodeassistant.engine.SideQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MemoryRerankService — 使用 LLM 对记忆搜索结果进行精排。
 * <p>
 * BM25 候选集经过 LLM 重排后，按语义相关度返回 Top-K 结果。
 * 降级策略: LLM 调用失败时返回原始 BM25 排序结果。
 *
 * @see MemdirService#searchMemories(String, int)
 */
@Service
public class MemoryRerankService {

    private static final Logger log = LoggerFactory.getLogger(MemoryRerankService.class);

    private static final String RERANK_SYSTEM_PROMPT = """
            你是一个相关性排序系统。给定一个查询和一组记忆条目，
            对每个条目与查询的相关性打分，分值范围为 0.0 到 1.0。
            
            返回严格符合以下格式的 JSON 对象：
            {"results": [{"index": 0, "relevanceScore": 0.95}, {"index": 1, "relevanceScore": 0.3}, ...]}
            
            规则：
            - 基于语义相关性打分，而非仅仅关键词匹配
            - 必须为所有条目返回分数
            - 分数越高表示与查询越相关
            - 仅返回 JSON，绝不附加任何额外文本""";

    private static final long RERANK_TIMEOUT_MS = 3000L;
    private static final int RERANK_MAX_TOKENS = 512;

    private final SideQueryService sideQueryService;
    private final boolean enabled;

    public MemoryRerankService(SideQueryService sideQueryService,
                               @Value("${app.memory.rerank-enabled:true}") boolean enabled) {
        this.sideQueryService = sideQueryService;
        this.enabled = enabled;
    }

    /**
     * 是否启用 LLM rerank。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 对候选记忆列表进行 LLM 精排。
     *
     * @param query      用户查询
     * @param candidates BM25 候选集
     * @param topK       返回前 K 条
     * @return 精排后的 Top-K 记忆列表；LLM 失败时返回原始前 K 条
     */
    public List<MemdirService.Memory> rerank(String query, List<MemdirService.Memory> candidates, int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (!enabled || candidates.size() <= topK) {
            return candidates.subList(0, Math.min(topK, candidates.size()));
        }

        try {
            // 构建用户内容: query + 候选条目
            StringBuilder userContent = new StringBuilder();
            userContent.append("Query: ").append(query).append("\n\nEntries:\n");
            for (int i = 0; i < candidates.size(); i++) {
                MemdirService.Memory mem = candidates.get(i);
                // 截取前200字符避免 prompt 过大
                String snippet = mem.content().length() > 200
                        ? mem.content().substring(0, 200) + "..."
                        : mem.content();
                userContent.append("[").append(i).append("] ").append(snippet).append("\n");
            }

            // 调用 LLM 结构化查询
            RerankResponse response = sideQueryService.queryStructured(
                    RERANK_SYSTEM_PROMPT,
                    userContent.toString(),
                    RerankResponse.class,
                    RERANK_MAX_TOKENS,
                    RERANK_TIMEOUT_MS);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                log.warn("Rerank returned empty response, falling back to BM25 order");
                return candidates.subList(0, Math.min(topK, candidates.size()));
            }

            // 按 relevanceScore 降序排列，取 Top-K
            List<MemdirService.Memory> reranked = new ArrayList<>();
            response.results().stream()
                    .filter(r -> r.index() >= 0 && r.index() < candidates.size())
                    .sorted(Comparator.comparingDouble(RerankResult::relevanceScore).reversed())
                    .limit(topK)
                    .forEach(r -> reranked.add(candidates.get(r.index())));

            log.debug("Rerank completed: {} candidates → {} results", candidates.size(), reranked.size());
            return reranked;

        } catch (Exception e) {
            log.warn("Rerank failed, falling back to BM25 order: {}", e.getMessage());
            return candidates.subList(0, Math.min(topK, candidates.size()));
        }
    }

    // ───── DTOs ─────

    /**
     * LLM 返回的 rerank 结果包装。
     */
    public record RerankResponse(List<RerankResult> results) {}

    /**
     * 单条 rerank 结果。
     */
    public record RerankResult(int index, double relevanceScore) {}
}
