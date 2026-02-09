package com.flux.export.spi.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.flux.export.dto.ExportResponse;
import com.flux.export.spi.ResponseWrapperSpi;

/**
 * 默认响应包装器实现
 * <p>
 * 保持当前的 {@code ResponseEntity} 响应格式, 确保向后兼容。
 * <p>
 * 如果接入方不提供自定义 {@code ResponseWrapperSpi} 实现,
 * Spring 将自动使用此默认实现。
 */
@Component
public class DefaultResponseWrapper implements ResponseWrapperSpi {

    @Override
    public Object wrapSuccess(Object data) {
        return ResponseEntity.ok(data);
    }

    @Override
    public Object wrapError(String errorMsg, int httpStatus) {
        // 构造错误响应对象
        ExportResponse errorResponse = new ExportResponse(
                null, // taskId
                null, // status
                0, // progress
                null, // fileUrl
                errorMsg, // errorMsg
                errorMsg // message
        );
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @Override
    public Object wrapNotFound() {
        return ResponseEntity.notFound().build();
    }
}