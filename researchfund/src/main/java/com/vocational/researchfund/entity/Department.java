package com.vocational.researchfund.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class Department extends BaseEntity {
    
    /**
     * 部门名称
     */
    private String name;
    
    /**
     * 部门编码
     */
    private String code;
    
    /**
     * 父部门ID
     */
    private Long parentId;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 负责人
     */
    private Long leaderId;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
} 