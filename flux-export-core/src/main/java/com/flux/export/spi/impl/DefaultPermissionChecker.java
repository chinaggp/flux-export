package com.flux.export.spi.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.flux.export.spi.PermissionCheckerSpi;

/**
 * 默认权限校验器
 * <p>
 * 默认实现, 允许所有用户导出
 */
@Primary
@Component("defaultPermissionChecker")
public class DefaultPermissionChecker implements PermissionCheckerSpi {

    @Override
    public boolean hasPermission(String userId, String bizType) {
        // 默认允许所有导出
        // 实际项目中应接入权限系统 (如 RBAC)
        return true;
    }
}
