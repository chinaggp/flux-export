package com.flux.export.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * FluxExport 示例应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.flux.export", "com.flux.export.sample" })
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
