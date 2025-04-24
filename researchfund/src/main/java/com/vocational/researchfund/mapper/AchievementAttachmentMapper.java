package com.vocational.researchfund.mapper;

import com.vocational.researchfund.entity.AchievementAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 成果奖励附件Mapper接口
 */
@Mapper
public interface AchievementAttachmentMapper {
    
    /**
     * 创建成果奖励附件
     * @param attachment 成果奖励附件
     * @return 影响行数
     */
    @Insert("INSERT INTO achievement_attachment(id, achievement_id, name, path, file_size, file_type, upload_time) " +
            "VALUES(#{id}, #{achievementId}, #{name}, #{path}, #{fileSize}, #{fileType}, NOW())")
    int insert(AchievementAttachment attachment);
    
    /**
     * 批量创建成果奖励附件
     * @param attachments 成果奖励附件列表
     * @param achievementId 成果奖励ID
     * @return 影响行数
     */
    int batchInsert(@Param("attachments") List<AchievementAttachment> attachments, @Param("achievementId") String achievementId);
    
    /**
     * 根据成果奖励ID查询附件列表
     * @param achievementId 成果奖励ID
     * @return 成果奖励附件列表
     */
    @Select("SELECT id, achievement_id, name, path, file_size, file_type, upload_time, create_time, update_time, is_deleted " +
            "FROM achievement_attachment WHERE achievement_id=#{achievementId} AND is_deleted=0")
    List<AchievementAttachment> selectByAchievementId(@Param("achievementId") String achievementId);
    
    /**
     * 根据ID查询成果奖励附件
     * @param id 成果奖励附件ID
     * @return 成果奖励附件
     */
    @Select("SELECT id, achievement_id, name, path, file_size, file_type, upload_time, create_time, update_time, is_deleted " +
            "FROM achievement_attachment WHERE id=#{id} AND is_deleted=0")
    AchievementAttachment selectById(@Param("id") String id);
    
    /**
     * 根据成果奖励ID删除所有附件（逻辑删除）
     * @param achievementId 成果奖励ID
     * @return 影响行数
     */
    @Update("UPDATE achievement_attachment SET is_deleted=1 WHERE achievement_id=#{achievementId}")
    int deleteByAchievementId(@Param("achievementId") String achievementId);
    
    /**
     * 根据ID删除成果奖励附件（逻辑删除）
     * @param id 成果奖励附件ID
     * @return 影响行数
     */
    @Update("UPDATE achievement_attachment SET is_deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") String id);
} 