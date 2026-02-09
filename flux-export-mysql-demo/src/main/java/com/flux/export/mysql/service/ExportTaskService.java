package com.flux.export.mysql.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flux.export.mysql.pojo.entity.ExportTaskEntity;
import com.flux.export.mysql.mapper.ExportTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExportTaskEntity Service 实现
 * 继承 MyBatis-Plus 的 ServiceImpl,获得常用的业务方法 */
@Slf4j
@Service
public class ExportTaskService extends ServiceImpl<ExportTaskMapper, ExportTaskEntity> {
    
    /**
     * 防抖机制: 记录每个任务上次更新进度的时间戳
     * Key: taskId, Value: 上次更新时间(毫秒)
     */
    private final Map<String, Long> lastUpdateTimeMap = new ConcurrentHashMap<>();
    
    /**
     * 防抖间隔时间(毫秒)
     */
    private static final long THROTTLE_INTERVAL_MS = 1000;
    
    /**
     * 创建任务
     */
    public boolean createTask(ExportTaskEntity task) {
        try {
            boolean success = save(task);
            log.info("Created task: taskId={}, bizType={}, success={}", 
                task.getTaskId(), task.getBizType(), success);
            return success;
        } catch (Exception e) {
            log.error("Failed to create task: taskId={}", task.getTaskId(), e);
            throw new RuntimeException("Failed to create task", e);
        }
    }
    
    /**
     * 更新任务状态 */
    public boolean updateStatus(String taskId, String status) {
        try {
            LambdaUpdateWrapper<ExportTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ExportTaskEntity::getTaskId, taskId)
                        .set(ExportTaskEntity::getStatus, status);
            
            boolean success = update(updateWrapper);
            log.debug("Updated task status: taskId={}, status={}, success={}", 
                taskId, status, success);
            return success;
        } catch (Exception e) {
            log.error("Failed to update task status: taskId={}, status={}", taskId, status, e);
            throw new RuntimeException("Failed to update task status", e);
        }
    }
    
    /**
     * 更新任务进度(带防抖机制)
     */
    public boolean updateProgress(String taskId, int progress) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdateTime = lastUpdateTimeMap.get(taskId);
        
        // 防抖逻辑
        if (lastUpdateTime != null && 
            (currentTime - lastUpdateTime) < THROTTLE_INTERVAL_MS && 
            progress < 100) {
            log.trace("Throttled progress update: taskId={}, progress={}", taskId, progress);
            return false;
        }
        
        try {
            LambdaUpdateWrapper<ExportTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ExportTaskEntity::getTaskId, taskId)
                        .set(ExportTaskEntity::getProgress, progress);
            
            boolean success = update(updateWrapper);
            lastUpdateTimeMap.put(taskId, currentTime);
            log.debug("Updated task progress: taskId={}, progress={}, success={}", 
                taskId, progress, success);
            
            // 任务完成时清理防抖缓存
             if (progress >= 100) {
                lastUpdateTimeMap.remove(taskId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("Failed to update task progress: taskId={}, progress={}", taskId, progress, e);
            throw new RuntimeException("Failed to update task progress", e);
        }
    }
    
    /**
     * 标记任务成功
     */
    public boolean markSuccess(String taskId, String fileUrl, String fileName) {
        try {
            LambdaUpdateWrapper<ExportTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ExportTaskEntity::getTaskId, taskId)
                        .set(ExportTaskEntity::getStatus, "SUCCESS")
                        .set(ExportTaskEntity::getProgress, 100)
                        .set(ExportTaskEntity::getFileUrl, fileUrl)
                        .set(ExportTaskEntity::getFileName, fileName)
                        .set(ExportTaskEntity::getFinishTime, LocalDateTime.now());
            
            boolean success = update(updateWrapper);
            lastUpdateTimeMap.remove(taskId); // 清理防抖缓存
            log.info("Marked task as success: taskId={}, fileUrl={}, success={}", 
                taskId, fileUrl, success);
            return success;
        } catch (Exception e) {
            log.error("Failed to mark task as success: taskId={}", taskId, e);
            throw new RuntimeException("Failed to mark task as success", e);
        }
    }
    
    /**
     * 标记任务失败
     */
    public boolean markFailed(String taskId, String errorMsg) {
        try {
            LambdaUpdateWrapper<ExportTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ExportTaskEntity::getTaskId, taskId)
                        .set(ExportTaskEntity::getStatus, "FAILED")
                        .set(ExportTaskEntity::getErrorMsg, errorMsg)
                        .set(ExportTaskEntity::getFinishTime, LocalDateTime.now());
            
            boolean success = update(updateWrapper);
            lastUpdateTimeMap.remove(taskId); // 清理防抖缓存
            log.info("Marked task as failed: taskId={}, errorMsg={}, success={}", 
                taskId, errorMsg, success);
            return success;
        } catch (Exception e) {
            log.error("Failed to mark task as failed: taskId={}", taskId, e);
            throw new RuntimeException("Failed to mark task as failed", e);
        }
    }
    
    /**
     * 根据任务ID查询
     */
    public Optional<ExportTaskEntity> findById(String taskId) {
        try {
            ExportTaskEntity task = getById(taskId);
            log.debug("Found task: taskId={}, exists={}", taskId, task != null);
            return Optional.ofNullable(task);
        } catch (Exception e) {
            log.error("Failed to find task: taskId={}", taskId, e);
            throw new RuntimeException("Failed to find task", e);
        }
    }
    
    /**
     * 查询用户的任务列表 */
    public List<ExportTaskEntity> findByUserId(String userId, int limit) {
        try {
            LambdaQueryWrapper<ExportTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExportTaskEntity::getUserId, userId)
                       .orderByDesc(ExportTaskEntity::getCreateTime)
                       .last("LIMIT " + limit);
            
            List<ExportTaskEntity> tasks = list(queryWrapper);
            log.debug("Found {} tasks for user: userId={}", tasks.size(), userId);
            return tasks;
        } catch (Exception e) {
            log.error("Failed to find tasks for user: userId={}", userId, e);
            throw new RuntimeException("Failed to find tasks for user", e);
        }
    }
}
