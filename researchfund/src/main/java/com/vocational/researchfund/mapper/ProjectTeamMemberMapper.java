package com.vocational.researchfund.mapper;

import com.vocational.researchfund.dto.ProjectDTO.TeamMemberDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目团队成员Mapper接口
 */
@Mapper
public interface ProjectTeamMemberMapper {
    
    /**
     * 创建项目团队成员
     * @param member 成员DTO
     * @param projectId 项目ID
     * @return 影响行数
     */
    @Insert("INSERT INTO project_team_member(project_id, name, role) " +
            "VALUES(#{projectId}, #{member.name}, #{member.role})")
    @Options(useGeneratedKeys = true, keyProperty = "member.id", keyColumn = "id")
    int insert(@Param("member") TeamMemberDTO member, @Param("projectId") Long projectId);
    
    /**
     * 批量插入项目团队成员
     * @param members 成员列表
     * @param projectId 项目ID
     * @return 影响行数
     */
    default int batchInsert(List<TeamMemberDTO> members, Long projectId) {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (TeamMemberDTO member : members) {
            count += insert(member, projectId);
        }
        return count;
    }
    
    /**
     * 根据项目ID查询团队成员
     * @param projectId 项目ID
     * @return 团队成员列表
     */
    @Select("SELECT id, name, role FROM project_team_member WHERE project_id=#{projectId} AND deleted=0")
    List<TeamMemberDTO> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 删除项目团队成员
     * @param id 成员ID
     * @return 影响行数
     */
    @Update("UPDATE project_team_member SET deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 删除项目的所有团队成员(逻辑删除)
     * @param projectId 项目ID
     * @return 影响行数
     */
    @Update("UPDATE project_team_member SET deleted=1 WHERE project_id=#{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 更新项目团队成员
     * @param member 成员DTO
     * @return 影响行数
     */
    @Update("UPDATE project_team_member SET name=#{name}, role=#{role} WHERE id=#{id}")
    int update(TeamMemberDTO member);
} 