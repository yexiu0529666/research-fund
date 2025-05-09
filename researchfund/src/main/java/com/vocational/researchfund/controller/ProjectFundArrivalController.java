package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectFundArrivalDTO;
import com.vocational.researchfund.service.ProjectFundArrivalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目经费到账控制器
 */
@RestController
@RequestMapping("/api/project-fund-arrivals")
public class ProjectFundArrivalController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFundArrivalController.class);

    @Autowired
    private ProjectFundArrivalService projectFundArrivalService;

    /**
     * 获取经费到账记录列表（分页）
     * @param projectId 项目ID
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 分页后的经费到账记录列表
     */
    @GetMapping
    public ResponseEntity<Result<PageDTO<ProjectFundArrivalDTO>>> getFundArrivalList(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        
        Map<String, Object> params = new HashMap<>();
        if (projectId != null) params.put("projectId", projectId);
        
        PageDTO<ProjectFundArrivalDTO> pagedFundArrivals = projectFundArrivalService.getFundArrivalList(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedFundArrivals));
    }

    /**
     * 获取项目的经费到账记录列表
     * @param projectId 项目ID
     * @return 经费到账记录列表
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Result<List<ProjectFundArrivalDTO>>> getProjectFundArrivals(@PathVariable Long projectId) {
        try {
            List<ProjectFundArrivalDTO> fundArrivals = projectFundArrivalService.getFundArrivalsByProjectId(projectId);
            return ResponseEntity.ok(Result.success(fundArrivals));
        } catch (Exception e) {
            logger.error("获取项目经费到账记录失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取项目经费到账记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目已到账总金额
     * @param projectId 项目ID
     * @return 已到账总金额
     */
    @GetMapping("/project/{projectId}/total-amount")
    public ResponseEntity<Result<BigDecimal>> getProjectTotalArrivedAmount(@PathVariable Long projectId) {
        try {
            BigDecimal totalAmount = projectFundArrivalService.getProjectTotalArrivedAmount(projectId);
            return ResponseEntity.ok(Result.success(totalAmount));
        } catch (Exception e) {
            logger.error("获取项目已到账总金额失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取项目已到账总金额失败: " + e.getMessage()));
        }
    }

    /**
     * 获取经费到账记录详情
     * @param id 记录ID
     * @return 经费到账记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ProjectFundArrivalDTO>> getFundArrivalDetail(@PathVariable Long id) {
        try {
            ProjectFundArrivalDTO fundArrival = projectFundArrivalService.getFundArrivalById(id);
            if (fundArrival == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Result.success(fundArrival));
        } catch (Exception e) {
            logger.error("获取经费到账记录详情失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取经费到账记录详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建经费到账记录
     * @param fundArrivalDTO 经费到账DTO
     * @return 创建后的经费到账记录
     */
    @PostMapping
    public ResponseEntity<Result<ProjectFundArrivalDTO>> createFundArrival(@RequestBody ProjectFundArrivalDTO fundArrivalDTO) {
        try {
            ProjectFundArrivalDTO createdFundArrival = projectFundArrivalService.createFundArrival(fundArrivalDTO);
            return ResponseEntity.ok(Result.success(createdFundArrival));
        } catch (Exception e) {
            logger.error("创建经费到账记录失败", e);
            return ResponseEntity.badRequest().body(Result.fail("创建经费到账记录失败: " + e.getMessage()));
        }
    }

    /**
     * 更新经费到账记录
     * @param id 记录ID
     * @param fundArrivalDTO 经费到账DTO
     * @return 更新后的经费到账记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<ProjectFundArrivalDTO>> updateFundArrival(
            @PathVariable Long id,
            @RequestBody ProjectFundArrivalDTO fundArrivalDTO) {
        try {
            ProjectFundArrivalDTO updatedFundArrival = projectFundArrivalService.updateFundArrival(id, fundArrivalDTO);
            return ResponseEntity.ok(Result.success(updatedFundArrival));
        } catch (Exception e) {
            logger.error("更新经费到账记录失败", e);
            return ResponseEntity.badRequest().body(Result.fail("更新经费到账记录失败: " + e.getMessage()));
        }
    }

    /**
     * 删除经费到账记录
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteFundArrival(@PathVariable Long id) {
        try {
            projectFundArrivalService.deleteFundArrival(id);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("删除经费到账记录失败", e);
            return ResponseEntity.badRequest().body(Result.fail("删除经费到账记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取可用于添加经费到账的项目列表（进行中的项目）
     * @return 可用项目列表
     */
    @GetMapping("/available-projects")
    public ResponseEntity<Result<List<Map<String, Object>>>> getAvailableProjects() {
        try {
            List<Map<String, Object>> projects = projectFundArrivalService.getAvailableProjectsForFundArrival();
            return ResponseEntity.ok(Result.success(projects));
        } catch (Exception e) {
            logger.error("获取可用于添加经费到账的项目列表失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取可用于添加经费到账的项目列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目各经费来源的到账金额统计
     * @param projectId 项目ID
     * @return 各经费来源的到账金额统计
     */
    @GetMapping("/project/{projectId}/by-source")
    public ResponseEntity<Result<List<Map<String, Object>>>> getProjectFundArrivalBySource(@PathVariable Long projectId) {
        try {
            List<Map<String, Object>> fundArrivalBySource = projectFundArrivalService.getProjectFundArrivalBySource(projectId);
            return ResponseEntity.ok(Result.success(fundArrivalBySource));
        } catch (Exception e) {
            logger.error("获取项目各经费来源的到账金额统计失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取项目各经费来源的到账金额统计失败: " + e.getMessage()));
        }
    }
} 