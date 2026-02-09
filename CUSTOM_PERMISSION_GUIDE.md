# FluxExport Custom Permission Guide

This guide explains how to implement custom permission logic for different export business types.

## 1. Implement PermissionCheckerSpi

Create a Spring Bean that implements the `PermissionCheckerSpi` interface.

```java
@Component("orderPermissionChecker")
public class OrderPermissionChecker implements PermissionCheckerSpi {
    @Override
    public boolean hasPermission(String userId, String bizType) {
        // Implement your logic here
        return true; 
    }
}
```

## 2. Register Business Configuration

Specify the bean name of your custom checker when registering the `ExportConfig`.

```java
ExportConfigRegistry.register(new ExportConfig(
    "ORDER_EXPORT",           // bizType
    "orderService",           // beanName
    "queryOrders",            // methodName
    OrderParams.class,        // paramType
    "orderPermissionChecker"  // custom permission checker bean name
));
```

## 3. How it Works

When a request is made to `/public/export/create`:
1. SDK finds the `ExportConfig` for the given `bizType`.
2. If `permissionCheckerBeanName` is specified, it uses that bean.
3. Otherwise, it uses the default `PermissionCheckerSpi` implementation.
