package com.vocational.researchfund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 经费结转数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferDTO {

    /**
     * 结转ID
     */
    private Long id;

    /**
     * 结转标题
     */
    private String title;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 所属项目名称
     */
    private String projectName;

    /**
     * 结转金额
     */
    private BigDecimal amount;

    /**
     * 结转日期
     */
    private LocalDate applyDate;

    /**
     * 结转原因
     */
    private String reason;

    /**
     * 结转说明
     */
    private String description;

    /**
     * 结转年度（从哪一年）
     */
    private String fromYear;

    /**
     * 结转年度（到哪一年）
     */
    private String toYear;

    /**
     * 申请人ID
     */
    private Long applyUserId;

    /**
     * 申请人姓名
     */
    private String applyUserName;

    /**
     * 状态：pending-待审核,approved-已通过,rejected-已拒绝,completed-已完成
     */
    private String status;

    /**
     * 审核人ID
     */
    private Long auditUserId;

    /**
     * 审核人姓名
     */
    private String auditUserName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 