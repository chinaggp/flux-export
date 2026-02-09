package com.flux.export.mysql.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 订单实体类 */
@Data
@TableName("t_order")
public class OrderEntity {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号 */
    private String orderNo;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 状态 */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
