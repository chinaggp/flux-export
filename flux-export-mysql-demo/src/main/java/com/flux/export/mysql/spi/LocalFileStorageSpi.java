package com.flux.export.mysql.spi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.flux.export.spi.FileStorageSpi;

import lombok.extern.slf4j.Slf4j;

/**
 * 本地文件存储 SPI 实现
 * <p>
 * 这是一个 Demo 实现, 将文件保存到本地磁盘的 exports 目录
 * <p>
 * <b>生产环境建议</b>: 使用云存储服务, 如阿里云 OSS、AWS S3、腾讯云 COS 等
 *
 * @author FluxExport Demo
 */
@Slf4j
@Component
public class LocalFileStorageSpi implements FileStorageSpi {

    /**
     * 文件存储根目录
     */
    @Value("${flux-export.storage.local.path:./exports}")
    private String storagePath;

    /**
     * 文件访问的基准 URL
     */
    @Value("${flux-export.storage.local.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Override
    public String upload(String fileName, byte[] fileContent) {
        try {
            // 确保存储目录存在
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                log.info("Created storage directory: {}", storageDir.toAbsolutePath());
            }

            // 生成唯一文件名, 添加时间戳避免重复
            String uniqueFileName = generateUniqueFileName(fileName);
            Path filePath = storageDir.resolve(uniqueFileName);

            // 写入文件
            Files.write(filePath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 生成访问 URL
            String fileUrl = baseUrl + "/" + uniqueFileName;

            log.info("File uploaded successfully: {} -> {}", fileName, fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // 从 URL 提取文件名
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                log.warn("Cannot extract file name from URL: {}", fileUrl);
                return;
            }

            // 删除文件
            Path filePath = Paths.get(storagePath).resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            // 删除失败不抛异常, 记录日志
        }
    }

    /**
     * 生成唯一文件名, 添加时间戳
     */
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 如果文件名包含扩展名, 在扩展名前插入时间戳
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String nameWithoutExt = originalFileName.substring(0, dotIndex);
            String extension = originalFileName.substring(dotIndex);
            return nameWithoutExt + "_" + timestamp + extension;
        } else {
            return originalFileName + "_" + timestamp;
        }
    }

    /**
     * 从 URL 提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // 提取最后一个 / 后面的部分
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }

        return null;
    }
}
