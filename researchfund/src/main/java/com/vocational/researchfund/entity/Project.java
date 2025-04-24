package com.vocational.researchfund.entity;

import com.vocational.researchfund.common.Constants.ProjectStatus;
import com.vocational.researchfund.common.Constants.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目类型：school-校级项目，horizontal-横向项目，vertical-纵向项目
     */
    private String type;

    /**
     * 项目负责人ID
     */
    private Long leaderId;

    /**
     * 项目负责人姓名
     */
    private String leaderName;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 总预算
     */
    private BigDecimal budget;

    /**
     * 已用预算
     */
    private BigDecimal usedBudget;

    /**
     * 项目状态：planning-筹划中，active-进行中，completed-已完成，suspended-已暂停
     */
    private String status;

    /**
     * 审核状态：pending-审核中，approved-审核通过，rejected-审核未通过
     */
    private String auditStatus;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 研究内容
     */
    private String researchContent;

    /**
     * 预期成果
     */
    private String expectedResults;
    
    /**
     * 项目申请书文件路径
     */
    private String filePath;
    
    /**
     * 结题报告文件路径
     */
    private String completionReportPath;
    
    /**
     * 结题审核状态：pending-待审核, approved-审核通过, rejected-审核不通过
     */
    private String completionAuditStatus;
    
    /**
     * 结题审核意见
     */
    private String completionAuditComment;
    
    /**
     * 结题报告提交时间
     */
    private LocalDate completionReportSubmitTime;
} 