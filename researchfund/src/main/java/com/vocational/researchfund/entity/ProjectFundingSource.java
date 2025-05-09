package com.vocational.researchfund.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目经费来源实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFundingSource extends BaseEntity {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 经费来源：fiscal-财政经费，school-校配套经费，other-其他经费
     */
    private String source;
} 