package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    // 项目文件存储目录名
    private static final String PROJECT_FILES_DIR = "uploads/project-files";
    
    @Autowired
    private UploadService uploadService;
    
    /**
     * 上传项目申请书PDF文件
     * @param file 上传的文件
     * @return 文件存储路径
     */
    @PostMapping("/project-file")
    public ResponseEntity<Result<String>> uploadProjectFile(@RequestParam("file") MultipartFile file) {
        logger.info("收到文件上传请求，文件名: {}, 文件大小: {}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest().body(Result.fail("只能上传PDF文件"));
            }
            
            // 验证文件大小，限制为10MB
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(Result.fail("文件大小不能超过10MB"));
            }
            
            // 调用上传服务保存文件
            String filePath = uploadService.saveProjectFile(file);
            
            // 判断文件是否是已存在的文件
            if (filePath.contains("已存在")) {
                logger.info("文件已存在，直接返回路径: {}", filePath.split("\\|")[1]);
                return ResponseEntity.ok(Result.success("文件已存在，无需重新上传", filePath.split("\\|")[1]));
            }
            
            logger.info("文件上传成功，存储路径: {}", filePath);
            return ResponseEntity.ok(Result.success(filePath));
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.badRequest().body(Result.fail("文件上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 下载项目申请书文件
     * @param filePath 文件相对路径
     * @param fileName 自定义下载文件名（可选）
     * @return 文件资源
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("filePath") String filePath,
                                         @RequestParam(value = "fileName", required = false) String fileName) {
        try {
            logger.info("收到文件下载请求，文件路径: {}", filePath);
            
            // 安全检查：验证文件路径不包含敏感字符
            if (filePath.contains("..") || filePath.contains("\\")) {
                logger.error("检测到不安全的文件路径: {}", filePath);
                return ResponseEntity.badRequest().body(Result.fail("非法的文件路径"));
            }
            
            // 使用当前工作目录作为基础路径
            String currentDir = System.getProperty("user.dir");
            String projectFilesPath = currentDir + File.separator;
            
            // 构建完整的文件路径
            Path path = Paths.get(projectFilesPath + filePath);
            logger.info("文件完整路径: {}", path.toAbsolutePath());
            
            // 创建文件资源
            Resource resource;
            try {
                resource = new UrlResource(path.toUri());
            } catch (Exception e) {
                logger.error("无法创建文件资源: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Result.fail("文件资源创建失败"));
            }
            
            // 检查文件是否存在且可读
            if (!resource.exists()) {
                logger.error("文件不存在: {}", path);
                return ResponseEntity.status(404).body(Result.fail("文件不存在"));
            }
            
            if (!resource.isReadable()) {
                logger.error("文件无法读取: {}", path);
                return ResponseEntity.status(403).body(Result.fail("文件无法读取"));
            }
            
            // 确定文件名
            String downloadFileName = fileName;
            if (downloadFileName == null || downloadFileName.isEmpty()) {
                // 如果未提供文件名，从路径中提取原始文件名
                File file = path.toFile();
                downloadFileName = file.getName();
                
                // 如果是使用UUID生成的文件名，添加项目申请书前缀
                if (downloadFileName.contains("-") && !downloadFileName.toLowerCase().contains("申请书")) {
                    downloadFileName = "项目申请书_" + downloadFileName;
                }
            }
            
            
            // URL编码文件名，解决中文文件名问题
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
                
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            logger.info("文件下载成功: {}, 下载文件名: {}", filePath, downloadFileName);
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (Exception e) {
            logger.error("文件下载失败: ", e);
            return ResponseEntity.status(500).body(Result.fail("文件下载失败: " + e.getMessage()));
        }
    }
} 