package com.flux.export.config;

/**
 * 导出配置项
 * <p>
 * 用于注册业务导出配置，将业务 Service 方法映射为导出能力
 *
 * @param bizType                   业务类型标识（唯一）
 * @param beanName                  Spring Bean 名称
 * @param methodName                要调用的方法名
 * @param paramType                 参数类型
 * @param permissionCheckerBeanName 权限校验器 Bean 名称（可选，为 null 时使用默认校验器）
 */
public record ExportConfig(
                String bizType,
                String beanName,
                String methodName,
                Class<?> paramType,
                String permissionCheckerBeanName) {
        /**
         * 创建不带自定义权限校验器的配置
         */
        public ExportConfig(String bizType, String beanName, String methodName, Class<?> paramType) {
                this(bizType, beanName, methodName, paramType, null);
        }

        /**
         * 是否配置了自定义权限校验器
         */
        public boolean hasCustomPermissionChecker() {
                return permissionCheckerBeanName != null && !permissionCheckerBeanName.isBlank();
        }
}
