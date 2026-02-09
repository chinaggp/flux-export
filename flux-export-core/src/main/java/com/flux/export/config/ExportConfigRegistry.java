package com.flux.export.config;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出配置注册中心
 * <p>
 * 用于管理所有业务导出配置的注册与查询
 */
public class ExportConfigRegistry {

    private static final Map<String, ExportConfig> REGISTRY = new ConcurrentHashMap<>();

    /**
     * 注册导出配置
     *
     * @param config 导出配置
     */
    public static void register(ExportConfig config) {
        REGISTRY.put(config.bizType(), config);
    }

    /**
     * 根据业务类型获取配置
     *
     * @param bizType 业务类型
     * @return 配置（可能为空）
     */
    public static Optional<ExportConfig> get(String bizType) {
        return Optional.ofNullable(REGISTRY.get(bizType));
    }

    /**
     * 检查业务类型是否已注册
     *
     * @param bizType 业务类型
     * @return 是否已注册
     */
    public static boolean contains(String bizType) {
        return REGISTRY.containsKey(bizType);
    }

    /**
     * 获取所有已注册的配置
     *
     * @return 配置映射
     */
    public static Map<String, ExportConfig> getAll() {
        return Map.copyOf(REGISTRY);
    }
}
