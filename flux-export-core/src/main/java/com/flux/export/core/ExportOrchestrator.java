package com.flux.export.core;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.alibaba.excel.EasyExcel;
import com.flux.export.config.ExportConfig;
import com.flux.export.config.ExportConfigRegistry;
import com.flux.export.model.ExportTask;
import com.flux.export.spi.FileStorageSpi;
import com.flux.export.spi.TaskPersistenceSpi;

/**
 * 核心调度器 ExportOrchestrator
 * <p>
 * 负责任务调度、生命周期管理、反射调用业务方法、生成 Excel 等
 */
@Component
public class ExportOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ExportOrchestrator.class);
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ApplicationContext applicationContext;
    private final TaskPersistenceSpi taskPersistenceSpi;
    private final FileStorageSpi fileStorageSpi;
    private final ExecutorService executor;

    public ExportOrchestrator(ApplicationContext applicationContext,
            TaskPersistenceSpi taskPersistenceSpi, FileStorageSpi fileStorageSpi) {
        this.applicationContext = applicationContext;
        this.taskPersistenceSpi = taskPersistenceSpi;
        this.fileStorageSpi = fileStorageSpi;
        // 使用固定线程池执行异步任务
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 提交导出任务
     * <p>
     * 注意: 调用此方法前, Controller 层应已完成参数校验
     *
     * @param bizType         业务类型
     * @param userId          用户 ID
     * @param validatedParams 已校验的参数对象
     * @return 任务 ID
     */
    public String submitTask(String bizType, String userId, Object validatedParams) {
        // 1. 检查配置是否存在
        ExportConfig config = ExportConfigRegistry.get(bizType)
                .orElseThrow(() -> new IllegalArgumentException("未找到业务类型配置: " + bizType));

        // 2. 生成任务 ID
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 3. 创建任务记录
        ExportTask task = ExportTask.createPending(taskId, bizType, userId, validatedParams);
        taskPersistenceSpi.createTask(task);

        // 4. 异步执行(传入已校验的参数)
        executor.submit(() -> executeTask(taskId, config, validatedParams));

        return taskId;
    }

    /**
     * 获取业务配置
     *
     * @param bizType 业务类型
     * @return 配置对象，如果不存在则返回 null
     */
    public ExportConfig getConfig(String bizType) {
        return ExportConfigRegistry.get(bizType).orElse(null);
    }

    /**
     * 从 Spring 容器获取 Bean
     *
     * @param beanName Bean 名称
     * @param clazz    Bean 类型
     * @return Bean 实例
     */
    public <T> T getBean(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }

    /**
     * 异步执行导出任务
     *
     * @param taskId      任务ID
     * @param config      导出配置
     * @param typedParams 已校验的参数对象
     */
    private void executeTask(String taskId, ExportConfig config, Object typedParams) {
        try {
            // 更新状态为处理中
            taskPersistenceSpi.updateProgress(taskId, 0);

            // 1. 反射调用业务方法
            Class<?> paramType = config.paramType();
            Object bean = applicationContext.getBean(config.beanName());
            Method method = findMethod(bean.getClass(), config.methodName(), paramType);

            List<?> dataList = (List<?>) method.invoke(bean, typedParams);

            taskPersistenceSpi.updateProgress(taskId, 50);

            // 2. 生成 Excel
            byte[] excelContent = generateExcel(dataList);

            taskPersistenceSpi.updateProgress(taskId, 80);

            // 3. 上传文件
            String fileName = config.bizType() + "_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";
            String fileUrl = fileStorageSpi.upload(fileName, excelContent);

            // 4. 更新为成功状态
            taskPersistenceSpi.markSuccess(taskId, fileUrl);

            log.info("导出任务完成: taskId={}, fileUrl={}", taskId, fileUrl);

        } catch (Exception e) {
            log.error("导出任务失败: taskId={}", taskId, e);
            taskPersistenceSpi.markFailed(taskId, e.getMessage());
        }
    }

    /**
     * 查找方法
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?> paramType)
            throws NoSuchMethodException {
        try {
            return clazz.getMethod(methodName, paramType);
        } catch (NoSuchMethodException e) {
            // 尝试查找父类方法
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                    if (method.getParameterTypes()[0].isAssignableFrom(paramType)) {
                        return method;
                    }
                }
            }
            throw e;
        }
    }

    /**
     * 生成 Excel 文件
     */
    private byte[] generateExcel(List<?> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("导出数据为空");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Class<?> dataClass = dataList.get(0).getClass();

        EasyExcel.write(outputStream, dataClass).sheet("Sheet1").doWrite(dataList);

        return outputStream.toByteArray();
    }
}