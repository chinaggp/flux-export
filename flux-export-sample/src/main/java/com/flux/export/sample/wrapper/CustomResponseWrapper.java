package com.flux.export.sample.wrapper;

import com.flux.export.spi.ResponseWrapperSpi;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 自定义响应包装器示例
 */
@Primary
@Component
public class CustomResponseWrapper implements ResponseWrapperSpi {

    @Override
    public Object wrapSuccess(Object data) {
        return new ApiResponse<>(200, "success", data);
    }

    @Override
    public Object wrapError(String errorMsg, int httpStatus) {
        return new ApiResponse<>(httpStatus, errorMsg, null);
    }

    @Override
    public Object wrapNotFound() {
        return new ApiResponse<>(404, "任务不存在", null);
    }

    /**
     * 统一响应格式
     */
    public record ApiResponse<T>(int code, String msg, T data) {
    }
}