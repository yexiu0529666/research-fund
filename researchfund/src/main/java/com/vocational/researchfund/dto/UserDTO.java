package com.vocational.researchfund.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
    
    /**
     * 个人简介
     */
    private String remark;
} 