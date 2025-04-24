package com.vocational.researchfund.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vocational.researchfund.entity.Department;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取部门信息
     * 
     * @param id 部门ID
     * @return 部门信息
     */
    Department getById(Long id);
    
    /**
     * 获取所有部门列表
     * 
     * @return 部门列表
     */
    List<Department> listAllDepartments();
    
    /**
     * 获取有效的部门列表（状态正常）
     * 
     * @return 部门列表
     */
    List<Department> getActiveDepartments();
    
    /**
     * 通过ID获取部门信息
     * 
     * @param id 部门ID
     * @return 部门信息
     */
    Department getDepartmentById(Long id);
} 