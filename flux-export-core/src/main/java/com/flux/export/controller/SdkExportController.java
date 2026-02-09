package com.flux.export.controller;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.export.config.ExportConfig;
import com.flux.export.core.ExportOrchestrator;
import com.flux.export.dto.ExportRequest;
import com.flux.export.dto.ExportResponse;
import com.flux.export.model.ExportTask;
import com.flux.export.spi.PermissionCheckerSpi;
import com.flux.export.spi.ResponseWrapperSpi;
import com.flux.export.spi.TaskPersistenceSpi;
import com.flux.export.spi.UserContextSpi;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

/**
 * SDK 导出 Controller
 * <p>
 * 提供导出任务的创建、查询接口
 */
@RestController
@RequestMapping("/public/export")
@RequiredArgsConstructor
public class SdkExportController {

    private final ExportOrchestrator orchestrator;
    private final UserContextSpi userContextSpi;
    private final PermissionCheckerSpi defaultPermissionChecker;
    private final TaskPersistenceSpi taskPersistenceSpi;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final ResponseWrapperSpi responseWrapper;

    /**
     * 创建导出任务
     * <p>
     * POST /public/export/create
     */
    @PostMapping("/create")
    public Object create(@Valid @RequestBody ExportRequest request) {
        // 1. 获取当前用户
        String userId = userContextSpi.getCurrentUserId();

        // 2. 获取业务配置
        ExportConfig config = orchestrator.getConfig(request.bizType());
        if (config == null) {
            return responseWrapper.wrapError("未找到业务类型配置: " + request.bizType(), 400);
        }

        // 3. 检查权限(支持自定义权限校验器)
        boolean hasPermission = checkPermission(userId, request.bizType(), config);
        if (!hasPermission) {
            return responseWrapper.wrapError("无权限执行此导出", 403);
        }

        // 4. 参数类型转换
        Class<?> paramType = config.paramType();
        Object typedParams;
        try {
            typedParams = objectMapper.convertValue(request.params(), paramType);
        } catch (Exception e) {
            return responseWrapper.wrapError("参数类型转换失败: " + e.getMessage(), 400);
        }

        // 5. 参数校验(JSR-303)
        Set<ConstraintViolation<Object>> violations = validator.validate(typedParams);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream().map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
            return responseWrapper.wrapError(errorMsg, 400);
        }

        // 6. 提交任务(传入已校验的参数)
        String taskId = orchestrator.submitTask(request.bizType(), userId, typedParams);

        return responseWrapper.wrapSuccess(ExportResponse.submitted(taskId));
    }

    /**
     * 检查权限
     * <p>
     * 如果业务配置了自定义权限校验器, 则使用自定义校验器; 否则使用默认校验器
     */
    private boolean checkPermission(String userId, String bizType, ExportConfig config) {
        if (config.hasCustomPermissionChecker()) {
            try {
                // 使用自定义权限校验器
                PermissionCheckerSpi customChecker = orchestrator
                        .getBean(config.permissionCheckerBeanName(), PermissionCheckerSpi.class);
                return customChecker.hasPermission(userId, bizType);
            } catch (Exception e) {
                // 如果自定义校验器不存在或出错, 记录日志并拒绝访问
                return false;
            }
        } else {
            // 使用默认权限校验器
            return defaultPermissionChecker.hasPermission(userId, bizType);
        }
    }

    /**
     * 查询任务状态
     * <p>
     * GET /public/export/status?taskId=xxx
     */
    @GetMapping("/status")
    public Object status(@RequestParam String taskId) {
        return taskPersistenceSpi.findById(taskId)
                .map(task -> responseWrapper.wrapSuccess(ExportResponse.fromTask(task)))
                .orElse(responseWrapper.wrapNotFound());
    }

    /**
     * 查询用户的任务列表
     * <p>
     * GET /public/export/list?limit=10
     */
    @GetMapping("/list")
    public Object list(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        String userId = userContextSpi.getCurrentUserId();
        List<ExportTask> tasks = taskPersistenceSpi.findByUserId(userId, limit);
        List<ExportResponse> responses = tasks.stream().map(ExportResponse::fromTask).toList();
        return responseWrapper.wrapSuccess(responses);
    }
}