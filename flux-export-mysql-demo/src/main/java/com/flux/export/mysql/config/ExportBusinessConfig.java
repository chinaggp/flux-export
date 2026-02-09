package com.flux.export.mysql.config;

import com.flux.export.config.ExportConfig;
import com.flux.export.config.ExportConfigRegistry;
import com.flux.export.mysql.pojo.params.OrderParams;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 导出业务配置示例
 * <p>
 * 展示如何注册不同的导出业务，包括使用自定义权限校验器
 */
@Component
public class ExportBusinessConfig {

    /**
     * 在应用启动时注册所有导出业务配置
     */
    @PostConstruct
    public void registerExportConfigs() {

        // 示例 1: 订单导出 - 使用自定义权限校验器
        ExportConfigRegistry.register(new ExportConfig(
                "ORDER_EXPORT", // 业务类型
                "orderService", // Service Bean 名称
                "queryOrders", // 方法名
                OrderParams.class, // 参数类型
                "orderService" // 自定义权限校验器 Bean 名称
        ));
    }
}
