package com.flux.export.mysql.spi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.flux.export.model.ExportTask;
import com.flux.export.spi.TaskPersistenceSpi;
import com.flux.export.mysql.converter.ExportTaskConverter;
import com.flux.export.mysql.pojo.entity.ExportTaskEntity;
import com.flux.export.mysql.service.ExportTaskService;

import lombok.extern.slf4j.Slf4j;

/**
 * MySQL 任务持久化 SPI 实现 (基于 MyBatis-Plus)
 *
 * @author FluxExport Demo
 */
@Slf4j
@Component
public class MySqlTaskPersistenceSpi implements TaskPersistenceSpi {

    private final ExportTaskService exportTaskService;

    public MySqlTaskPersistenceSpi(ExportTaskService exportTaskService) {
        this.exportTaskService = exportTaskService;
    }

    @Override
    public void createTask(ExportTask coreTask) {
        log.debug("Creating task: {}", coreTask.taskId());

        // 类型转换: Core ExportTaskEntity -> Demo ExportTaskEntity
        ExportTaskEntity demoTask = ExportTaskConverter.toDemoTask(coreTask);

        // 调用 Service 层创建任务
        exportTaskService.createTask(demoTask);
    }

    @Override
    public void updateProgress(String taskId, int progress) {
        log.debug("Updating progress for task: {}, progress: {}", taskId, progress);

        // Service 层已实现防抖机制
        exportTaskService.updateProgress(taskId, progress);
    }

    @Override
    public void markSuccess(String taskId, String fileUrl) {
        log.debug("Marking task as success: {}, fileUrl: {}", taskId, fileUrl);

        // 从 URL 提取文件名
        String fileName = extractFileNameFromUrl(fileUrl);

        // 调用 Service 层标记成功
        exportTaskService.markSuccess(taskId, fileUrl, fileName);
    }

    @Override
    public void markFailed(String taskId, String errorMsg) {
        log.debug("Marking task as failed: {}, error: {}", taskId, errorMsg);

        // 调用 Service 层标记失败
        exportTaskService.markFailed(taskId, errorMsg);
    }

    @Override
    public Optional<com.flux.export.model.ExportTask> findById(String taskId) {
        log.debug("Finding task by id: {}", taskId);

        // 查询 Demo ExportTaskEntity
        Optional<ExportTaskEntity> demoTaskOpt = exportTaskService.findById(taskId);

        // 类型转换: Demo ExportTaskEntity -> Core ExportTaskEntity
        return demoTaskOpt.map(ExportTaskConverter::toCoreTask);
    }

    @Override
    public List<com.flux.export.model.ExportTask> findByUserId(String userId, int limit) {
        log.debug("Finding tasks by userId: {}, limit: {}", userId, limit);

        // 查询 Demo ExportTaskEntity 列表
        List<ExportTaskEntity> demoTasks = exportTaskService.findByUserId(userId, limit);

        // 类型转换: List<Demo ExportTaskEntity> -> List<Core ExportTaskEntity>
        return demoTasks.stream()
                .map(ExportTaskConverter::toCoreTask)
                .collect(Collectors.toList());
    }

    /**
     * 从 URL 提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "export.xlsx";
        }
        try {
            // 使用 URI 解析路径部分，去除 query param
            String path = new java.net.URI(fileUrl).getPath();
            if (path == null) {
                return "export.xlsx";
            }
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
                return path.substring(lastSlashIndex + 1);
            }
        } catch (Exception e) {
            log.warn("Failed to parse file name from url: {}", fileUrl);
        }
        return "export.xlsx"; // 默认文件名
    }
}
