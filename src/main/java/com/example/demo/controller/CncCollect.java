package com.example.demo.controller;

import com.example.demo.dto.CncFileDownloadDto;
import com.example.demo.mode.CncDownloadFile;
import com.example.demo.mode.CncFileDetail;
import com.example.demo.mode.CncUploadFile;
import com.example.demo.mode.UserProfile;
import com.example.demo.service.CncDownloadService;
import com.example.demo.service.CncFileDetailService;
import com.example.demo.service.CncUploadFileService;
import com.example.demo.service.UserService;
import com.example.demo.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@CrossOrigin(origins = "*")
@ResponseBody
public class CncCollect {
    private final CncUploadFileService uploadFileService;
    private final CncFileDetailService fileDetailService;
    private final CncDownloadService cncDownloadService;
    private final UserService userService;
    @Value("${file.upload.dir}")
    private String uploadDir;
    @Value("${file.temp.dir}")
    private String backupDir;
    @Value("${file.backup.dir}")
    private String recoveryDir;


    @Autowired
    public CncCollect(CncUploadFileService service, CncFileDetailService fileDetailService, UserService userService, CncDownloadService cncDownloadService) {
        this.uploadFileService = service;
        this.fileDetailService = fileDetailService;
        this.userService = userService;
        this.cncDownloadService = cncDownloadService;
    }

    @PostMapping(value = "/cnc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String submitApplication(@RequestPart("main") CncUploadFile application, @RequestPart("details") List<CncFileDetail> programDetails, @RequestPart("ardFiles") List<MultipartFile> ardFiles, @RequestPart(value = "cadFiles", required = false) List<MultipartFile> cadFiles) throws IOException {
        if (application == null || programDetails == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        application.setDetails(programDetails);
        if (cadFiles != null) {
            String fileName = application.getCadFileName();
            FileUtils.validateFileName(fileName);
            Path zipPath = Paths.get(uploadDir, fileName + ".zip");
            Files.createDirectories(zipPath.getParent());
            try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipPath)))) {

                for (MultipartFile file : cadFiles) {
                    String entryName = file.getOriginalFilename();
                    if (entryName == null || entryName.contains("..") || entryName.contains("/")) {
                        throw new SecurityException("非法文件名: " + entryName);
                    }
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    try (InputStream is = file.getInputStream()) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                }
            } catch (IOException e) {
                Files.deleteIfExists(zipPath);
                throw new RuntimeException("ZIP文件生成失败: " + e.getMessage(), e);
            }
        } else {
            application.setCadFileName(null);
        }
        if (ardFiles != null && !ardFiles.isEmpty()) {
            Path ardDir = Paths.get(uploadDir);
            Files.createDirectories(ardDir);

            for (MultipartFile file : ardFiles) {
                String originalName = file.getOriginalFilename();
                if (originalName == null || originalName.contains("..")) {
                    throw new SecurityException("非法文件名: " + originalName);
                }
                Path targetPath = ardDir.resolve(originalName);

                try (InputStream is = file.getInputStream(); OutputStream os = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    Files.deleteIfExists(targetPath);
                    throw new RuntimeException("ARD文件保存失败: " + originalName, e);
                }
            }
        }
        try {
            uploadFileService.createFileRecord(application);
//            for (CncFileDetail fileDetail : programDetails) {
//                fileDetail.setCncUploadId(application.getId());
//                fileDetail.setState("0");
//                fileDetailService.createDetailRecord(fileDetail);
//            }
        } catch (Exception e) {
            throw new RuntimeException("存储数据库失败" + e.getMessage(), e);
        }

        return "提交成功";
    }

    @GetMapping("/cnc/{id}")
    public List<CncUploadFile> getCnc(@PathVariable String id) {
        List<CncUploadFile> uploadFile = uploadFileService.getFileByProductCode(id);
        for (CncUploadFile file : uploadFile) {
            file.setDetails(fileDetailService.getDetailsByProduct(file.getId()));
        }
        return uploadFile;
    }

    @PutMapping("/cnc/{id}")
    public void putState(@RequestBody CncFileDownloadDto dto, @PathVariable int id) {
        CncFileDetail fileDetail = new CncFileDetail();
        UserProfile userProfile = userService.findUser(dto.getWorkCode());
        fileDetail.setId(id);
        fileDetail.setSuspendTime(dto.getTime());
        fileDetail.setSuspender(userProfile.getUserName());
        fileDetail.setSuspendReason(dto.getReason());
        fileDetailService.updateDetailRecord(fileDetail);
    }

    @PutMapping("/cnc/user")
    public void addDownloadUser(@RequestBody CncFileDownloadDto dto) {
        CncDownloadFile downloadFile = new CncDownloadFile();
        UserProfile userProfile = userService.findUser(dto.getWorkCode());
        downloadFile.setDownloader(userProfile.getUserName());
        downloadFile.setDownloadTime(dto.getTime());
        downloadFile.setCncDetailId(Integer.valueOf(dto.getReason()));
        cncDownloadService.recordDownload(downloadFile);
    }
}
