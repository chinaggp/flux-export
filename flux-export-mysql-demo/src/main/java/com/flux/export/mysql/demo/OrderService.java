package com.flux.export.mysql.demo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flux.export.spi.PermissionCheckerSpi;
import com.flux.export.mysql.mapper.OrderMapper;
import com.flux.export.mysql.pojo.entity.OrderEntity;
import com.flux.export.mysql.pojo.params.OrderParams;
import com.flux.export.mysql.pojo.vo.OrderVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单导出示例
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderService implements PermissionCheckerSpi {

    private final OrderMapper orderMapper;

    /**
     * 查询订单数据（业务方法）
     */
    public List<OrderVO> queryOrders(OrderParams params) {
        log.info("从数据库查询订单数据, 参数: {}", params);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (params != null) {
            if (org.springframework.util.StringUtils.hasText(params.getStatus())) {
                queryWrapper.eq(OrderEntity::getStatus, params.getStatus());
            }
            if (org.springframework.util.StringUtils.hasText(params.getStartTime())) {
                queryWrapper.ge(OrderEntity::getCreateTime, params.getStartTime());
            }
            if (org.springframework.util.StringUtils.hasText(params.getEndTime())) {
                queryWrapper.le(OrderEntity::getCreateTime, params.getEndTime());
            }
        }

        List<OrderEntity> orderEntities = orderMapper.selectList(queryWrapper);

        return orderEntities.stream()
                .map(entity -> new OrderVO(
                        entity.getOrderNo(),
                        entity.getProductName(),
                        entity.getAmount(),
                        entity.getStatus(),
                        entity.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(String userId, String bizType) {
        log.info("检查用户 {} 的订单导出权限", userId);

        // 示例逻辑:只有特定用户可以导出订单
        if ("admin".equals(userId) || "order-manager".equals(userId)) {
            log.info("用户 {} 有订单导出权限", userId);
            return true;
        }

        log.warn("用户 {} 无订单导出权限", userId);
        return false;
    }
}
