# FluxExport

> 🚀 轻量级、插件化的 Java 异步导出 SDK —— 一次接入，永久复用

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![EasyExcel](https://img.shields.io/badge/EasyExcel-3.3.3-blue)](https://easyexcel.opensource.alibaba.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

## ✨ 核心特性

| 特性 | 描述 |
|:---|:---|
| **零业务侵入** | SDK 不包含任何业务逻辑，仅负责任务调度与生命周期管理 |
| **配置化驱动** | 通过简单配置注册，将现有 Service 方法转化为导出能力 |
| **SPI 扩展** | 存储、持久化、权限、用户上下文等均可自定义实现 |
| **进度感知** | 实时任务进度查询，内置防抖机制避免数据库压力 |
| **稳健低运维** | 纯数据库存储，无需 Redis，适合后台管理系统 |

## 📦 模块结构

```
flux-export/
├── flux-export-core/          # 核心 SDK (包名: com.flux.export)
│   ├── controller/            # 导出 API 控制器
│   ├── core/                  # 调度器 ExportOrchestrator
│   ├── config/                # 配置注册中心
│   ├── model/                 # 任务模型
│   ├── dto/                   # 请求/响应 DTO
│   └── spi/                   # SPI 扩展接口
│
├── flux-export-sample/        # 基础示例 (内存级实现)
│   └── 适合快速原型验证、开发测试
│
└── flux-export-mysql-demo/    # 企业级示例 (MySQL + MyBatis-Plus)
    └── 适合生产环境参考实现
```

## 🔌 SPI 扩展点

| SPI 接口 | 职责 | 默认实现 |
|:---|:---|:---|
| `TaskPersistenceSpi` | 任务状态持久化与查询 | 需业务方实现 |
| `FileStorageSpi` | 文件上传与下载链接获取 | 需业务方实现 |
| `UserContextSpi` | 获取当前用户身份标识 | 需业务方实现 |
| `PermissionCheckerSpi` | 导出操作鉴权 | 默认放行 |
| `ResponseWrapperSpi` | 统一 API 响应格式适配 | 默认包装 |

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.flux</groupId>
    <artifactId>flux-export-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 实现 SPI 接口

```java
@Component
public class MyTaskPersistenceSpi implements TaskPersistenceSpi {
    @Override
    public void createTask(ExportTask task) { /* 存储任务 */ }
    
    @Override
    public void updateProgress(String taskId, int progress) { /* 更新进度 */ }
    
    @Override
    public void markSuccess(String taskId, String fileUrl) { /* 标记成功 */ }
    
    @Override
    public Optional<ExportTask> findById(String taskId) { /* 查询任务 */ }
}
```

### 3. 注册导出业务

```java
@Component
public class ExportBusinessConfig {

    @PostConstruct
    public void register() {
        ExportConfigRegistry.register(new ExportConfig(
            "ORDER_EXPORT",      // 业务类型
            "orderService",      // Spring Bean 名称
            "queryOrders",       // 数据查询方法
            OrderParams.class    // 参数类型 (支持 JSR-303 校验)
        ));
    }
}
```

### 4. 编写业务 Service

```java
@Service
public class OrderService {

    public List<OrderVO> queryOrders(OrderParams params) {
        // 返回数据列表，SDK 自动生成 Excel
        return orderRepository.findByCondition(params);
    }
}
```

### 5. 调用导出 API

```bash
# 创建导出任务
curl -X POST http://localhost:8080/public/export/create \
  -H "Content-Type: application/json" \
  -d '{"bizType": "ORDER_EXPORT", "params": {"status": "COMPLETED"}}'

# 查询任务状态
curl "http://localhost:8080/public/export/status?taskId=xxx"

# 查询任务列表
curl "http://localhost:8080/public/export/list?limit=10"
```

## 🔄 核心交互流程

```
┌─────────┐    POST /create     ┌─────────────────┐
│  前端   │ ─────────────────▶ │ SdkExportController
└─────────┘                     └────────┬────────┘
                                         │
                        ┌────────────────▼────────────────┐
                        │    1. 获取用户 (UserContextSpi) │
                        │    2. 权限校验 (PermissionChecker)
                        │    3. 参数转换 & JSR-303 校验   │
                        └────────────────┬────────────────┘
                                         │
           ┌─────────────────────────────▼─────────────────────────────┐
           │                  ExportOrchestrator                       │
           │  ┌──────────────────────────────────────────────────────┐ │
           │  │ 4. 创建任务 (TaskPersistenceSpi.createTask)          │ │
           │  │ 5. 异步执行: 反射调用 bizService.queryXxx()          │ │
           │  │ 6. 生成 Excel (EasyExcel)                           │ │
           │  │ 7. 上传文件 (FileStorageSpi.upload)                  │ │
           │  │ 8. 更新状态 (TaskPersistenceSpi.markSuccess)         │ │
           │  └──────────────────────────────────────────────────────┘ │
           └───────────────────────────────────────────────────────────┘
                                         │
┌─────────┐    GET /status?taskId=       │
│  前端   │ ◀────────────────────────────┘
└─────────┘   返回进度/下载链接
```

## 📚 示例模块

### flux-export-sample (内存级)

- **存储方式**: ConcurrentHashMap
- **特点**: 开箱即用，无需数据库
- **适用**: 快速原型验证、开发测试

```powershell
.\mvnw.bat clean package -DskipTests -pl flux-export-sample -am
java -jar flux-export-sample/target/flux-export-sample-1.0.0-SNAPSHOT.jar
```

### flux-export-mysql-demo (MySQL)

- **存储方式**: MySQL + MyBatis-Plus
- **特点**: 任务持久化、可恢复、生产级实现参考
- **适用**: 真实业务场景

```powershell
.\mvnw.bat clean package -DskipTests -pl flux-export-mysql-demo -am
java -jar flux-export-mysql-demo/target/flux-export-mysql-demo-1.0.0-SNAPSHOT.jar
```

详细文档请参考各模块的 README:
- [flux-export-sample/README.md](flux-export-sample/README.md)
- [flux-export-mysql-demo/README.md](flux-export-mysql-demo/README.md)

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|:---|:---|:---|
| Java | 17+ | 使用 Records、Jakarta EE |
| Spring Boot | 3.2.2+ | 基础框架 |
| EasyExcel | 3.3.3 | 高性能 Excel 读写 |
| Jackson | 2.16.1 | JSON 处理 |
| Maven | 3.6+ | 构建工具 |

## 🏗️ 构建项目

```powershell
# 完整构建
.\mvnw.bat clean package -DskipTests

# 仅构建 core
.\mvnw.bat clean package -DskipTests -pl flux-export-core

# 构建并运行 sample
.\mvnw.bat clean package -DskipTests -pl flux-export-sample -am
```



---

💡 **如有问题或建议，欢迎提 Issue 或 PR！**
