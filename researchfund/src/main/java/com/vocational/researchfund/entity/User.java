package com.vocational.researchfund.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 部门名称
     */
    @TableField(exist = false)
    private String departmentName;
    
    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
    
    /**
     * 性别（0未知 1男 2女）
     */
    private Integer gender;
    
    /**
     * 个人简介
     */
    private String remark;
    
    /**
     * 用户角色列表，包含id和name
     */
    @TableField(exist = false)
    private List<Map<String, Object>> roles;
    
    /**
     * 用户角色ID列表，用于前端编辑表单
     */
    @TableField(exist = false)
    private List<Long> roleIds;
} 