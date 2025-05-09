package com.vocational.researchfund.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 项目预算科目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBudgetItem extends BaseEntity {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 预算科目：设备费、材料费、测试化验费、差旅费、会议费、劳务费、专家咨询费、其他费用
     */
    private String category;

    /**
     * 预算金额
     */
    private BigDecimal amount;
} 