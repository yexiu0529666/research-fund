package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.service.ProjectService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.mapper.ExpenseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ExpenseMapper expenseMapper;

    /**
     * 获取项目列表（分页）
     * @param name 项目名称
     * @param type 项目类型
     * @param status 项目状态
     * @param auditStatus 审核状态
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 分页后的项目列表
     */
    @GetMapping
    public ResponseEntity<Result<PageDTO<ProjectDTO>>> getProjectList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        
        Map<String, Object> params = new HashMap<>();
        if (name != null) params.put("name", name);
        if (type != null) params.put("type", type);
        if (status != null) params.put("status", status);
        if (auditStatus != null) params.put("auditStatus", auditStatus);
        
        PageDTO<ProjectDTO> pagedProjects = projectService.getProjectList(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedProjects));
    }

    /**
     * 获取当前用户的项目列表（分页）
     * @param name 项目名称
     * @param type 项目类型
     * @param status 项目状态
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 分页后的项目列表
     */
    @GetMapping("/my")
    public ResponseEntity<Result<PageDTO<ProjectDTO>>> getMyProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        
        Map<String, Object> params = new HashMap<>();
        if (name != null) params.put("name", name);
        if (type != null) params.put("type", type);
        if (status != null) params.put("status", status);

        PageDTO<ProjectDTO> pagedProjects = projectService.getCurrentUserProjects(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedProjects));
    }
    
    /**
     * 获取可用于报销的项目列表
     * @return 可用项目列表
     */
    @GetMapping("/available-for-expense")
    public ResponseEntity<Result<List<ProjectDTO>>> getAvailableProjectsForExpense() {
        try {
            List<ProjectDTO> projects = projectService.getAvailableProjectsForExpense();
            return ResponseEntity.ok(Result.success(projects));
        } catch (Exception e) {
            logger.error("获取可用于经费申请的项目列表失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取可用于经费申请的项目列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ProjectDTO>> getProjectDetail(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(Result.success(project));
    }

    /**
     * 创建项目
     * @param projectDTO 项目DTO
     * @return 创建后的项目
     */
    @PostMapping
    public ResponseEntity<Result<ProjectDTO>> createProject(@RequestBody ProjectDTO projectDTO) {
        ProjectDTO createdProject = projectService.createProject(projectDTO);
        return ResponseEntity.ok(Result.success(createdProject));
    }

    /**
     * 更新项目
     * @param id 项目ID
     * @param projectDTO 项目DTO
     * @return 更新后的项目
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<ProjectDTO>> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectDTO projectDTO) {
        ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
        return ResponseEntity.ok(Result.success(updatedProject));
    }

    /**
     * 删除项目
     * @param id 项目ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Result.success());
    }

    /**
     * 审核项目
     * @param id 项目ID
     * @param auditStatus 审核状态
     * @param comment 审核意见
     * @return 审核后的项目
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<Result<ProjectDTO>> auditProject(
            @PathVariable Long id,
            @RequestParam String auditStatus,
            @RequestParam(required = false) String comment) {
        ProjectDTO auditedProject = projectService.auditProject(id, auditStatus, comment);
        return ResponseEntity.ok(Result.success(auditedProject));
    }

    /**
     * 确认立项 - 将项目提交审核
     * @param id 项目ID
     * @return 更新后的项目
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Result<ProjectDTO>> confirmProject(@PathVariable Long id) {
        ProjectDTO confirmedProject = projectService.confirmProject(id);
        return ResponseEntity.ok(Result.success(confirmedProject));
    }

    /**
     * 获取项目经费使用统计
     * @param id 项目ID
     * @return 项目经费使用统计
     */
    @GetMapping("/{id}/expense-stats")
    public ResponseEntity<Result<List<Map<String, Object>>>> getProjectExpenseStats(@PathVariable Long id) {
        try {
            ProjectDTO project = projectService.getProjectById(id);
            if (project == null) {
                return ResponseEntity.status(404).body(Result.fail("项目不存在"));
            }

            // 获取项目的所有已批准或已支付的经费申请，按类型分组汇总
            List<Map<String, Object>> stats = expenseMapper.getExpenseStatsByProject(id);
            
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            logger.error("获取项目经费使用统计失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取项目经费使用统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待结题状态的项目列表（用于经费结转）
     * @return 待结题状态的项目列表
     */
    @GetMapping("/pending-completion")
    public ResponseEntity<Result<List<ProjectDTO>>> getPendingCompletionProjects() {
        try {
            List<ProjectDTO> projects = projectService.getProjectsByStatus("pending_completion");
            return ResponseEntity.ok(Result.success(projects));
        } catch (Exception e) {
            logger.error("获取待结题项目列表失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取待结题项目列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 手动检查并更新已过期项目状态
     * @return 结果
     */
    @PostMapping("/check-expired")
    public ResponseEntity<Result<Void>> checkExpiredProjects() {
        projectService.checkAndUpdateExpiredProjects();
        return ResponseEntity.ok(Result.success());
    }
    
    /**
     * 提交项目结题报告
     * @param id 项目ID
     * @param reportPath 报告文件路径
     * @return 更新后的项目
     */
    @PostMapping("/{id}/completion-report")
    public ResponseEntity<Result<ProjectDTO>> submitCompletionReport(
            @PathVariable Long id,
            @RequestParam String reportPath) {
        try {
            ProjectDTO project = projectService.submitCompletionReport(id, reportPath);
            return ResponseEntity.ok(Result.success(project));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 审核项目结题
     * @param id 项目ID
     * @param auditStatus 审核状态
     * @param comment 审核意见
     * @return 更新后的项目
     */
    @PostMapping("/{id}/completion-audit")
    public ResponseEntity<Result<ProjectDTO>> auditProjectCompletion(
            @PathVariable Long id,
            @RequestParam String auditStatus,
            @RequestParam(required = false) String comment) {
        try {
            ProjectDTO project = projectService.auditProjectCompletion(id, auditStatus, comment);
            return ResponseEntity.ok(Result.success(project));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
} 