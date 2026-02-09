package com.flux.export.sample.demo;

import com.flux.export.spi.PermissionCheckerSpi;
import com.flux.export.sample.pojo.params.OrderParams;
import com.flux.export.sample.pojo.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单导出示例
 */
@Slf4j
@Component
public class OrderService implements PermissionCheckerSpi {
    /**
     * 查询订单数据
     */
    public List<OrderVO> queryOrders(OrderParams params) {
        List<OrderVO> orders = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            orders.add(new OrderVO(
                    "ORD" + String.format("%08d", i),
                    "商品" + i,
                    BigDecimal.valueOf(99.99 * i),
                    i % 3 == 0 ? "已完成" : (i % 3 == 1 ? "待支付" : "已发货"),
                    LocalDateTime.now().minusDays(i)));
        }
        return orders;
    }

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