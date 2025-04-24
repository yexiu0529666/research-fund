package com.vocational.researchfund.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成果奖励实体类
 */
@Data
public class Achievement {
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 成果名称
     */
    private String title;
    
    /**
     * 成果类型: journal(期刊论文), conference(会议论文), patent(专利), book(著作), software(软件著作权), other(其他)
     */
    private String type;
    
    /**
     * 成果级别: national(国家级), provincial(省部级), departmental(厅局级), school(校级), other(其他)
     */
    private String level;
    
    /**
     * 作者，以JSON数组格式存储
     */
    private String authors;
    
    /**
     * 获奖日期
     */
    private LocalDate awardDate;
    
    /**
     * 奖励金额
     */
    private BigDecimal awardAmount;
    
    /**
     * 相关项目ID
     */
    private String projectId;
    
    /**
     * 成果简介
     */
    private String description;
    
    /**
     * 状态: pending(待审核), approved(已通过), rejected(已拒绝)
     */
    private String status;
    
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    
    /**
     * 审核人ID
     */
    private String auditUserId;
    
    /**
     * 审核意见
     */
    private String auditComment;
    
    /**
     * 创建人ID
     */
    private String creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remarks;
    
    /**
     * 是否删除: 0(未删除), 1(已删除)
     */
    private Integer isDeleted;
} 