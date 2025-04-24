package com.vocational.researchfund.mapper;

import com.vocational.researchfund.dto.ExpenseDTO;
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
 * 经费申请Mapper接口
 */
@Mapper
public interface ExpenseMapper {
    
    /**
     * 创建经费申请
     * @param expense 经费申请DTO
     * @return 影响行数
     */
    @Insert("INSERT INTO expense_apply(title, category, project_id, project_name, type, amount, apply_date, " +
            "purpose, reason, apply_user_id, apply_user_name, status) " +
            "VALUES(#{title}, #{category}, #{projectId}, #{projectName}, #{type}, #{amount}, #{applyDate}, " +
            "#{purpose}, #{reason}, #{applyUserId}, #{applyUserName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ExpenseDTO expense);
    
    /**
     * 更新经费申请
     * @param expense 经费申请DTO
     * @return 影响行数
     */
    @Update("UPDATE expense_apply SET title=#{title}, category=#{category}, project_id=#{projectId}, project_name=#{projectName}, " +
            "type=#{type}, amount=#{amount}, apply_date=#{applyDate}, purpose=#{purpose}, reason=#{reason} " +
            "WHERE id=#{id}")
    int update(ExpenseDTO expense);
    
    /**
     * 更新经费申请状态
     * @param id 经费申请ID
     * @param status 状态
     * @param auditUserId 审核人ID（可为null）
     * @param auditUserName 审核人姓名（可为null）
     * @param auditComment 审核意见（可为null）
     * @return 影响行数
     */
    @Update({
        "<script>",
        "UPDATE expense_apply SET status=#{status} ",
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
     * 根据ID查询经费申请
     * @param id 经费申请ID
     * @return 经费申请DTO
     */
    @Select("SELECT id, title, category, project_id, project_name, type, amount, apply_date, purpose, reason, " +
            "apply_user_id, apply_user_name, status, audit_user_id, audit_user_name, audit_time, audit_comment, " +
            "create_time " +
            "FROM expense_apply WHERE id=#{id} AND deleted=0")
    ExpenseDTO selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询经费申请列表
     * @param applyUserId 申请人ID
     * @return 经费申请列表
     */
    @Select("SELECT id, title, category, project_id, project_name, type, amount, apply_date, purpose, reason, " +
            "apply_user_id, apply_user_name, status, audit_user_id, audit_user_name, audit_time, audit_comment, " +
            "create_time " +
            "FROM expense_apply WHERE apply_user_id=#{applyUserId} AND deleted=0 ORDER BY create_time DESC")
    List<ExpenseDTO> selectByUserId(@Param("applyUserId") Long applyUserId);
    
    /**
     * 查询所有经费申请列表
     * @return 经费申请列表
     */
    @Select("SELECT id, title, category, project_id, project_name, type, amount, apply_date, purpose, reason, " +
            "apply_user_id, apply_user_name, status, audit_user_id, audit_user_name, audit_time, audit_comment, " +
            "create_time " +
            "FROM expense_apply WHERE deleted=0 ORDER BY create_time DESC")
    List<ExpenseDTO> selectAll();
    
    /**
     * 逻辑删除经费申请
     * @param id 经费申请ID
     * @return 影响行数
     */
    @Update("UPDATE expense_apply SET deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据项目ID查询经费申请列表
     * @param projectId 项目ID
     * @return 经费申请列表
     */
    @Select("SELECT id, title, category, project_id, project_name, type, amount, apply_date, purpose, reason, " +
            "apply_user_id, apply_user_name, status, audit_user_id, audit_user_name, audit_time, audit_comment, " +
            "create_time " +
            "FROM expense_apply WHERE project_id=#{projectId} AND deleted=0 ORDER BY create_time DESC")
    List<ExpenseDTO> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询所有已支付的借款类型经费申请
     * @return 符合条件的经费申请列表
     */
    List<ExpenseDTO> selectPaidAdvanceExpenses();

    /**
     * 查询所有待提交报销凭证的经费申请
     * @return 待提交报销凭证的经费申请列表
     */
    List<ExpenseDTO> selectReceiptPendingExpenses();

    /**
     * 获取项目经费使用统计，按类型统计已审批和已支付的经费申请
     * @param projectId 项目ID
     * @return 经费使用统计列表，包含type和amount字段
     */
    @Select("SELECT type, SUM(amount) as amount FROM expense_apply " +
            "WHERE project_id=#{projectId} AND (status='repayment_pending' OR status='paid' OR status='receipt_pending' OR " +
            "status='receipt_audit' OR status='completed' OR status = 'repaid') AND deleted=0 " +
            "GROUP BY type ORDER BY amount DESC")
    List<Map<String, Object>> getExpenseStatsByProject(@Param("projectId") Long projectId);

    /**
     * 获取待审核的经费申请
     * @return 待审核的经费申请列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, status " +
            "FROM expense WHERE status = 'pending' AND deleted = 0")
    List<Map<String, Object>> getPendingAuditExpenses();
    
    /**
     * 获取用户的待审核经费申请
     * @param userId 用户ID
     * @return 用户的待审核经费申请列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, status " +
            "FROM expense WHERE apply_user_id = #{userId} AND status = 'pending' AND deleted = 0")
    List<Map<String, Object>> getUserPendingExpenses(@Param("userId") Long userId);
    
    /**
     * 获取用户的待提交报销凭证的经费申请
     * @param userId 用户ID
     * @return 用户的待提交报销凭证的经费申请列表
     */
    @Select("SELECT id, title, project_id, project_name, amount, apply_date, status " +
            "FROM expense WHERE apply_user_id = #{userId} AND status = 'receipt_pending' AND deleted = 0")
    List<Map<String, Object>> getUserReceiptPendingExpenses(@Param("userId") Long userId);
    
    /**
     * 获取全局待审核经费申请数量
     * @return 待审核经费申请数量
     */
    @Select("SELECT COUNT(*) FROM expense WHERE status = 'pending' AND deleted = 0")
    int getPendingExpenseCount();
    
    /**
     * 获取全局待支付经费申请数量
     * @return 待支付经费申请数量
     */
    @Select("SELECT COUNT(*) FROM expense WHERE status = 'approved' AND deleted = 0")
    int getPendingPaymentCount();
    
    /**
     * 获取用户的待审核经费申请数量
     * @param userId 用户ID
     * @return 用户的待审核经费申请数量
     */
    @Select("SELECT COUNT(*) FROM expense WHERE apply_user_id = #{userId} AND status = 'pending' AND deleted = 0")
    int getUserPendingExpenseCount(@Param("userId") Long userId);
    
    /**
     * 获取用户的待支付经费申请数量
     * @param userId 用户ID
     * @return 用户的待支付经费申请数量
     */
    @Select("SELECT COUNT(*) FROM expense WHERE apply_user_id = #{userId} AND status = 'approved' AND deleted = 0")
    int getUserPendingPaymentCount(@Param("userId") Long userId);
} 