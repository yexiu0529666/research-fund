package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘待办事项
     * @param isAdmin 是否为管理员
     * @return 待办事项列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<Result<List<Map<String, Object>>>> getDashboardTasks(
            @RequestParam(required = false, defaultValue = "false") Boolean isAdmin) {
        try {
            List<Map<String, Object>> tasks = dashboardService.getDashboardTasks(isAdmin);
            return ResponseEntity.ok(Result.success(tasks));
        } catch (Exception e) {
            logger.error("获取仪表盘待办事项失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取仪表盘待办事项失败: " + e.getMessage()));
        }
    }

    /**
     * 获取仪表盘项目概览
     * @param isAdmin 是否为管理员
     * @return 项目概览列表
     */
    @GetMapping("/projects")
    public ResponseEntity<Result<List<Map<String, Object>>>> getDashboardProjects(
            @RequestParam(required = false, defaultValue = "false") Boolean isAdmin) {
        try {
            List<Map<String, Object>> projects = dashboardService.getDashboardProjects(isAdmin);
            return ResponseEntity.ok(Result.success(projects));
        } catch (Exception e) {
            logger.error("获取仪表盘项目概览失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取仪表盘项目概览失败: " + e.getMessage()));
        }
    }

    /**
     * 获取仪表盘统计数据
     * @param isAdmin 是否为管理员
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getDashboardStats(
            @RequestParam(required = false, defaultValue = "false") Boolean isAdmin) {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats(isAdmin);
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取仪表盘统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目类型统计
     * @param isAdmin 是否为管理员
     * @return 项目类型统计
     */
    @GetMapping("/project-types")
    public ResponseEntity<Result<List<Map<String, Object>>>> getProjectTypeStats(
            @RequestParam(required = false, defaultValue = "false") Boolean isAdmin) {
        try {
            List<Map<String, Object>> typeStats = dashboardService.getProjectTypeStats(isAdmin);
            return ResponseEntity.ok(Result.success(typeStats));
        } catch (Exception e) {
            logger.error("获取项目类型统计失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取项目类型统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取研究成果统计
     * @param isAdmin 是否为管理员
     * @return 研究成果统计
     */
    @GetMapping("/research-results")
    public ResponseEntity<Result<List<Map<String, Object>>>> getResearchResults(
            @RequestParam(required = false, defaultValue = "false") Boolean isAdmin) {
        try {
            List<Map<String, Object>> results = dashboardService.getResearchResults(isAdmin);
            return ResponseEntity.ok(Result.success(results));
        } catch (Exception e) {
            logger.error("获取研究成果统计失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取研究成果统计失败: " + e.getMessage()));
        }
    }
} 