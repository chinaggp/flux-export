package com.flux.export.sample.pojo.params;

import lombok.Getter;
import lombok.Setter;

/**
 * 产品导出参数
 */
@Getter
@Setter
public class ProductParams {
    private String category;
    private String keyword;
    private String priceRange;
}
