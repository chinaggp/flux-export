package com.flux.export.spi;

import java.util.List;
import java.util.Optional;

import com.flux.export.model.ExportTask;

/**
 * 任务持久化 SPI 接口
 * <p>
 * 接入方需实现此接口，将任务状态持久化到数据库（如 MySQL、Oracle 等）
 */
public interface TaskPersistenceSpi {

    /**
     * 创建导出任务
     *
     * @param task 任务对象
     */
    void createTask(ExportTask task);

    /**
     * 更新任务进度
     * <p>
     * 注意：建议在实现层加入防抖逻辑，避免频繁写库
     *
     * @param taskId   任务 ID
     * @param progress 进度 (0-100)
     */
    void updateProgress(String taskId, int progress);

    /**
     * 更新任务状态为成功
     *
     * @param taskId  任务 ID
     * @param fileUrl 文件下载地址
     */
    void markSuccess(String taskId, String fileUrl);

    /**
     * 更新任务状态为失败
     *
     * @param taskId   任务 ID
     * @param errorMsg 错误信息
     */
    void markFailed(String taskId, String errorMsg);

    /**
     * 根据 任务 ID 查询任务
     *
     * @param taskId 任务 ID
     * @return 任务对象
     */
    Optional<ExportTask> findById(String taskId);

    /**
     * 查询用户的任务列表
     *
     * @param userId 用户 ID
     * @param limit  限制数量
     * @return 任务列表
     */
    List<ExportTask> findByUserId(String userId, int limit);
}
