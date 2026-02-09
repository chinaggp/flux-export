package com.flux.export.model;

/**
 * 导出任务状态枚举
 */
public enum TaskStatus {
    /** 待处理 */
    PENDING,
    /** 处理中 */
    PROCESSING,
    /** 成功 */
    SUCCESS,
    /** 失败 */
    FAILED
}
