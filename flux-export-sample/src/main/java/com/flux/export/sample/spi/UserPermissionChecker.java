package com.flux.export.sample.spi;

import com.flux.export.spi.PermissionCheckerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户导出权限校验器
 */
@Component("userPermissionChecker")
public class UserPermissionChecker implements PermissionCheckerSpi {

    private static final Logger log = LoggerFactory.getLogger(UserPermissionChecker.class);

    @Override
    public boolean hasPermission(String userId, String bizType) {
        log.info("检查用户 {} 的用户数据导出权限", userId);
        if ("admin".equals(userId)) {
            log.info("用户 {} 有用户数据导出权限", userId);
            return true;
        }
        log.warn("用户 {} 无用户数据导出权限, 需要管理员权限", userId);
        return false;
    }
}