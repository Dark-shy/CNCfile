package com.example.demo.controller;

import com.example.demo.mode.amadaFile;
import com.example.demo.service.FileService;
import com.example.demo.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@CrossOrigin(origins = "*")
@ResponseBody
public class FileCollect {
    @Value("${file.upload.dir}")
    private String uploadDir;
    @Value("${file.temp.dir}")
    private String backupDir;
    @Value("${file.backup.dir}")
    private String recoveryDir;
    @Autowired
    private FileService fileService;

    @GetMapping("/file/{id}")
    public List<amadaFile> findFile(@PathVariable String id) {
        return fileService.findFile(id);
    }

    @PutMapping("/file/state/{id}")
    public String updateState(@PathVariable String id) {
        return fileService.updateState(id);
    }

    @PutMapping("/file")
    public String update(@RequestBody amadaFile file1) {
        fileService.update(String.valueOf(file1.getFileId()), file1.getState());

        try {
            Path backupPath = Paths.get(backupDir);
            Path uploadPath = Paths.get(uploadDir);
            Path recoveryPath = Paths.get(recoveryDir);
            String targetBaseName = file1.getProcedureName();

            // 使用工具类创建目录
            FileUtils.createDirectories(uploadPath, recoveryPath);

            // 查找匹配文件
            Path sourceFile = FileUtils.findFileByBaseName(backupPath, targetBaseName);
            if (sourceFile == null) {
                System.out.println("未找到匹配文件: " + targetBaseName);
                return "文件上传失败,请重新上传文件";
            }
            if (file1.getState().equals("4")) {
                FileUtils.moveFileWithBackup(sourceFile, backupPath, recoveryPath);
                return "审批成功";
            }
            // 使用工具类处理文件移动
            FileUtils.moveFileWithBackup(sourceFile, uploadPath, recoveryPath);
            System.out.println("已更新文件: " + sourceFile);
            return "操作成功";

        } catch (IOException e) {
            System.err.println("文件操作失败: " + e.getMessage());
            return "服务器内部错误";
        }
    }

    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String procedureName,
            @RequestParam(required = false) String filename) {

        try {
            // 使用工具类校验文件名
            FileUtils.validateFileName(procedureName);

            Path targetDir = Paths.get(uploadDir);
            Path filePath = FileUtils.findFileByBaseName(targetDir, procedureName);

            if (filePath != null && Files.exists(filePath)) {
                // 使用工具类构建资源
                Resource resource = FileUtils.createResource(filePath);

                // 动态设置下载文件名
                String downloadName = (filename != null && !filename.isEmpty()) ?
                        filename : filePath.getFileName().toString();
                // 强制为 .txt 文件设置二进制流类型（避免浏览器预览）
                MediaType mediaType = FileUtils.getMediaType(filePath);
                if (downloadName.toLowerCase().endsWith(".txt")) {
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }

                // 使用 RFC 5987 标准编码文件名（解决后缀丢失问题）
                ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                        .filename(downloadName, StandardCharsets.UTF_8) // 自动处理编码
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                        .contentType(mediaType)
                        .body(resource);

//                return ResponseEntity.ok()
//                        .header(HttpHeaders.CONTENT_DISPOSITION,
//                                "attachment; filename=\"" + downloadName + "\"")
//                        .contentType(FileUtils.getMediaType(filePath))
//                        .body(resource);
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ByteArrayResource(e.getMessage().getBytes()));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<String> addFile(
            @RequestParam("addUser") String addUser,
            @RequestParam("department") String department,
            @RequestParam("engineer") String engineer,
            @RequestParam("categoryId") String categoryId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("specification") String specification,
            @RequestParam("type") String type,
            @RequestParam("state") String state,
            @RequestPart("file") MultipartFile file) {

        try {
            // 1. 封装文件元数据
            amadaFile amadaFile = new amadaFile();
            amadaFile.setAddUser(addUser);
            amadaFile.setEngineer(engineer);
            amadaFile.setCategoryId(categoryId);
            amadaFile.setCategoryName(categoryName);
            amadaFile.setSpecification(specification);
            amadaFile.setType(type);
            amadaFile.setState(state);

            // 2. 生成唯一文件名并设置
            String procedureName = fileService.addFile(amadaFile);
            amadaFile.setProcedureName(procedureName);

            // 3. 验证上传文件
            if (file.isEmpty()) {
                throw new IllegalArgumentException("上传文件内容不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new IllegalArgumentException("无效的文件名");
            }

            // 4. 准备文件存储路径
            Path uploadDir = Paths.get(backupDir);
            Path recovery_Dir = Paths.get(recoveryDir);

            // 使用工具类创建目录
            FileUtils.createDirectories(uploadDir, recovery_Dir);

            // 5. 构造存储文件名
            String fileExtension = FileUtils.getFileExtension(originalFilename);
            String storedFilename = procedureName + fileExtension;
            Path targetPath = uploadDir.resolve(storedFilename);

            // 6. 处理同名文件
            if (Files.exists(targetPath)) {
                String backupName = FileUtils.generateRecoveryName(storedFilename);
                Path backupPath = recovery_Dir.resolve(backupName);
                Files.move(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 7. 保存文件
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }


            return ResponseEntity.ok().body("文件上传成功");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("文件存储失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("系统错误: " + e.getMessage());
        }
    }

}
