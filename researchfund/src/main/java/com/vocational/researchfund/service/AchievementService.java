package com.vocational.researchfund.service;

import com.vocational.researchfund.dto.AchievementDTO;
import com.vocational.researchfund.dto.PageDTO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 成果奖励服务接口
 */
public interface AchievementService {
    
    /**
     * 创建成果奖励
     * @param achievementDTO 成果奖励DTO
     * @return 创建后的成果奖励
     */
    AchievementDTO createAchievement(AchievementDTO achievementDTO);
    
    /**
     * 更新成果奖励
     * @param id 成果奖励ID
     * @param achievementDTO 成果奖励DTO
     * @return 更新后的成果奖励
     */
    AchievementDTO updateAchievement(String id, AchievementDTO achievementDTO);
    
    /**
     * 审核成果奖励
     * @param id 成果奖励ID
     * @param status 审核状态
     * @param comment 审核意见
     * @return 审核后的成果奖励
     */
    AchievementDTO auditAchievement(String id, String status, String comment, Double awardAmount);
    
    /**
     * 获取成果奖励详情
     * @param id 成果奖励ID
     * @return 成果奖励详情
     */
    AchievementDTO getAchievementById(String id);
    
    /**
     * 获取当前用户的成果奖励列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的成果奖励列表
     */
    PageDTO<AchievementDTO> getCurrentUserAchievements(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 获取所有成果奖励列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的成果奖励列表
     */
    PageDTO<AchievementDTO> getAllAchievements(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 删除成果奖励
     * @param id 成果奖励ID
     */
    void deleteAchievement(String id);
    
    /**
     * 支付成果奖励
     * @param id 成果ID
     * @return 更新后的成果DTO
     */
    AchievementDTO payAchievement(String id);
} 