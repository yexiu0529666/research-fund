package com.vocational.researchfund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    /**
     * 项目ID
     */
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目类型
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
     * 项目状态
     */
    private String status;

    /**
     * 审核状态
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
     * 项目申请书提交时间
     */
    private LocalDate createTime;

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

    /**
     * 项目团队
     */
    private List<TeamMemberDTO> team;

    /**
     * 项目里程碑
     */
    private List<MilestoneDTO> milestones;

    /**
     * 团队成员DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberDTO {
        private Long id;
        private String name;
        private String role;
    }

    /**
     * 里程碑DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneDTO {
        private Long id;
        private String title;
        private LocalDate date;
        private String status;
        private String description;
    }
} 