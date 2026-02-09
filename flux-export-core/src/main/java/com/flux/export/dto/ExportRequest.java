package com.flux.export.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 导出请求 DTO
 *
 * @param bizType 业务类型标识
 * @param params  业务参数对象
 */
public record ExportRequest(
                @NotBlank(message = "业务类型不能为空") String bizType,
                Object params) {

}
