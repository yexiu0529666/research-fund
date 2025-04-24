package com.vocational.researchfund.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface UploadService {
    
    /**
     * 保存项目申请书文件
     * @param file 上传的文件
     * @return 文件存储路径
     * @throws Exception 上传异常
     */
    String saveProjectFile(MultipartFile file) throws Exception;
} 