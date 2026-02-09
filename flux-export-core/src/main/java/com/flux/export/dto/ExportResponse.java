package com.flux.export.dto;

import com.flux.export.model.TaskStatus;

/**
 * 导出响应 DTO
 *
 * @param taskId   任务 ID
 * @param status   任务状态
 * @param progress 进度 (0-100)
 * @param fileUrl  文件下载地址（成功时返回）
 * @param errorMsg 错误信息（失败时返回）
 * @param message  提示信息
 */
public record ExportResponse(
        String taskId,
        TaskStatus status,
        int progress,
        String fileUrl,
        String errorMsg,
        String message
) {
    /**
     * 创建提交成功响应
     */
    public static ExportResponse submitted(String taskId) {
        return new ExportResponse(taskId, TaskStatus.PENDING, 0, null, null, "任务已提交");
    }

    /**
     * 创建查询响应
     */
    public static ExportResponse fromTask(com.flux.export.model.ExportTask task) {
        String message = switch (task.status()) {
            case PENDING -> "任务等待处理";
            case PROCESSING -> "任务处理中，进度: " + task.progress() + "%";
            case SUCCESS -> "导出成功";
            case FAILED -> "导出失败: " + task.errorMsg();
        };
        return new ExportResponse(
                task.taskId(),
                task.status(),
                task.progress(),
                task.fileUrl(),
                task.errorMsg(),
                message
        );
    }
}