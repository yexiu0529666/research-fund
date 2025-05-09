package com.vocational.researchfund.repository;

import com.vocational.researchfund.entity.ProjectFundingSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目经费来源数据访问接口
 */
@Mapper
public interface ProjectFundingSourceRepository {
    
    /**
     * 批量插入项目经费来源
     * @param projectId 项目ID
     * @param fundingSources 经费来源列表
     * @param createBy 创建者ID
     * @return 影响行数
     */
    int batchInsert(@Param("projectId") Long projectId, 
                    @Param("fundingSources") List<String> fundingSources,
                    @Param("createBy") Long createBy);
    
    /**
     * 根据项目ID查询经费来源列表
     * @param projectId 项目ID
     * @return 经费来源列表
     */
    List<ProjectFundingSource> findByProjectId(Long projectId);
    
    /**
     * 根据项目ID删除经费来源
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteByProjectId(Long projectId);
} 