package com.vocational.researchfund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vocational.researchfund.entity.ProjectBudgetItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目预算科目Mapper接口
 */
@Mapper
public interface ProjectBudgetItemMapper extends BaseMapper<ProjectBudgetItem> {
    
    /**
     * 根据项目ID查询预算科目列表
     * 
     * @param projectId 项目ID
     * @return 预算科目列表
     */
    @Select("SELECT * FROM project_budget_item WHERE project_id = #{projectId} AND deleted = 0")
    List<ProjectBudgetItem> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 批量插入预算科目
     * 
     * @param items 预算科目列表
     * @return 影响行数
     */
    int batchInsert(@Param("items") List<ProjectBudgetItem> items);
    
    /**
     * 根据项目ID删除预算科目
     * 
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);
} 