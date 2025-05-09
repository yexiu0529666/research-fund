package com.vocational.researchfund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目经费到账DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFundArrivalDTO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 项目名称
     */
    private String projectName;
    
    /**
     * 经费来源
     */
    private String fundingSource;
    
    /**
     * 到账金额
     */
    private BigDecimal amount;
    
    /**
     * 到账日期
     */
    private LocalDate arrivalDate;
    
    /**
     * 到账凭证文件路径
     */
    private String voucherPath;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    /**
     * 项目总预算
     */
    private BigDecimal projectBudget;
    
    /**
     * 已到账总金额（包括当前记录）
     */
    private BigDecimal totalArrivedAmount;
    
    /**
     * 待到账金额
     */
    private BigDecimal pendingAmount;
} 