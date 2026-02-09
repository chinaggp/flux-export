# FluxExport MySQL Demo

> 🎯 这是一个企业级示例，演示如何整合 FluxExport SDK 与 MySQL 数据库，实现真正的任务持久化。

## 📑 目录

- [概述](#概述)
- [项目结构](#项目结构)
- [SPI 实现说明](#spi-实现说明)
- [数据库设计](#数据库设计)
- [快速开始](#快速开始)
- [API 接口](#api-接口)
- [配置参数](#配置参数)
- [生产环境建议](#生产环境建议)

## 概述

本模块展示了如何将 `flux-export-core` SDK 集成到实际业务项目中，核心特点包括：

| 特性 | 描述 |
|:---|:---|
| **MyBatis-Plus 持久化** | 任务状态存储到 MySQL，支持任务查询与恢复 |
| **防抖机制** | 避免高频进度更新导致数据库压力过大 |
| **本地文件存储** | Demo 级别的文件存储，可替换为 OSS/S3 |
| **自定义权限校验** | 演示如何实现业务级别的导出权限控制 |

### 与 Sample 模块的区别

| 模块 | 存储方式 | 持久化 | 适用场景 |
|:---|:---|:---|:---|
| `flux-export-sample` | 内存 (ConcurrentHashMap) | ❌ 重启丢失 | 快速原型验证 |
| `flux-export-mysql-demo` | MySQL + MyBatis-Plus | ✅ 持久化 | 生产级实现参考 |

## 项目结构

```
src/main/java/com/flux/export/mysql/
├── MysqlDemoApplication.java      # Spring Boot 启动类
├── config/
│   ├── ExportBusinessConfig.java  # 导出业务注册配置 ⭐
│   └── MyBatisPlusConfig.java     # MyBatis-Plus 配置
├── converter/
│   └── ExportTaskConverter.java   # Core ↔ Demo 实体转换器
├── demo/
│   └── OrderService.java          # 业务服务示例 + 权限校验
├── mapper/
│   ├── ExportTaskMapper.java      # 任务 Mapper
│   └── OrderMapper.java           # 订单 Mapper
├── pojo/
│   ├── entity/                    # 数据库实体
│   │   ├── ExportTaskEntity.java  # 导出任务表实体
│   │   └── OrderEntity.java       # 订单表实体
│   ├── params/
│   │   └── OrderParams.java       # 导出查询参数
│   └── vo/
│       └── OrderVO.java           # EasyExcel 导出 VO
├── service/
│   └── ExportTaskService.java     # 任务服务层 (含防抖逻辑)
└── spi/                           # SPI 实现 ⭐
    ├── MySqlTaskPersistenceSpi.java   # 任务持久化
    ├── LocalFileStorageSpi.java       # 本地文件存储
    ├── DefaultUserContextSpi.java     # 用户上下文
    └── DefaultPermissionCheckerSpi.java # 默认权限校验
```

## SPI 实现说明

FluxExport SDK 通过 SPI 机制实现扩展，本 Demo 实现了以下接口：

### 1. TaskPersistenceSpi（任务持久化）

```java
@Component
public class MySqlTaskPersistenceSpi implements TaskPersistenceSpi {
    
    @Override
    public void createTask(ExportTask task) {
        // Core 实体 -> Demo 实体，存入 MySQL
    }
    
    @Override
    public void updateProgress(String taskId, int progress) {
        // Service 层已实现防抖机制
    }
    
    @Override
    public void markSuccess(String taskId, String fileUrl) {
        // 标记成功，记录文件 URL
    }
    
    @Override
    public Optional<ExportTask> findById(String taskId) {
        // Demo 实体 -> Core 实体，返回给 SDK
    }
}
```

### 2. FileStorageSpi（文件存储）

```java
@Component  
public class LocalFileStorageSpi implements FileStorageSpi {
    
    @Value("${flux-export.storage.local.path:./exports}")
    private String storagePath;

    @Override
    public String upload(String fileName, byte[] fileContent) {
        // 写入本地磁盘，返回访问 URL
    }
}
```

> ⚠️ **生产环境建议**: 替换为阿里云 OSS、AWS S3 或腾讯云 COS 等云存储服务。

### 3. 业务配置注册

```java
@Component
public class ExportBusinessConfig {

    @PostConstruct
    public void registerExportConfigs() {
        // 注册导出业务类型
        ExportConfigRegistry.register(new ExportConfig(
            "ORDER_EXPORT",      // 业务类型标识
            "orderService",      // Spring Bean 名称
            "queryOrders",       // 数据查询方法
            OrderParams.class,   // 参数类型 (支持 JSR-303 校验)
            "orderService"       // 自定义权限校验器 Bean (可选)
        ));
    }
}
```

## 数据库设计

### 任务表 (t_export_task)

```sql
CREATE TABLE t_export_task (
    task_id      VARCHAR(64)   PRIMARY KEY COMMENT '任务ID',
    biz_type     VARCHAR(64)   NOT NULL    COMMENT '业务类型',
    user_id      VARCHAR(64)   NOT NULL    COMMENT '用户ID',
    status       VARCHAR(16)   NOT NULL    COMMENT '状态: PENDING/PROCESSING/SUCCESS/FAILED',
    progress     INT           DEFAULT 0   COMMENT '进度 0-100',
    file_url     VARCHAR(512)              COMMENT '文件下载地址',
    file_name    VARCHAR(256)              COMMENT '文件名',
    error_msg    VARCHAR(1024)             COMMENT '错误信息',
    params       TEXT                      COMMENT '请求参数JSON',
    create_time  DATETIME      NOT NULL    COMMENT '创建时间',
    finish_time  DATETIME                  COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出任务表';
```

### 订单表 (orders) - Demo 数据

```sql
CREATE TABLE orders (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64)   NOT NULL UNIQUE COMMENT '订单号',
    product_name VARCHAR(256)  NOT NULL        COMMENT '商品名称',
    amount       DECIMAL(10,2) NOT NULL        COMMENT '金额',
    status       VARCHAR(16)   NOT NULL        COMMENT '状态',
    create_time  DATETIME      NOT NULL        COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 插入测试数据
INSERT INTO orders (order_no, product_name, amount, status, create_time) VALUES
('ORD-20240101-0001', 'iPhone 15 Pro', 8999.00, 'COMPLETED', NOW()),
('ORD-20240101-0002', 'MacBook Pro 14', 14999.00, 'PENDING', NOW());
-- ... 更多测试数据
```

## 快速开始

### 1. 准备数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`flux-export\` DEFAULT CHARSET=utf8mb4"

# 执行建表 SQL (参考上方数据库设计)
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flux-export?...
    username: your_username
    password: your_password
```

### 3. 构建并运行

```powershell
# 构建项目
.\mvnw.bat clean package -DskipTests -pl flux-export-mysql-demo -am

# 运行应用
java -jar flux-export-mysql-demo/target/flux-export-mysql-demo-1.0.0-SNAPSHOT.jar
```

### 4. 测试导出

```bash
# 创建导出任务
curl -X POST http://localhost:8080/public/export/create \
  -H "Content-Type: application/json" \
  -d '{"bizType": "ORDER_EXPORT", "params": {"status": "COMPLETED"}}'

# 响应示例
# {"code": 200, "data": {"taskId": "abc123...", "status": "PENDING"}}

# 查询任务状态
curl "http://localhost:8080/public/export/status?taskId=abc123..."

# 查询任务列表
curl "http://localhost:8080/public/export/list?limit=10"
```

## API 接口

| 方法 | 路径 | 说明 |
|:---|:---|:---|
| `POST` | `/public/export/create` | 创建导出任务 |
| `GET` | `/public/export/status?taskId=xxx` | 查询任务状态 |
| `GET` | `/public/export/list?limit=10` | 查询用户任务列表 |

### 创建任务请求体

```json
{
  "bizType": "ORDER_EXPORT",
  "params": {
    "status": "COMPLETED",
    "startTime": "2024-01-01",
    "endTime": "2024-12-31"
  }
}
```

### 任务状态响应

```json
{
  "code": 200,
  "data": {
    "taskId": "abc123...",
    "bizType": "ORDER_EXPORT",
    "status": "SUCCESS",
    "progress": 100,
    "fileUrl": "http://localhost:8080/files/ORDER_EXPORT_20240101_120000.xlsx",
    "createTime": "2024-01-01T12:00:00"
  }
}
```

## 配置参数

| 配置项 | 默认值 | 说明 |
|:---|:---|:---|
| `flux-export.storage.local.path` | `./exports` | 文件存储目录 |
| `flux-export.storage.local.base-url` | `http://localhost:8080/files` | 文件访问基础 URL |

## 生产环境建议

### 1. 文件存储

将 `LocalFileStorageSpi` 替换为云存储实现，例如：

```java
@Component
@Profile("prod")
public class OssFileStorageSpi implements FileStorageSpi {
    
    @Override
    public String upload(String fileName, byte[] fileContent) {
        // 上传到阿里云 OSS / AWS S3
        return ossClient.upload(fileName, fileContent);
    }
}
```

### 2. 用户上下文

集成实际的认证框架（如 Spring Security）：

```java
@Component
public class SecurityUserContextSpi implements UserContextSpi {
    
    @Override
    public String getCurrentUserId() {
        return SecurityContextHolder.getContext()
            .getAuthentication().getName();
    }
}
```

### 3. 权限控制

实现细粒度的权限校验：

```java
@Component
public class RbacPermissionChecker implements PermissionCheckerSpi {
    
    @Override
    public boolean hasPermission(String userId, String bizType) {
        // 基于 RBAC 的权限校验
        return permissionService.hasExportPermission(userId, bizType);
    }
}
```

### 4. 监控与告警

- 接入 Prometheus + Grafana 监控导出任务指标
- 配置失败任务告警通知
- 添加分布式链路追踪 (SkyWalking / Zipkin)

---

📚 更多信息请参考 [flux-export-core README](../flux-export-core/README.md) 和 [flux-export-sample](../flux-export-sample/README.md)。
