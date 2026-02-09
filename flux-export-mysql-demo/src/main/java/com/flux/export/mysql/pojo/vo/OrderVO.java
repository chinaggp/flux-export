package com.flux.export.mysql.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单视图对象（用于 Excel 导出） */
@Setter
@Getter
public class OrderVO {
    @ExcelProperty("订单号")
    private String orderId;

    @ExcelProperty("商品名称")
    private String productName;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    public OrderVO() {
    }

    public OrderVO(String orderId, String productName, BigDecimal amount, String status, LocalDateTime createTime) {
        this.orderId = orderId;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.createTime = createTime;
    }

}
