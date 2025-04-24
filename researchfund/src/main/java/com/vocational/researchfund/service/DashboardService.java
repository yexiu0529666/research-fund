package com.vocational.researchfund.service;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘待办事项
     * @param isAdmin 是否为管理员
     * @return 待办事项列表
     */
    List<Map<String, Object>> getDashboardTasks(Boolean isAdmin);

    /**
     * 获取仪表盘项目概览
     * @param isAdmin 是否为管理员
     * @return 项目概览列表
     */
    List<Map<String, Object>> getDashboardProjects(Boolean isAdmin);

    /**
     * 获取仪表盘统计数据
     * @param isAdmin 是否为管理员
     * @return 统计数据
     */
    Map<String, Object> getDashboardStats(Boolean isAdmin);

    /**
     * 获取项目类型统计
     * @param isAdmin 是否为管理员
     * @return 项目类型统计
     */
    List<Map<String, Object>> getProjectTypeStats(Boolean isAdmin);

    /**
     * 获取研究成果统计
     * @param isAdmin 是否为管理员
     * @return 研究成果统计
     */
    List<Map<String, Object>> getResearchResults(Boolean isAdmin);
} 