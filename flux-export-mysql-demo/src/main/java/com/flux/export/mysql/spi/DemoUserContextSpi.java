package com.flux.export.mysql.spi;

import org.springframework.stereotype.Component;

import com.flux.export.spi.UserContextSpi;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo 用户上下文 SPI 实现
 * <p>
 * 这是一个 Demo 实现, 返回模拟的用户 ID
 * <p>
 * <b>生产环境实现建议</b>:
 * <ul>
 * <li>从 Spring Security 的 SecurityContextHolder 获取当前认证用户</li>
 * <li>从 Session 中获取登录用户信息</li>
 * <li>从 JWT Token 中解析用户 ID</li>
 * <li>从 ThreadLocal 中获取请求上下文中的用户信息</li>
 * </ul>
 * 
 * @author FluxExport Demo
 */
@Slf4j
@Component
public class DemoUserContextSpi implements UserContextSpi {

    /**
     * 模拟用户 ID
     */
    private static final String DEMO_USER_ID = "admin";

    @Override
    public String getCurrentUserId() {
        log.debug("Getting current user ID (Demo mode): {}", DEMO_USER_ID);

        // TODO: 在实际项目中, 这里应该从真实认证机制中获取用户 ID
        return DEMO_USER_ID;
    }
}
