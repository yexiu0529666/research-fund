package com.vocational.researchfund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 附件数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 经费申请ID
     */
    private Long expenseId;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件URL
     */
    private String url;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;
} 