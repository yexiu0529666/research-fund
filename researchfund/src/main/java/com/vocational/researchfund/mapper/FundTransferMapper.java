package com.vocational.researchfund.mapper;

import com.vocational.researchfund.dto.FundTransferDTO;
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
 * 经费结转Mapper接口
 */
@Mapper
public interface FundTransferMapper {
    
    /**
     * 创建经费结转
     * @param transfer 经费结转DTO
     * @return 影响行数
     */
    @Insert("INSERT INTO fund_transfer(title, project_id, project_name, amount, apply_date, " +
            "reason, description, from_year, to_year, apply_user_id, apply_user_name, status) " +
            "VALUES(#{title}, #{projectId}, #{projectName}, #{amount}, #{applyDate}, " +
            "#{reason}, #{description}, #{fromYear}, #{toYear}, #{applyUserId}, #{applyUserName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(FundTransferDTO transfer);
    
    /**
     * 更新经费结转
     * @param transfer 经费结转DTO
     * @return 影响行数
     */
    @Update("UPDATE fund_transfer SET title=#{title}, project_id=#{projectId}, project_name=#{projectName}, " +
            "amount=#{amount}, apply_date=#{applyDate}, reason=#{reason}, description=#{description}, " +
            "from_year=#{fromYear}, to_year=#{toYear} " +
            "WHERE id=#{id}")
    int update(FundTransferDTO transfer);
    
    /**
     * 更新经费结转状态
     * @param id 经费结转ID
     * @param status 状态
     * @param auditUserId 审核人ID（可为null）
     * @param auditUserName 审核人姓名（可为null）
     * @param auditComment 审核意见（可为null）
     * @return 影响行数
     */
    @Update({
        "<script>",
        "UPDATE fund_transfer SET status=#{status} ",
        "<if test='auditUserId != null'>",
        ", audit_user_id=#{auditUserId} ",
        "</if>",
        "<if test='auditUserName != null'>",
        ", audit_user_name=#{auditUserName} ",
        "</if>",
        "<if test='auditComment != null'>",
        ", audit_time=NOW(), audit_comment=#{auditComment} ",
        "</if>",
        "WHERE id=#{id}",
        "</script>"
    })
    int updateStatus(@Param("id") Long id, @Param("status") String status, 
                     @Param("auditUserId") Long auditUserId, 
                     @Param("auditUserName") String auditUserName, 
                     @Param("auditComment") String auditComment);
    
    /**
     * 根据ID查询经费结转
     * @param id 经费结转ID
     * @return 经费结转DTO
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, reason, description, " +
            "from_year, to_year, apply_user_id, apply_user_name, status, audit_user_id, " +
            "audit_user_name, audit_time, audit_comment, create_time " +
            "FROM fund_transfer WHERE id=#{id} AND deleted=0")
    FundTransferDTO selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询经费结转列表
     * @param applyUserId 申请人ID
     * @return 经费结转列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, reason, description, " +
            "from_year, to_year, apply_user_id, apply_user_name, status, audit_user_id, " +
            "audit_user_name, audit_time, audit_comment, create_time " +
            "FROM fund_transfer WHERE apply_user_id=#{applyUserId} AND deleted=0 ORDER BY create_time DESC")
    List<FundTransferDTO> selectByUserId(@Param("applyUserId") Long applyUserId);
    
    /**
     * 查询所有经费结转列表
     * @return 经费结转列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, reason, description, " +
            "from_year, to_year, apply_user_id, apply_user_name, status, audit_user_id, " +
            "audit_user_name, audit_time, audit_comment, create_time " +
            "FROM fund_transfer WHERE deleted=0 ORDER BY create_time DESC")
    List<FundTransferDTO> selectAll();
    
    /**
     * 逻辑删除经费结转
     * @param id 经费结转ID
     * @return 影响行数
     */
    @Update("UPDATE fund_transfer SET deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据项目ID查询经费结转列表
     * @param projectId 项目ID
     * @return 经费结转列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, reason, description, " +
            "from_year, to_year, apply_user_id, apply_user_name, status, audit_user_id, " +
            "audit_user_name, audit_time, audit_comment, create_time " +
            "FROM fund_transfer WHERE project_id=#{projectId} AND deleted=0 ORDER BY create_time DESC")
    List<FundTransferDTO> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 获取待审核的经费结转列表
     * @return 待审核的经费结转列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, status " +
            "FROM fund_transfer WHERE status = 'pending' AND deleted = 0")
    List<Map<String, Object>> getPendingAuditTransfers();
} 