package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件上传服务实现类
 */
@Service
public class UploadServiceImpl implements UploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);
    
    // 项目文件存储目录名
    private static final String PROJECT_FILES_DIR = "uploads/project-files";
    
    private String uploadPath;
    
    // 用于缓存文件MD5和路径的映射关系，避免频繁计算MD5，使用ConcurrentHashMap保证线程安全
    private final Map<String, String> fileHashPathMap = new ConcurrentHashMap<>();
    
    /**
     * 服务初始化时扫描已有文件
     */
    @PostConstruct
    public void init() {
        // 使用当前工作目录作为基础路径
        String currentDir = System.getProperty("user.dir");
        uploadPath = currentDir + File.separator + PROJECT_FILES_DIR;
        
        logger.info("文件上传路径设置为当前工作目录: {}", uploadPath);
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                logger.info("创建上传目录: {}", uploadPath);
            } else {
                logger.error("创建上传目录失败: {}", uploadPath);
            }
            return;
        }
        
        logger.info("开始扫描已有文件...");
        int count = scanDirectory(uploadDir, "");
        logger.info("扫描完成，共发现 {} 个PDF文件", count);
    }
    
    /**
     * 递归扫描目录下的PDF文件并计算其哈希值
     * @param directory 目录
     * @param relativePath 相对路径
     * @return 扫描到的PDF文件数
     */
    private int scanDirectory(File directory, String relativePath) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        
        for (File file : files) {
            String currentRelativePath = relativePath.isEmpty() ? 
                file.getName() : relativePath + "/" + file.getName();
                
            if (file.isDirectory()) {
                count += scanDirectory(file, currentRelativePath);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                try {
                    String fileHash = calculateMD5(file);
                    fileHashPathMap.put(fileHash, currentRelativePath);
                    count++;
                    
                    if (count % 100 == 0) {
                        logger.info("已扫描 {} 个PDF文件", count);
                    }
                } catch (IOException e) {
                    logger.error("计算文件MD5失败: {}", file.getAbsolutePath(), e);
                }
            }
        }
        
        return count;
    }
    
    @Override
    public String saveProjectFile(MultipartFile file) throws Exception {
        // 计算文件MD5哈希值
        String fileHash = calculateMD5(file);
        logger.info("文件MD5: {}", fileHash);
        
        // 检查是否已存在相同内容的文件
        String existingFilePath = fileHashPathMap.get(fileHash);
        if (existingFilePath != null) {
            // 验证文件确实存在
            File existingFile = new File(uploadPath, existingFilePath);
            if (existingFile.exists()) {
                logger.info("文件已存在，直接返回路径: {}", existingFilePath);
                return "已存在|" + existingFilePath;
            } else {
                // 清除无效的缓存
                fileHashPathMap.remove(fileHash);
            }
        }
        
        // 生成文件名，使用UUID防止重名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString();
        
        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 使用日期文件夹对文件进行分类存储
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateDir = dateFormat.format(new Date());
        
        // 创建目录
        File dateDirectory = new File(uploadPath, dateDir);
        if (!dateDirectory.exists()) {
            boolean created = dateDirectory.mkdirs();
            if (created) {
                logger.info("创建日期目录: {}", dateDirectory.getAbsolutePath());
            } else {
                logger.error("创建日期目录失败: {}", dateDirectory.getAbsolutePath());
                throw new IOException("无法创建上传目录: " + dateDirectory.getAbsolutePath());
            }
        }
        
        // 完整的文件路径
        String fullFileName = fileName + extension;
        String relativePath = dateDir + "/" + fullFileName;
        File destFile = new File(dateDirectory, fullFileName);
        
        logger.info("保存文件到: {}", destFile.getAbsolutePath());
        
        // 保存文件
        try {
            file.transferTo(destFile);
            
            if (!destFile.exists()) {
                throw new IOException("文件保存失败，目标文件不存在: " + destFile.getAbsolutePath());
            }
            
            // 更新缓存
            fileHashPathMap.put(fileHash, relativePath);
            
            // 返回相对路径，用于存储到数据库
            return relativePath;
        } catch (IOException e) {
            logger.error("保存文件失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 计算文件的MD5哈希值
     * @param file MultipartFile文件
     * @return MD5哈希值
     * @throws IOException 计算失败
     */
    private String calculateMD5(MultipartFile file) throws IOException {
        return DigestUtils.md5DigestAsHex(file.getInputStream());
    }
    
    /**
     * 计算文件的MD5哈希值
     * @param file 本地文件
     * @return MD5哈希值
     * @throws IOException 计算失败
     */
    private String calculateMD5(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(fis);
        }
    }
} 