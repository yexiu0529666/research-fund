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

import java.math.BigDecimal;
import java.util.ArrayList;
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

    /**
     * 获取项目可用预算科目列表
     * 
     * @param projectId 项目ID
     * @return 项目预算科目列表及剩余预算
     */
    @GetMapping("/{projectId}/budget-items")
    public ResponseEntity<Result<List<Map<String, Object>>>> getProjectBudgetItems(@PathVariable Long projectId) {
        try {
            // 获取项目详情，包含预算科目
            ProjectDTO project = projectService.getProjectById(projectId);
            if (project == null) {
                return ResponseEntity.badRequest().body(Result.error("项目不存在"));
            }
            
            // 获取项目的经费使用情况按类型统计
            List<Map<String, Object>> expenseStats = expenseMapper.getExpenseStatsByProject(projectId);
            Map<String, BigDecimal> usedAmountByType = new HashMap<>();
            
            // 将经费使用情况转换为Map便于查找
            if (expenseStats != null) {
                for (Map<String, Object> stat : expenseStats) {
                    String type = (String) stat.get("type");
                    BigDecimal amount = (BigDecimal) stat.get("amount");
                    if (type != null && amount != null) {
                        usedAmountByType.put(type, amount);
                    }
                }
            }
            
            // 计算每个预算科目的剩余可用预算
            List<Map<String, Object>> result = new ArrayList<>();
            if (project.getBudgetItems() != null) {
                for (ProjectDTO.BudgetItemDTO item : project.getBudgetItems()) {
                    String category = transformCategoryToType(item.getCategory());
                    BigDecimal budgetAmount = item.getAmount();
                    BigDecimal usedAmount = usedAmountByType.getOrDefault(category, BigDecimal.ZERO);
                    BigDecimal remainingAmount = budgetAmount.subtract(usedAmount);
                    
                    // 如果剩余预算大于0，则添加到可用列表
                    if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                        Map<String, Object> budgetItem = new HashMap<>();
                        budgetItem.put("category", item.getCategory());
                        budgetItem.put("type", category);
                        budgetItem.put("budgetAmount", budgetAmount);
                        budgetItem.put("usedAmount", usedAmount);
                        budgetItem.put("remainingAmount", remainingAmount);
                        result.add(budgetItem);
                    }
                }
            }
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error("获取项目预算科目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将项目预算科目名称转换为经费申请类型
     * 
     * @param category 预算科目名称
     * @return 对应的经费申请类型
     */
    private String transformCategoryToType(String category) {
        Map<String, String> categoryTypeMap = new HashMap<>();
        categoryTypeMap.put("设备费", "equipment");
        categoryTypeMap.put("材料费", "material");
        categoryTypeMap.put("测试化验费", "test");
        categoryTypeMap.put("差旅费", "travel");
        categoryTypeMap.put("会议费", "meeting");
        categoryTypeMap.put("劳务费", "labor");
        categoryTypeMap.put("专家咨询费", "consultation");
        categoryTypeMap.put("其他费用", "other");
        
        return categoryTypeMap.getOrDefault(category, "other");
    }
} 