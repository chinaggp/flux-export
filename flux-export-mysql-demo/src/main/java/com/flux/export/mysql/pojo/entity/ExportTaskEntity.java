package com.flux.export.mysql.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出任务实体类
 * 
 * @author FluxExport Demo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_export_task")  // 表名
public class ExportTaskEntity {
    
    /**
     * 任务唯一ID (主键)
     */
    @TableId("task_id")
    private String taskId;
    
    /**
     * 业务类型标识
     */
    @TableField("biz_type")
    private String bizType;
    
    /**
     * 触发用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 任务状态, PENDING/PROCESSING/SUCCESS/FAILED
     */
    @TableField("status")
    private String status;
    
    /**
     * 进度 0-100
     */
    @TableField("progress")
    private Integer progress;
    
    /**
     * 文件下载地址
     */
    @TableField("file_url")
    private String fileUrl;
    
    /**
     * 文件名 */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;
    
    /**
     * 请求参数JSON快照
     */
    @TableField("params")
    private String params;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;
    
    /**
     * 创建一个新的待处理任务
     */
    public static ExportTaskEntity createPending(String taskId, String bizType, String userId, String params) {
        return ExportTaskEntity.builder()
                .taskId(taskId)
                .bizType(bizType)
                .userId(userId)
                .status("PENDING")
                .progress(0)
                .params(params)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 判断任务是否已完成(成功或失败)
     */
    public boolean isFinished() {
        return "SUCCESS".equals(status) || "FAILED".equals(status);
    }
    
    /**
     * 判断任务是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    /**
     * 判断任务是否处理中 */
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
}
