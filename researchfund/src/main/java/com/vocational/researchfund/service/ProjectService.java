package com.vocational.researchfund.service;

import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.dto.PageDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 项目服务接口
 */
public interface ProjectService {
    /**
     * 根据ID获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectDTO getProjectById(Long id);
    
    /**
     * 获取项目列表（分页）
     * @param params 查询参数，可包含：name, type, status, auditStatus
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的项目列表
     */
    PageDTO<ProjectDTO> getProjectList(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 创建项目
     * @param projectDTO 项目DTO
     * @return 创建后的项目
     */
    ProjectDTO createProject(ProjectDTO projectDTO);
    
    /**
     * 更新项目
     * @param id 项目ID
     * @param projectDTO 项目DTO
     * @return 更新后的项目
     */
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    
    /**
     * 删除项目
     * @param id 项目ID
     */
    void deleteProject(Long id);
    
    /**
     * 审核项目
     * @param id 项目ID
     * @param auditStatus 审核状态
     * @param comment 审核意见
     * @return 审核后的项目
     */
    ProjectDTO auditProject(Long id, String auditStatus, String comment);
    
    /**
     * 获取当前用户的项目列表（分页）
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的项目列表
     */
    PageDTO<ProjectDTO> getCurrentUserProjects(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 确认立项 - 将项目状态更新为待审核
     * @param id 项目ID
     * @return 更新后的项目
     */
    ProjectDTO confirmProject(Long id);
    
    /**
     * 获取当前用户可用于报销申请的项目列表
     * 这些项目必须满足条件：
     * 1. 该用户是项目负责人
     * 2. 项目审核状态为audit
     * 3. 项目未删除
     * @return 可用项目列表
     */
    List<ProjectDTO> getAvailableProjectsForExpense();
    
    /**
     * 检查并更新已过期项目状态
     * 将已过结束日期但状态仍为active的项目更新为待结题状态
     */
    void checkAndUpdateExpiredProjects();
    
    /**
     * 提交项目结题报告
     * @param id 项目ID
     * @param reportPath 结题报告文件路径
     * @return 更新后的项目
     */
    ProjectDTO submitCompletionReport(Long id, String reportPath);
    
    /**
     * 审核项目结题
     * @param id 项目ID
     * @param auditStatus 审核状态: approved-通过, rejected-不通过
     * @param comment 审核意见
     * @return 更新后的项目
     */
    ProjectDTO auditProjectCompletion(Long id, String auditStatus, String comment);
    
    /**
     * 更新项目已用预算
     * @param id 项目ID
     * @param amount 增加的预算金额
     * @return 更新后的项目
     */
    ProjectDTO updateUsedBudget(Long id, BigDecimal amount);
    
    /**
     * 根据状态获取项目列表
     * @param status 项目状态
     * @return 项目列表
     */
    List<ProjectDTO> getProjectsByStatus(String status);
} 