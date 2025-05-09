package com.vocational.researchfund.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目经费到账记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFundArrival extends BaseEntity {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 经费来源：fiscal-财政经费，school-校配套经费，other-其他经费
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
} 