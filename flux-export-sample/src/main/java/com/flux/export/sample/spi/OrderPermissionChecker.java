package com.flux.export.sample.spi;

import com.flux.export.spi.PermissionCheckerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 订单导出权限校验器
 */
@Component("orderPermissionChecker")
public class OrderPermissionChecker implements PermissionCheckerSpi {

    private static final Logger log = LoggerFactory.getLogger(OrderPermissionChecker.class);

    @Override
    public boolean hasPermission(String userId, String bizType) {
        log.info("检查用户 {} 的订单导出权限", userId);
        if ("admin".equals(userId) || "order-manager".equals(userId)) {
            log.info("用户 {} 有订单导出权限", userId);
            return true;
        }
        log.warn("用户 {} 无订单导出权限", userId);
        return false;
    }
}