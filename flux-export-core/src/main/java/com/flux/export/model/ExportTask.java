package com.flux.export.model;

import java.time.LocalDateTime;

/**
 * 导出任务实体
 *
 * @param taskId     任务唯一 ID
 * @param bizType    业务类型标识
 * @param userId     触发用户 ID
 * @param status     任务状态
 * @param progress   进度 (0-100)
 * @param fileUrl    文件下载地址
 * @param fileName   文件名
 * @param errorMsg   错误信息
 * @param params     请求参数对象快照
 * @param createTime 创建时间
 * @param finishTime 完成时间
 */
public record ExportTask(
        String taskId,
        String bizType,
        String userId,
        TaskStatus status,
        int progress,
        String fileUrl,
        String fileName,
        String errorMsg,
        Object params,
        LocalDateTime createTime,
        LocalDateTime finishTime) {
    /**
     * 创建一个新的待处理任务
     */
    public static ExportTask createPending(String taskId, String bizType, String userId, Object params) {
        return new ExportTask(
                taskId,
                bizType,
                userId,
                TaskStatus.PENDING,
                0,
                null,
                null,
                null,
                params,
                LocalDateTime.now(),
                null);
    }

    /**
     * 复制并更新状态为处理中
     */
    public ExportTask withProcessing() {
        return new ExportTask(taskId, bizType, userId, TaskStatus.PROCESSING, progress, fileUrl, fileName, errorMsg,
                params, createTime, finishTime);
    }

    /**
     * 复制并更新进度
     */
    public ExportTask withProgress(int newProgress) {
        return new ExportTask(taskId, bizType, userId, status, newProgress, fileUrl, fileName, errorMsg, params,
                createTime, finishTime);
    }

    /**
     * 复制并更新为成功状态
     */
    public ExportTask withSuccess(String url, String name) {
        return new ExportTask(taskId, bizType, userId, TaskStatus.SUCCESS, 100, url, name, null, params, createTime,
                LocalDateTime.now());
    }

    /**
     * 复制并更新为失败状态
     */
    public ExportTask withFailed(String error) {
        return new ExportTask(taskId, bizType, userId, TaskStatus.FAILED, progress, fileUrl, fileName, error, params,
                createTime, LocalDateTime.now());
    }
}
