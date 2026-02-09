package com.flux.export.sample.pojo.params;

import lombok.Getter;
import lombok.Setter;

/**
 * 订单导出参数
 */
@Getter
@Setter
public class OrderParams {
    private String status;
    private String startTime;
    private String endTime;
}
