package com.flux.export.sample.spi;

import org.springframework.stereotype.Component;

import com.flux.export.spi.UserContextSpi;

/**
 * 默认用户上下文实现
 * <p>
 * 全局唯一的用户上下文实现, 负责获取当前用户信息
 */
@Component
public class DefaultUserContextSpi implements UserContextSpi {

    @Override
    public String getCurrentUserId() {
        // 默认返回固定用户 ID
        // 实际项目中应从 SecurityContext 或 Session 中获取
        // 例如: SecurityContextHolder.getContext().getAuthentication().getName()
        return "admin";
    }
}
