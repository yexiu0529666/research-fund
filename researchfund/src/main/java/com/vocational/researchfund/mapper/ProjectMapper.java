package com.vocational.researchfund.mapper;

import com.vocational.researchfund.dto.ProjectDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper {
    
    /**
     * 创建项目
     * @param project 项目DTO
     * @return 影响行数
     */
    @Insert("INSERT INTO project(name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path) " +
            "VALUES(#{name}, #{type}, #{leaderId}, #{leaderName}, #{startDate}, #{endDate}, " +
            "#{budget}, #{usedBudget}, #{status}, #{auditStatus}, #{description}, #{researchContent}, #{expectedResults}, #{filePath})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ProjectDTO project);
    
    /**
     * 更新项目
     * @param project 项目DTO
     * @return 影响行数
     */
    @Update("UPDATE project SET name=#{name}, type=#{type}, leader_name=#{leaderName}, " +
            "start_date=#{startDate}, end_date=#{endDate}, budget=#{budget}, used_budget=#{usedBudget}, " +
            "status=#{status}, audit_status=#{auditStatus}, description=#{description}, " +
            "research_content=#{researchContent}, expected_results=#{expectedResults}, file_path=#{filePath} " +
            "WHERE id=#{id}")
    int update(ProjectDTO project);
    
    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目DTO
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path, " +
            "completion_report_path, completion_audit_status, completion_audit_comment " +
            "FROM project WHERE id=#{id} AND deleted=0")
    ProjectDTO selectById(@Param("id") Long id);
    
    /**
     * 查询项目列表
     * @return 项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, create_time," +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path " +
            "FROM project WHERE deleted=0 ORDER BY id DESC ")
    List<ProjectDTO> selectList();
    
    /**
     * 查询用户的项目列表
     * @param leaderId 用户ID
     * @return 项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path " +
            "FROM project WHERE leader_id=#{leaderId} AND deleted=0")
    List<ProjectDTO> selectByLeaderId(@Param("leaderId") Long leaderId);
    
    /**
     * 根据审核状态查询项目
     * @param auditStatus 审核状态
     * @return 项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path " +
            "FROM project WHERE audit_status=#{auditStatus} AND deleted=0")
    List<ProjectDTO> selectByAuditStatus(@Param("auditStatus") String auditStatus);
    
    /**
     * 根据项目状态查询项目
     * @param status 项目状态
     * @return 项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path " +
            "FROM project WHERE status=#{status} AND deleted=0")
    List<ProjectDTO> selectByStatus(@Param("status") String status);
    
    /**
     * 逻辑删除项目
     * @param id 项目ID
     * @return 影响行数
     */
    @Update("UPDATE project SET deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 更新项目审核状态
     * @param id 项目ID
     * @param auditStatus 审核状态
     * @return 影响行数
     */
    @Update("UPDATE project SET audit_status=#{auditStatus} WHERE id=#{id}")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") String auditStatus);
    
    /**
     * 更新项目已用经费
     * @param id 项目ID
     * @param amount 增加的经费金额
     * @return 影响行数
     */
    @Update("UPDATE project SET used_budget = used_budget + #{amount} WHERE id=#{id}")
    int updateUsedBudget(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);
    
    /**
     * 查询已过期且状态为active的项目（已超过结束日期但未进入结题流程）
     * @return 项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, start_date, end_date, " +
            "budget, used_budget, status, audit_status, description, research_content, expected_results, file_path " +
            "FROM project WHERE status = 'active' AND end_date < CURRENT_DATE() AND deleted = 0")
    List<ProjectDTO> selectExpiredActiveProjects();
    
    /**
     * 更新项目为待结题状态
     * @param id 项目ID
     * @return 影响行数
     */
    @Update("UPDATE project SET status = 'pending_completion' WHERE id = #{id}")
    int updateToPendingCompletion(@Param("id") Long id);
    
    /**
     * 更新项目结题报告信息
     * @param id 项目ID
     * @param reportPath 结题报告路径
     * @return 影响行数
     */
    @Update("UPDATE project SET completion_report_path = #{reportPath}, " +
            "status = 'completion_review', " +
            "completion_audit_status = 'pending', " +
            "completion_report_submit_time = CURRENT_DATE() " +
            "WHERE id = #{id}")
    int updateCompletionReport(@Param("id") Long id, @Param("reportPath") String reportPath);
    
    /**
     * 更新项目结题审核状态
     * @param id 项目ID
     * @param auditStatus 审核状态
     * @param comment 审核意见
     * @return 影响行数
     */
    @Update("UPDATE project SET completion_audit_status = #{auditStatus}, " +
            "completion_audit_comment = #{comment}, " +
            "status = CASE WHEN #{auditStatus} = 'approved' THEN 'archived' " +
            "WHEN #{auditStatus} = 'rejected' THEN 'pending_completion' ELSE status END " +
            "WHERE id = #{id}")
    int updateCompletionAuditStatus(@Param("id") Long id, 
                                    @Param("auditStatus") String auditStatus, 
                                    @Param("comment") String comment);
    
    /**
     * 获取待审核项目
     * @return 待审核项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, status, audit_status " +
            "FROM project WHERE audit_status = 'pending' AND deleted = 0")
    List<Map<String, Object>> getPendingAuditProjects();
    
    /**
     * 获取用户负责的待结题项目
     * @param userId 用户ID
     * @return 待结题项目列表
     */
    @Select("SELECT id, name, type, leader_id, leader_name, status " +
            "FROM project WHERE leader_id = #{userId} AND status = 'pending_completion' AND deleted = 0")
    List<Map<String, Object>> getUserPendingCompletionProjects(@Param("userId") Long userId);
    
    /**
     * 获取所有活跃项目
     * @return 活跃项目列表
     */
    @Select("SELECT id, name, status, budget, used_budget " +
            "FROM project WHERE status IN ('active', 'planning') AND deleted = 0 " +
            "ORDER BY used_budget DESC")
    List<Map<String, Object>> getActiveProjects();
    
    /**
     * 获取用户参与的活跃项目
     * @param userId 用户ID
     * @return 用户参与的活跃项目列表
     */
    @Select("SELECT id, name, status, budget, used_budget " +
            "FROM project " +
            "WHERE leader_id = #{userId} " +
            "AND status IN ('active', 'planning') AND deleted = 0 " +
            "ORDER BY used_budget DESC")
    List<Map<String, Object>> getUserActiveProjects(@Param("userId") Long userId);
    
    /**
     * 获取项目总数
     * @return 项目总数
     */
    @Select("SELECT COUNT(*) FROM project WHERE deleted = 0")
    int getProjectCount();
    
    /**
     * 获取用户参与的项目总数
     * @param userId 用户ID
     * @return 用户参与的项目总数
     */
    @Select("SELECT COUNT(*) " +
            "FROM project " +
            "WHERE leader_id = #{userId} " +
            "AND deleted = 0")
    int getUserProjectCount(@Param("userId") Long userId);
    
    /**
     * 获取全局预算使用情况
     * @return 全局预算使用情况
     */
    @Select("SELECT COALESCE(SUM(budget), 0) as total_budget, " +
            "COALESCE(SUM(used_budget), 0) as total_used_budget " +
            "FROM project WHERE deleted = 0")
    Map<String, Object> getTotalBudgetUsage();
    
    /**
     * 获取用户相关项目的预算使用情况
     * @param userId 用户ID
     * @return 用户相关项目的预算使用情况
     */
    @Select("SELECT COALESCE(SUM(budget), 0) as total_budget, " +
            "COALESCE(SUM(used_budget), 0) as total_used_budget " +
            "FROM project " +
            "WHERE leader_id = #{userId} " +
            "AND deleted = 0")
    Map<String, Object> getUserBudgetUsage(@Param("userId") Long userId);
    
    /**
     * 获取项目类型统计
     * @return 项目类型统计
     */
    @Select("SELECT type, COUNT(*) as count " +
            "FROM project WHERE deleted = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> getProjectTypeStats();
    
    /**
     * 获取用户参与的项目类型统计
     * @param userId 用户ID
     * @return 用户参与的项目类型统计
     */
    @Select("SELECT type, COUNT(*) as count " +
            "FROM project " +
            "WHERE leader_id = #{userId} " +
            "AND deleted = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> getUserProjectTypeStats(@Param("userId") Long userId);
} 