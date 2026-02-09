package com.flux.export.spi;

/**
 * 用户上下文 SPI 接口
 * <p>
 * 接入方需实现此接口, 提供当前用户信息
 * <p>
 * <b>注意</b>: 此接口应该只有一个全局实现, 用于统一获取用户上下文信息
 */
public interface UserContextSpi {

    /**
     * 获取当前用户 ID
     * <p>
     * 通常从 SecurityContext 或 Session 中获取
     *
     * @return 当前用户 ID
     */
    String getCurrentUserId();
}
