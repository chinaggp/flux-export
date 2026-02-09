package com.flux.export.sample.pojo.params;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户导出参数
 */
@Getter
@Setter
public class UserParams {
    private String department;
    private String role;
    private String keyword;
}
