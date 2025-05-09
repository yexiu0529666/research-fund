package com.vocational.researchfund.service;

import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectFundArrivalDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 项目经费到账服务接口
 */
public interface ProjectFundArrivalService {
    
    /**
     * 创建项目经费到账记录
     * @param fundArrivalDTO 经费到账DTO
     * @return 创建后的经费到账记录
     */
    ProjectFundArrivalDTO createFundArrival(ProjectFundArrivalDTO fundArrivalDTO);
    
    /**
     * 更新项目经费到账记录
     * @param id 记录ID
     * @param fundArrivalDTO 经费到账DTO
     * @return 更新后的经费到账记录
     */
    ProjectFundArrivalDTO updateFundArrival(Long id, ProjectFundArrivalDTO fundArrivalDTO);
    
    /**
     * 根据ID获取项目经费到账记录
     * @param id 记录ID
     * @return 经费到账记录
     */
    ProjectFundArrivalDTO getFundArrivalById(Long id);
    
    /**
     * 获取项目经费到账记录列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页后的经费到账记录列表
     */
    PageDTO<ProjectFundArrivalDTO> getFundArrivalList(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 获取项目的经费到账记录列表
     * @param projectId 项目ID
     * @return 经费到账记录列表
     */
    List<ProjectFundArrivalDTO> getFundArrivalsByProjectId(Long projectId);
    
    /**
     * 删除项目经费到账记录
     * @param id 记录ID
     */
    void deleteFundArrival(Long id);
    
    /**
     * 获取项目已到账的总金额
     * @param projectId 项目ID
     * @return 已到账总金额
     */
    BigDecimal getProjectTotalArrivedAmount(Long projectId);
    
    /**
     * 获取项目各经费来源的到账金额统计
     * @param projectId 项目ID
     * @return 各经费来源的到账金额统计
     */
    List<Map<String, Object>> getProjectFundArrivalBySource(Long projectId);
    
    /**
     * 获取可用于添加经费到账的项目列表（进行中的项目）
     * @return 可添加经费到账的项目列表
     */
    List<Map<String, Object>> getAvailableProjectsForFundArrival();
} 