package com.flux.export.mysql.converter;

import com.flux.export.model.TaskStatus;
import com.flux.export.mysql.pojo.entity.ExportTaskEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * ExportTaskEntity 类型转换工具类
 * 用于在 Core 模块的 ExportTask (Record) 和 Demo 模块的 ExportTaskEntity (Lombok 类) 之间进行转换
 * 
 * @author FluxExport Demo
 */
@Slf4j
public class ExportTaskConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将 Core 的 ExportTask (Record) 转换为 Demo 的 ExportTaskEntity (Lombok 类)
     * 
     * @param coreTask Core 模块的 ExportTask
     * @return Demo 模块的 ExportTaskEntity
     */
    public static ExportTaskEntity toDemoTask(
            com.flux.export.model.ExportTask coreTask) {
        if (coreTask == null) {
            return null;
        }

        return ExportTaskEntity.builder()
                .taskId(coreTask.taskId())
                .bizType(coreTask.bizType())
                .userId(coreTask.userId())
                .status(coreTask.status() != null ? coreTask.status().name() : null)
                .progress(coreTask.progress())
                .fileUrl(coreTask.fileUrl())
                .fileName(coreTask.fileName())
                .errorMsg(coreTask.errorMsg())
                .params(serializeParams(coreTask.params()))
                .createTime(coreTask.createTime())
                .finishTime(coreTask.finishTime())
                .build();
    }

    /**
     * 将 Demo 的 ExportTaskEntity (Lombok 类) 转换为 Core 的 ExportTask (Record)
     * 
     * @param demoTask Demo 模块的 ExportTaskEntity
     * @return Core 模块的 ExportTask
     */
    public static com.flux.export.model.ExportTask toCoreTask(
            ExportTaskEntity demoTask) {
        if (demoTask == null) {
            return null;
        }

        return new com.flux.export.model.ExportTask(
                demoTask.getTaskId(),
                demoTask.getBizType(),
                demoTask.getUserId(),
                parseStatus(demoTask.getStatus()),
                demoTask.getProgress() != null ? demoTask.getProgress() : 0,
                demoTask.getFileUrl(),
                demoTask.getFileName(),
                demoTask.getErrorMsg(),
                deserializeParams(demoTask.getParams()),
                demoTask.getCreateTime(),
                demoTask.getFinishTime());
    }

    /**
     * 灏嗗瓧绗︿覆鐘舵€佽浆鎹负 TaskStatus 鏋氫妇
     */
    private static TaskStatus parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid task status: {}, defaulting to PENDING", status);
            return TaskStatus.PENDING;
        }
    }

    /**
     * 将参数对象序列化为 JSON 字符串*/
    private static String serializeParams(Object params) {
        if (params == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize params: {}", params, e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     * <p>
     * 注意: 这里返回的是通用的 Object 类型, 实际使用时可能需要根据业务类型进行类型转换 */
    private static Object deserializeParams(String paramsJson) {
        if (paramsJson == null || paramsJson.trim().isEmpty()) {
            return null;
        }
        try {
            // 返回 Map 类型, 保持通用性
             return OBJECT_MAPPER.readValue(paramsJson, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize params: {}", paramsJson, e);
            return null;
        }
    }
}
