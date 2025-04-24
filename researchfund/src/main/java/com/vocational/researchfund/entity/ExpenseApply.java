package com.vocational.researchfund.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 经费申请实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseApply extends BaseEntity {

    /**
     * 申请标题
     */
    private String title;

    /**
     * 申请类别：advance-经费借款,reimbursement-报销
     */
    private String category;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 所属项目名称
     */
    private String projectName;

    /**
     * 申请类型：equipment-设备费,material-材料费,test-测试化验费,travel-差旅费,
     * meeting-会议费,labor-劳务费,consultation-专家咨询费,other-其他费用
     */
    private String type;

    /**
     * 申请金额
     */
    private BigDecimal amount;

    /**
     * 申请日期
     */
    private LocalDate applyDate;

    /**
     * 预计用途
     */
    private String purpose;

    /**
     * 申请理由
     */
    private String reason;

    /**
     * 申请人ID
     */
    private Long applyUserId;

    /**
     * 申请人姓名
     */
    private String applyUserName;

    /**
     * 状态：pending-待审核,approved-已批准,rejected-已拒绝,paid-已支付
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
     * 附件列表（非数据库字段）
     */
    private List<ExpenseAttachment> attachments;
} 