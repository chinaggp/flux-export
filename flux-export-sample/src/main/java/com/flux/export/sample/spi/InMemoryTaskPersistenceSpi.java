package com.flux.export.sample.spi;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.flux.export.model.ExportTask;
import com.flux.export.model.TaskStatus;
import com.flux.export.spi.TaskPersistenceSpi;

/**
 * TaskPersistenceSPI 内存实现(带防抖)
 * <p>
 * 适用于开发测试环境,生产环境请使用数据库实现
 */
@Component
public class InMemoryTaskPersistenceSpi implements TaskPersistenceSpi {

    private static final Logger log = LoggerFactory.getLogger(InMemoryTaskPersistenceSpi.class);
    private static final long THROTTLE_MS = 1000; // 防抖间隔: 1 秒
    private final Map<String, ExportTask> taskStore = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdateTime = new ConcurrentHashMap<>();

    @Override
    public void createTask(ExportTask task) {
        taskStore.put(task.taskId(), task);
        log.info("创建任务: taskId={}, bizType={}", task.taskId(), task.bizType());
    }

    @Override
    public void updateProgress(String taskId, int progress) {
        // 防抖逻辑
        long now = System.currentTimeMillis();
        Long lastTime = lastUpdateTime.get(taskId);
        if (lastTime != null && (now - lastTime) < THROTTLE_MS && progress < 100) {
            log.debug("防抖跳过进度更新: taskId={}, progress={}", taskId, progress);
            return;
        }
        lastUpdateTime.put(taskId, now);

        taskStore.computeIfPresent(taskId, (id, task) -> {
            ExportTask updated = new ExportTask(
                    task.taskId(), task.bizType(), task.userId(),
                    TaskStatus.PROCESSING, progress,
                    task.fileUrl(), task.fileName(), task.errorMsg(),
                    task.params(), task.createTime(), task.finishTime());
            log.info("更新进度: taskId={}, progress={}", taskId, progress);
            return updated;
        });
    }

    @Override
    public void markSuccess(String taskId, String fileUrl) {
        taskStore.computeIfPresent(taskId, (id, task) -> {
            ExportTask updated = new ExportTask(
                    task.taskId(), task.bizType(), task.userId(),
                    TaskStatus.SUCCESS, 100,
                    fileUrl, extractFileName(fileUrl), null,
                    task.params(), task.createTime(), LocalDateTime.now());
            log.info("任务成功: taskId={}, fileUrl={}", taskId, fileUrl);
            return updated;
        });
        lastUpdateTime.remove(taskId);
    }

    @Override
    public void markFailed(String taskId, String errorMsg) {
        taskStore.computeIfPresent(taskId, (id, task) -> {
            ExportTask updated = new ExportTask(
                    task.taskId(), task.bizType(), task.userId(),
                    TaskStatus.FAILED, task.progress(),
                    task.fileUrl(), task.fileName(), errorMsg,
                    task.params(), task.createTime(), LocalDateTime.now());
            log.error("任务失败: taskId={}, error={}", taskId, errorMsg);
            return updated;
        });
        lastUpdateTime.remove(taskId);
    }

    @Override
    public Optional<ExportTask> findById(String taskId) {
        return Optional.ofNullable(taskStore.get(taskId));
    }

    @Override
    public List<ExportTask> findByUserId(String userId, int limit) {
        return taskStore.values().stream()
                .filter(task -> task.userId().equals(userId))
                .sorted(Comparator.comparing(ExportTask::createTime).reversed())
                .limit(limit)
                .toList();
    }

    private String extractFileName(String fileUrl) {
        if (fileUrl == null)
            return null;
        int lastSlash = fileUrl.lastIndexOf('/');
        return lastSlash >= 0 ? fileUrl.substring(lastSlash + 1) : fileUrl;
    }
}
