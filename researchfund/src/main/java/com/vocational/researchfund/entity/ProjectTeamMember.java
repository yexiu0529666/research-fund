package com.vocational.researchfund.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 项目团队成员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTeamMember extends BaseEntity {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 成员姓名
     */
    private String name;

    /**
     * 成员角色
     */
    private String role;
} 