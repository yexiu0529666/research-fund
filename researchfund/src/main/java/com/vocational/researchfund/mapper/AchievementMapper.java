package com.vocational.researchfund.mapper;

import com.vocational.researchfund.entity.Achievement;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 成果奖励Mapper接口
 */
@Mapper
public interface AchievementMapper {
    
    /**
     * 创建成果奖励
     * @param achievement 成果奖励实体
     * @return 影响行数
     */
    @Insert("INSERT INTO achievement(id, title, type, level, authors, award_date, award_amount, project_id, description, " +
            "status, creator_id, create_time, update_time, remarks, is_deleted) " +
            "VALUES(#{id}, #{title}, #{type}, #{level}, #{authors}, #{awardDate}, #{awardAmount}, #{projectId}, #{description}, " +
            "#{status}, #{creatorId}, NOW(), NOW(), #{remarks}, 0)")
    int insert(Achievement achievement);
    
    /**
     * 更新成果奖励
     * @param achievement 成果奖励实体
     * @return 影响行数
     */
    @Update("UPDATE achievement SET title=#{title}, type=#{type}, level=#{level}, authors=#{authors}, " +
            "award_date=#{awardDate}, award_amount=#{awardAmount}, project_id=#{projectId}, description=#{description}, " +
            "update_time=NOW(), remarks=#{remarks} WHERE id=#{id} AND is_deleted=0")
    int update(Achievement achievement);
    
    /**
     * 更新成果奖励状态
     * @param id 成果奖励ID
     * @param status 状态
     * @param auditUserId 审核人ID
     * @param auditComment 审核意见
     * @return 影响行数
     */
    @Update("UPDATE achievement SET status=#{status}, audit_user_id=#{auditUserId}, audit_comment=#{auditComment}, " +
            "audit_time=NOW(), update_time=NOW() , award_amount=#{awardAmount} WHERE id=#{id} AND is_deleted=0")
    int updateStatus(@Param("id") String id, @Param("status") String status, 
                     @Param("auditUserId") String auditUserId, @Param("auditComment") String auditComment,
                     @Param("awardAmount") Double awardAmount);
    
    /**
     * 更新成果支付状态
     * @param id 成果奖励ID
     * @param status 状态
     * @param auditUserId 支付人ID
     * @param auditComment 支付备注
     * @return 影响行数
     */
    @Update("UPDATE achievement SET status=#{status}, audit_user_id=#{auditUserId}, audit_comment=#{auditComment}, " +
            "update_time=NOW() WHERE id=#{id} AND is_deleted=0")
    int updatePaymentStatus(@Param("id") String id, @Param("status") String status, 
                     @Param("auditUserId") String auditUserId, @Param("auditComment") String auditComment);
    
    /**
     * 根据ID查询成果奖励
     * @param id 成果奖励ID
     * @return 成果奖励实体
     */
    @Select("SELECT a.* FROM achievement a WHERE a.id=#{id} AND a.is_deleted=0")
    Achievement selectById(@Param("id") String id);
    
    /**
     * 根据创建者ID查询成果奖励列表
     * @param creatorId 创建者ID
     * @param params 查询参数
     * @return 成果奖励实体列表
     */
    List<Achievement> selectByCreatorId(@Param("creatorId") String creatorId, @Param("params") Map<String, Object> params);
    
    /**
     * 根据条件查询成果奖励列表
     * @param params 查询参数
     * @return 成果奖励实体列表
     */
    List<Achievement> selectByParams(@Param("params") Map<String, Object> params);
    
    /**
     * 根据创建者ID查询成果奖励数量
     * @param creatorId 创建者ID
     * @param params 查询参数
     * @return 成果奖励数量
     */
    int countByCreatorId(@Param("creatorId") String creatorId, @Param("params") Map<String, Object> params);
    
    /**
     * 根据条件查询成果奖励数量
     * @param params 查询参数
     * @return 成果奖励数量
     */
    int countByParams(@Param("params") Map<String, Object> params);
    
    /**
     * 逻辑删除成果奖励
     * @param id 成果奖励ID
     * @return 影响行数
     */
    @Update("UPDATE achievement SET is_deleted=1, update_time=NOW() WHERE id=#{id}")
    int deleteById(@Param("id") String id);
    
    /**
     * 按类型统计成果数量
     * @param params 查询参数
     * @return 各类型成果数量
     */
    List<Map<String, Object>> countByType(@Param("params") Map<String, Object> params);
    
    /**
     * 按类型统计特定用户的成果数量
     * @param creatorId 创建者ID
     * @param params 查询参数
     * @return 各类型成果数量
     */
    List<Map<String, Object>> countByTypeAndCreator(@Param("creatorId") String creatorId, @Param("params") Map<String, Object> params);
} 