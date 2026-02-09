package com.flux.export.spi;

/**
 * 权限校验 SPI 接口
 * <p>
 * 接入方可为不同业务实现此接口, 提供差异化的权限校验逻辑
 * <p>
 * 与 {@link UserContextSpi} 不同, 此接口可以有多个实现, 每个业务可以配置自己的权限校验器
 */
public interface PermissionCheckerSpi {

    /**
     * 检查用户是否有权限导出指定业务类型的数据
     *
     * @param userId  用户 ID
     * @param bizType 业务类型标识
     * @return 是否有权限
     */
    boolean hasPermission(String userId, String bizType);
}
