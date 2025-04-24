package com.vocational.researchfund.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocational.researchfund.entity.Department;
import com.vocational.researchfund.mapper.DepartmentMapper;
import com.vocational.researchfund.service.DepartmentService;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public Department getById(Long id) {
        if (id == null) {
            return null;
        }
        return super.getById(id);
    }
    
    @Override
    public List<Department> listAllDepartments() {
        return this.list();
    }
    
    @Override
    public List<Department> getActiveDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 0); // 只查询状态正常的部门
        wrapper.orderByAsc(Department::getOrderNum); // 按排序字段升序
        return this.list(wrapper);
    }
    
    @Override
    public Department getDepartmentById(Long id) {
        return this.getById(id);
    }
} 