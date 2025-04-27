package com.example.demo.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtils {

    /**
     * 移动文件并处理已有文件冲突
     *
     * @param sourceFile  源文件路径
     * @param targetDir   目标目录
     * @param recoveryDir 备份目录
     */
    public static void moveFileWithBackup(Path sourceFile, Path targetDir, Path recoveryDir) throws IOException {
        Path targetFile = targetDir.resolve(sourceFile.getFileName());

        if (Files.exists(targetFile)) {
            String recoveryName = generateRecoveryName(targetFile.getFileName().toString());
            Path recoveryPath = recoveryDir.resolve(recoveryName);
            Files.move(targetFile, recoveryPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 生成带UUID的恢复文件名
     */
    public static String generateRecoveryName(String originalName) {
        String extension = getFileExtension(originalName);
        String baseName = originalName.replace(extension, "");
        return baseName + "_" + UUID.randomUUID() + extension;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }

    /**
     * 创建多个目录
     */
    public static void createDirectories(Path... directories) throws IOException {
        for (Path dir : directories) {
            Files.createDirectories(dir);
        }
    }

    /**
     * 根据基本名称查找文件
     *
     * @return 第一个匹配文件的路径，找不到返回null
     */
    public static Path findFileByBaseName(Path searchDir, String baseName) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchDir, baseName + ".*")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * 安全校验文件名
     */
    public static void validateFileName(String fileName) {
        if (fileName.contains("..") || fileName.contains("/")) {
            throw new SecurityException("非法文件路径: " + fileName);
        }
    }

    /**
     * 创建下载资源对象
     */
    public static Resource createResource(Path filePath) throws MalformedURLException {
        return new UrlResource(filePath.toUri());
    }

    /**
     * 获取文件媒体类型
     */
    public static MediaType getMediaType(Path filePath) throws IOException {
        String contentType = Files.probeContentType(filePath);
        return MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream");
    }
}
