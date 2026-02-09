package com.flux.export.spi;

/**
 * 文件存储 SPI 接口
 * <p>
 * 接入方需实现此接口，将生成的 Excel 文件上传到指定存储（如 OSS、S3、本地磁盘等）
 */
public interface FileStorageSpi {

    /**
     * 上传文件
     *
     * @param fileName    文件名
     * @param fileContent 文件内容（字节数组）
     * @return 文件下载 URL
     */
    String upload(String fileName, byte[] fileContent);

    /**
     * 删除文件（可选实现）
     *
     * @param fileUrl 文件 URL
     */
    default void delete(String fileUrl) {
        // 默认空实现
    }
}