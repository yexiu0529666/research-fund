package com.vocational.researchfund.mapper;

import com.vocational.researchfund.dto.AttachmentDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 附件Mapper接口
 */
@Mapper
public interface AttachmentMapper {
    
    /**
     * 创建附件
     * @param attachment 附件DTO
     * @return 影响行数
     */
    @Insert("INSERT INTO expense_attachment(expense_id, name, url, file_size, file_type) " +
            "VALUES(#{expenseId}, #{name}, #{url}, #{fileSize}, #{fileType})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(AttachmentDTO attachment);
    
    /**
     * 批量创建附件
     * @param attachments 附件DTO列表
     * @param expenseId 经费申请ID
     * @return 影响行数
     */
    int batchInsert(@Param("attachments") List<AttachmentDTO> attachments, @Param("expenseId") Long expenseId);
    
    /**
     * 根据经费申请ID查询附件列表
     * @param expenseId 经费申请ID
     * @return 附件DTO列表
     */
    @Select("SELECT id, expense_id, name, url, file_size, file_type " +
            "FROM expense_attachment WHERE expense_id=#{expenseId} AND deleted=0")
    List<AttachmentDTO> selectByExpenseId(@Param("expenseId") Long expenseId);
    
    /**
     * 根据ID查询附件
     * @param id 附件ID
     * @return 附件DTO
     */
    @Select("SELECT id, expense_id, name, url, file_size, file_type " +
            "FROM expense_attachment WHERE id=#{id} AND deleted=0")
    AttachmentDTO selectById(@Param("id") Long id);
    
    /**
     * 根据经费申请ID删除所有附件（逻辑删除）
     * @param expenseId 经费申请ID
     * @return 影响行数
     */
    @Update("UPDATE expense_attachment SET deleted=1 WHERE expense_id=#{expenseId}")
    int deleteByExpenseId(@Param("expenseId") Long expenseId);
    
    /**
     * 根据ID删除附件（逻辑删除）
     * @param id 附件ID
     * @return 影响行数
     */
    @Update("UPDATE expense_attachment SET deleted=1 WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
} 