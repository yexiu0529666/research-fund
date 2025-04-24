package com.vocational.researchfund.controller;

import com.alibaba.druid.util.StringUtils;
import com.vocational.researchfund.dto.AchievementDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.service.AchievementService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import com.vocational.researchfund.common.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成果奖励控制器
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private static final Logger logger = LoggerFactory.getLogger(AchievementController.class);

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserService userService;

    /**
     * 创建成果奖励
     */
    @PostMapping
    public ResponseEntity<Result<AchievementDTO>> createAchievement(@RequestBody AchievementDTO achievementDTO) {
        logger.info("创建成果奖励请求: {}", achievementDTO.getTitle());
        try {
            AchievementDTO result = achievementService.createAchievement(achievementDTO);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("创建成果奖励失败", e);
            return ResponseEntity.status(500).body(Result.fail("创建成果奖励失败: " + e.getMessage()));
        }
    }

    /**
     * 更新成果奖励
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<AchievementDTO>> updateAchievement(@PathVariable String id, @RequestBody AchievementDTO achievementDTO) {
        logger.info("更新成果奖励请求, id: {}", id);
        try {
            AchievementDTO result = achievementService.updateAchievement(id, achievementDTO);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("更新成果奖励失败", e);
            return ResponseEntity.status(500).body(Result.fail("更新成果奖励失败: " + e.getMessage()));
        }
    }

    /**
     * 审核成果奖励
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<Result<AchievementDTO>> auditAchievement(@PathVariable String id, @RequestBody Map<String, String> params) {
        String status = params.get("status");
        String comment = params.get("comment");
        Double awardAmount = Double.valueOf(params.get("awardAmount"));
        logger.info("审核成果奖励请求, id: {}, status: {}", id, status);
        
        try {
            AchievementDTO result = achievementService.auditAchievement(id, status, comment, awardAmount);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("审核成果奖励失败", e);
            return ResponseEntity.status(500).body(Result.fail("审核成果奖励失败: " + e.getMessage()));
        }
    }

    /**
     * 获取成果奖励详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<AchievementDTO>> getAchievement(@PathVariable String id) {
        logger.info("获取成果奖励详情请求, id: {}", id);
        try {
            AchievementDTO result = achievementService.getAchievementById(id);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取成果奖励详情失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取成果奖励详情失败: " + e.getMessage()));
        }
    }

    /**
     * 获取成果奖励列表
     */
    @GetMapping
    public ResponseEntity<Result<PageDTO<AchievementDTO>>> getAchievements(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            if (!StringUtils.isEmpty(title)) {
                params.put("title", title);
            }
            if (!StringUtils.isEmpty(type)) {
                params.put("type", type);
            }
            if (!StringUtils.isEmpty(projectId)) {
                params.put("projectId", projectId);
            }
            if (!StringUtils.isEmpty(status)) {
                params.put("status", status);
            }
            if (!StringUtils.isEmpty(startDate)) {
                params.put("startDate", startDate);
            }
            if (!StringUtils.isEmpty(endDate)) {
                params.put("endDate", endDate);
            }
            
            logger.info("获取成果奖励列表请求: pageNum={}, pageSize={}, params={}", pageNum, pageSize, params);

            
            PageDTO<AchievementDTO> result;
            
            if (isAdmin()) {
                // 管理员可以看到所有成果奖励
                logger.info("调用管理员方法获取所有成果");
                result = achievementService.getAllAchievements(params, pageNum, pageSize);
            } else {
                // 普通用户只能看到自己的成果奖励
                logger.info("调用普通用户方法获取成果");
                result = achievementService.getCurrentUserAchievements(params, pageNum, pageSize);
            }
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取成果奖励列表失败", e);
            return ResponseEntity.status(500).body(Result.fail("获取成果奖励列表失败: " + e.getMessage()));
        }
    }

    private boolean isAdmin() {
        // 获取当前登录用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);

        if (currentUser == null) {
            logger.warn("无法获取当前用户信息");
            return false;
        }

        // 获取用户角色
        List<String> userRoles = userService.listUserRoles(currentUser.getId());
        logger.info("当前用户: {}, 角色: {}", username, userRoles);
        return userRoles.contains("ROLE_ADMIN");
    }

    /**
     * 删除成果奖励
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteAchievement(@PathVariable String id) {
        logger.info("删除成果奖励请求, id: {}", id);
        try {
            achievementService.deleteAchievement(id);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("删除成果奖励失败", e);
            return ResponseEntity.status(500).body(Result.fail("删除成果奖励失败: " + e.getMessage()));
        }
    }

    /**
     * 支付成果奖励（仅管理员可操作）
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Result<AchievementDTO>> payAchievement(@PathVariable String id) {
        logger.info("支付成果奖励请求, id: {}", id);
        try {
            AchievementDTO achievement = achievementService.payAchievement(id);
            return ResponseEntity.ok(Result.success(achievement));
        } catch (Exception e) {
            logger.error("支付成果奖励失败", e);
            return ResponseEntity.status(500).body(Result.fail("支付成果奖励失败: " + e.getMessage()));
        }
    }
} 