package com.flux.export.sample.config;

import com.flux.export.config.ExportConfig;
import com.flux.export.config.ExportConfigRegistry;
import com.flux.export.sample.pojo.params.OrderParams;
import com.flux.export.sample.pojo.params.ProductParams;
import com.flux.export.sample.pojo.params.ReportParams;
import com.flux.export.sample.pojo.params.UserParams;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 导出业务配置
 */
@Component
public class ExportBusinessConfig {

    @PostConstruct
    public void registerExportConfigs() {
        ExportConfigRegistry.register(new ExportConfig(
                "ORDER_EXPORT",
                "orderService",
                "queryOrders",
                OrderParams.class,
                "orderService"
        ));

        ExportConfigRegistry.register(new ExportConfig(
                "USER_EXPORT",
                "userService",
                "queryUsers",
                UserParams.class,
                "userPermissionChecker"
        ));

        ExportConfigRegistry.register(new ExportConfig(
                "PRODUCT_EXPORT",
                "productService",
                "queryProducts",
                ProductParams.class
        ));

        ExportConfigRegistry.register(new ExportConfig(
                "REPORT_EXPORT",
                "reportService",
                "queryReports",
                ReportParams.class
        ));
    }
}