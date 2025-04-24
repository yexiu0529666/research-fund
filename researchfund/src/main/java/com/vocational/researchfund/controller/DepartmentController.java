package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.entity.Department;
import com.vocational.researchfund.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/api/department")
public class DepartmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    
    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 获取所有部门
     *
     * @return 所有部门列表
     */
    @GetMapping("/all")
    public Result<List<Department>> getAllDepartments() {
        try {
            logger.info("获取所有部门列表");
            List<Department> departments = departmentService.listAllDepartments();
            return Result.success(departments);
        } catch (Exception e) {
            logger.error("获取部门列表异常: {}", e.getMessage(), e);
            return Result.error("获取部门列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有有效部门
     *
     * @return 所有有效部门列表
     */
    @GetMapping("/active")
    public Result<List<Department>> getActiveDepartments() {
        try {
            logger.info("获取所有有效部门列表");
            List<Department> departments = departmentService.getActiveDepartments();
            return Result.success(departments);
        } catch (Exception e) {
            logger.error("获取有效部门列表异常: {}", e.getMessage(), e);
            return Result.error("获取有效部门列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过ID获取部门
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        try {
            logger.info("获取部门信息: id={}", id);
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                return Result.error("部门不存在");
            }
            return Result.success(department);
        } catch (Exception e) {
            logger.error("获取部门信息异常: {}", e.getMessage(), e);
            return Result.error("获取部门信息失败: " + e.getMessage());
        }
    }
} 