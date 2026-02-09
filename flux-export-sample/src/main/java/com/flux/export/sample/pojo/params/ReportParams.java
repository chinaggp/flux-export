package com.flux.export.sample.pojo.params;

import lombok.Getter;
import lombok.Setter;

/**
 * 报表导出参数
 */
@Getter
@Setter
public class ReportParams {
    private String reportType;
    private String startDate;
    private String endDate;
}
