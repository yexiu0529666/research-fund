package com.vocational.researchfund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 成果奖励附件数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementAttachmentDTO {

    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 成果奖励ID
     */
    private String achievementId;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件路径
     */
    private String path;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
} 