package com.flux.export.spi;

/**
 * 响应包装器 SPI
 * <p>
 * 允许接入方自定义 Controller 的响应格式, 以适配不同项目的统一响应规范。
 * <p>
 * <b>使用场景:</b>
 * <ul>
 * <li>项目 A: 标准 REST 风格 - 直接返回业务对象</li>
 * <li>项目 B: 统一响应包装 - {"code": 200, "msg": "success", "data": {...}}</li>
 * <li>项目 C: 业务码分离 - {"success": true, "result": {...}, "errorMsg": null}</li>
 * </ul>
 * <p>
 * <b>默认行为:</b> 如果不提供自定义实现, SDK 将使用 {@code DefaultResponseWrapper},
 * 保持当前的 {@code ResponseEntity} 响应格式。
 *
 * @see com.flux.export.spi.impl.DefaultResponseWrapper
 */
public interface ResponseWrapperSpi {

    /**
     * 包装成功响应
     * <p>
     * 将业务数据包装成项目的统一响应格式。
     *
     * @param data 业务数据 (通常是 ExportResponse 或 List&lt;ExportResponse&gt;)
     * @return 包装后的响应对象 (可以是 ResponseEntity、自定义 DTO 等)
     */
    Object wrapSuccess(Object data);

    /**
     * 包装错误响应
     * <p>
     * 将错误信息包装成项目的统一错误响应格式。
     *
     * @param errorMsg   错误信息
     * @param httpStatus HTTP 状态码 (如 400, 403, 500)
     * @return 包装后的错误响应对象
     */
    Object wrapError(String errorMsg, int httpStatus);

    /**
     * 包装 404 响应
     * <p>
     * 当资源不存在时 (如任务 ID 不存在), 返回的响应格式。
     *
     * @return 包装后的 404 响应对象
     */
    Object wrapNotFound();
}
