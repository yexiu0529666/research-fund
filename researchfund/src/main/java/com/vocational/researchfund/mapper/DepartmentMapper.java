package com.vocational.researchfund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vocational.researchfund.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门数据访问层
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
} 