package com.vocational.researchfund.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色代码
     */
    private String roleCode;
    
    /**
     * 显示顺序
     */
    private Integer sort;
    
    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
} 