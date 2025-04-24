package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.dto.AchievementDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.AchievementAttachmentDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.entity.Achievement;
import com.vocational.researchfund.entity.AchievementAttachment;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.AchievementMapper;
import com.vocational.researchfund.mapper.AchievementAttachmentMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.mapper.UserMapper;
import com.vocational.researchfund.service.AchievementService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 成果奖励服务实现类
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementServiceImpl.class);

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private AchievementAttachmentMapper achievementAttachmentMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AchievementDTO createAchievement(AchievementDTO achievementDTO) {
        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }

        // 创建成果奖励
        Achievement achievement = new Achievement();
        achievement.setId(UUID.randomUUID().toString());
        achievement.setTitle(achievementDTO.getTitle());
        achievement.setType(achievementDTO.getType());
        achievement.setLevel(achievementDTO.getLevel());
        achievement.setAwardDate(achievementDTO.getAwardDate());
        achievement.setAwardAmount(achievementDTO.getAwardAmount());
        achievement.setProjectId(achievementDTO.getProjectId());
        achievement.setDescription(achievementDTO.getDescription());
        achievement.setRemarks(achievementDTO.getRemarks());
        achievement.setStatus("pending"); // 初始状态为待审核
        achievement.setCreatorId(currentUser.getId().toString());

        // 处理作者
        try {
            if (achievementDTO.getAuthors() != null) {
                achievement.setAuthors(objectMapper.writeValueAsString(achievementDTO.getAuthors()));
            }
        } catch (JsonProcessingException e) {
            logger.error("处理作者数据失败", e);
            throw new RuntimeException("处理作者数据失败");
        }

        // 插入成果奖励
        achievementMapper.insert(achievement);

        // 处理附件
        if (achievementDTO.getAttachments() != null && !achievementDTO.getAttachments().isEmpty()) {
            List<AchievementAttachment> attachments = new ArrayList<>();
            for (AchievementAttachmentDTO attachmentDTO : achievementDTO.getAttachments()) {
                AchievementAttachment attachment = new AchievementAttachment();
                attachment.setId(UUID.randomUUID().toString());
                attachment.setAchievementId(achievement.getId());
                attachment.setName(attachmentDTO.getName());
                attachment.setPath(attachmentDTO.getPath());
                attachment.setFileSize(attachmentDTO.getFileSize());
                attachment.setFileType(attachmentDTO.getFileType());
                attachments.add(attachment);
            }
            achievementAttachmentMapper.batchInsert(attachments, achievement.getId());
        }

        return getAchievementById(achievement.getId());
    }

    @Override
    @Transactional
    public AchievementDTO updateAchievement(String id, AchievementDTO achievementDTO) {
        // 获取现有成果奖励
        Achievement existingAchievement = achievementMapper.selectById(id);
        if (existingAchievement == null || existingAchievement.getIsDeleted() == 1) {
            throw new RuntimeException("成果奖励不存在");
        }

        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);


        // 更新成果奖励
        existingAchievement.setTitle(achievementDTO.getTitle());
        existingAchievement.setType(achievementDTO.getType());
        existingAchievement.setLevel(achievementDTO.getLevel());
        existingAchievement.setAwardDate(achievementDTO.getAwardDate());
        existingAchievement.setAwardAmount(achievementDTO.getAwardAmount());
        existingAchievement.setProjectId(achievementDTO.getProjectId());
        existingAchievement.setDescription(achievementDTO.getDescription());
        existingAchievement.setRemarks(achievementDTO.getRemarks());

        // 处理作者
        try {
            if (achievementDTO.getAuthors() != null) {
                existingAchievement.setAuthors(objectMapper.writeValueAsString(achievementDTO.getAuthors()));
            }
        } catch (JsonProcessingException e) {
            logger.error("处理作者数据失败", e);
            throw new RuntimeException("处理作者数据失败");
        }

        // 更新成果奖励
        achievementMapper.update(existingAchievement);

        // 处理附件 - 先删除旧附件
        achievementAttachmentMapper.deleteByAchievementId(id);

        // 保存新附件
        if (achievementDTO.getAttachments() != null && !achievementDTO.getAttachments().isEmpty()) {
            List<AchievementAttachment> attachments = new ArrayList<>();
            for (AchievementAttachmentDTO attachmentDTO : achievementDTO.getAttachments()) {
                AchievementAttachment attachment = new AchievementAttachment();
                attachment.setId(UUID.randomUUID().toString());
                attachment.setAchievementId(id);
                attachment.setName(attachmentDTO.getName());
                attachment.setPath(attachmentDTO.getPath());
                attachment.setFileSize(attachmentDTO.getFileSize());
                attachment.setFileType(attachmentDTO.getFileType());
                attachments.add(attachment);
            }
            achievementAttachmentMapper.batchInsert(attachments, id);
        }

        return getAchievementById(id);
    }

    @Override
    @Transactional
    public AchievementDTO auditAchievement(String id, String status, String comment, Double awardAmount) {
        // 获取成果奖励
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement == null || achievement.getIsDeleted() == 1) {
            throw new RuntimeException("成果奖励不存在");
        }

        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);

        // 更新审核状态
        achievementMapper.updateStatus(id, status, currentUser.getId().toString(), comment, awardAmount);

        return getAchievementById(id);
    }

    @Override
    public AchievementDTO getAchievementById(String id) {
        // 获取成果奖励
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement == null || achievement.getIsDeleted() == 1) {
            throw new RuntimeException("成果奖励不存在");
        }

        return convertToDTO(achievement);
    }

    @Override
    public PageDTO<AchievementDTO> getCurrentUserAchievements(Map<String, Object> params, int pageNum, int pageSize) {
        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        String creatorId = currentUser.getId().toString();

        // 计算分页
        int offset = (pageNum - 1) * pageSize;
        params.put("offset", offset);
        params.put("limit", pageSize);

        // 查询成果奖励
        List<Achievement> achievements = achievementMapper.selectByCreatorId(creatorId, params);
        int total = achievementMapper.countByCreatorId(creatorId, params);

        // 转换为DTO
        List<AchievementDTO> achievementDTOs = achievements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageDTO<AchievementDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(achievementDTOs);
        pageDTO.setTotal(total);
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);

        return pageDTO;
    }

    @Override
    public PageDTO<AchievementDTO> getAllAchievements(Map<String, Object> params, int pageNum, int pageSize) {
        // 计算分页
        int offset = (pageNum - 1) * pageSize;
        params.put("offset", offset);
        params.put("limit", pageSize);

        // 查询成果奖励
        List<Achievement> achievements = achievementMapper.selectByParams(params);
        int total = achievementMapper.countByParams(params);

        // 转换为DTO
        List<AchievementDTO> achievementDTOs = achievements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageDTO<AchievementDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(achievementDTOs);
        pageDTO.setTotal(total);
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);

        return pageDTO;
    }

    @Override
    @Transactional
    public void deleteAchievement(String id) {
        // 获取成果奖励
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement == null || achievement.getIsDeleted() == 1) {
            throw new RuntimeException("成果奖励不存在");
        }

        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);

        // 删除成果奖励及其附件
        achievementMapper.deleteById(id);
        achievementAttachmentMapper.deleteByAchievementId(id);
    }

    @Override
    @Transactional
    public AchievementDTO payAchievement(String id) {
        // 获取成果奖励
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement == null || achievement.getIsDeleted() == 1) {
            throw new RuntimeException("成果奖励不存在");
        }
        
        // 检查成果状态是否为已通过
        if (!"approved".equals(achievement.getStatus())) {
            throw new RuntimeException("只有已通过审核的成果才能进行支付");
        }
        
        // 获取当前用户（管理员）
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("无法获取当前用户信息");
        }
        
        // 更新成果状态为已支付
        achievementMapper.updatePaymentStatus(id, "paid", currentUser.getId().toString(), "成果奖励已支付");
        
        // 返回最新的成果信息
        return getAchievementById(id);
    }

    /**
     * 将Achievement实体转换为AchievementDTO
     */
    private AchievementDTO convertToDTO(Achievement achievement) {
        if (achievement == null) {
            return null;
        }

        AchievementDTO dto = new AchievementDTO();
        dto.setId(achievement.getId());
        dto.setTitle(achievement.getTitle());
        dto.setType(achievement.getType());
        dto.setLevel(achievement.getLevel());
        dto.setAwardDate(achievement.getAwardDate());
        dto.setAwardAmount(achievement.getAwardAmount());
        dto.setProjectId(achievement.getProjectId());
        dto.setDescription(achievement.getDescription());
        dto.setStatus(achievement.getStatus());
        dto.setAuditTime(achievement.getAuditTime());
        dto.setAuditUserId(achievement.getAuditUserId());
        dto.setAuditComment(achievement.getAuditComment());
        dto.setCreatorId(achievement.getCreatorId());
        dto.setCreateTime(achievement.getCreateTime());
        dto.setUpdateTime(achievement.getUpdateTime());
        dto.setRemarks(achievement.getRemarks());
        
        // 处理作者
        if (achievement.getAuthors() != null && !achievement.getAuthors().isEmpty()) {
            try {
                dto.setAuthors(objectMapper.readValue(achievement.getAuthors(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                logger.error("解析作者数据失败", e);
                dto.setAuthors(new ArrayList<>());
            }
        }
        
        // 获取创建人姓名
        if (achievement.getCreatorId() != null) {
            User creator = userMapper.selectById(Long.parseLong(achievement.getCreatorId()));
            if (creator != null) {
                dto.setCreatorName(creator.getRealName());
            }
        }
        
        // 获取审核人姓名
        if (achievement.getAuditUserId() != null) {
            User auditor = userMapper.selectById(Long.parseLong(achievement.getAuditUserId()));
            if (auditor != null) {
                dto.setAuditUserName(auditor.getRealName());
            }
        }
        
        // 获取项目名称
        if (achievement.getProjectId() != null && !achievement.getProjectId().isEmpty()) {
            try {
                ProjectDTO project = projectMapper.selectById(Long.parseLong(achievement.getProjectId()));
                if (project != null) {
                    dto.setProjectName(project.getName());
                }
            } catch (Exception e) {
                logger.warn("获取项目名称失败: " + e.getMessage());
            }
        }
        
        // 获取附件列表
        List<AchievementAttachment> attachments = achievementAttachmentMapper.selectByAchievementId(achievement.getId());
        if (attachments != null && !attachments.isEmpty()) {
            List<AchievementAttachmentDTO> attachmentDTOs = attachments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDTOs);
        }
        
        return dto;
    }
    
    /**
     * 将AchievementAttachment实体转换为AchievementAttachmentDTO
     */
    private AchievementAttachmentDTO convertToDTO(AchievementAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        
        AchievementAttachmentDTO dto = new AchievementAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setAchievementId(attachment.getAchievementId());
        dto.setName(attachment.getName());
        dto.setPath(attachment.getPath());
        dto.setFileSize(attachment.getFileSize());
        dto.setFileType(attachment.getFileType());
        dto.setUploadTime(attachment.getUploadTime());
        
        return dto;
    }
} 