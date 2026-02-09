package com.flux.export.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

/**
 * FluxExport MySQL Demo 应用启动类
 * 
 * <p>
 * 这是一个演示项目, 展示如何使用 MyBatis-Plus 实现 MySQL 数据库持久化
 * </p>
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = { "com.flux.export", "com.flux.export.mysql" })
public class MysqlDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MysqlDemoApplication.class, args);
        log.info("MySQL Demo Application started.");
    }
}
