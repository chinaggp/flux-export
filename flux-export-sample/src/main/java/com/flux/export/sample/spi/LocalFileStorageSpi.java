package com.flux.export.sample.spi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.flux.export.spi.FileStorageSpi;

/**
 * FileStorageSPI 本地文件实现
 * <p>
 * 将文件存储到本地磁盘,适用于开发测试环境
 */
@Component
public class LocalFileStorageSpi implements FileStorageSpi {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageSpi.class);

    @Value("${flux-export.storage.local.path:./exports}")
    private String storagePath;

    @Value("${flux-export.storage.local.url-prefix:http://localhost:8080/files/}")
    private String urlPrefix;

    @Override
    public String upload(String fileName, byte[] fileContent) {
        try {
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path filePath = dir.resolve(fileName);
            Files.write(filePath, fileContent);

            String fileUrl = urlPrefix + fileName;
            log.info("文件已保存: path={}, url={}", filePath.toAbsolutePath(), fileUrl);

            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + fileName, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String fileName = fileUrl.replace(urlPrefix, "");
            Path filePath = Paths.get(storagePath, fileName);
            Files.deleteIfExists(filePath);
            log.info("文件已删除: {}", filePath);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", fileUrl, e);
        }
    }
}
