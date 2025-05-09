package com.vocational.researchfund.mapper;

import com.vocational.researchfund.entity.ProjectFundArrival;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 项目经费到账Mapper接口
 */
@Mapper
public interface ProjectFundArrivalMapper {
    
    /**
     * 创建项目经费到账记录
     * @param fundArrival 经费到账记录
     * @return 影响行数
     */
    @Insert("INSERT INTO project_fund_arrival(project_id, project_name, funding_source, amount, " +
            "arrival_date, voucher_path, remark, create_time, update_time) " +
            "VALUES(#{projectId}, #{projectName}, #{fundingSource}, #{amount}, " +
            "#{arrivalDate}, #{voucherPath}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ProjectFundArrival fundArrival);
    
    /**
     * 更新项目经费到账记录
     * @param fundArrival 经费到账记录
     * @return 影响行数
     */
    @Update("UPDATE project_fund_arrival SET project_name=#{projectName}, funding_source=#{fundingSource}, " +
            "amount=#{amount}, arrival_date=#{arrivalDate}, voucher_path=#{voucherPath}, " +
            "remark=#{remark}, update_time=NOW() " +
            "WHERE id=#{id}")
    int update(ProjectFundArrival fundArrival);
    
    /**
     * 根据ID查询项目经费到账记录
     * @param id 记录ID
     * @return 经费到账记录
     */
    @Select("SELECT id, project_id, project_name, funding_source, amount, " +
            "arrival_date, voucher_path, remark, create_time, update_time " +
            "FROM project_fund_arrival WHERE id=#{id} AND deleted=0")
    ProjectFundArrival selectById(@Param("id") Long id);
    
    /**
     * 查询项目经费到账记录列表
     * @return 经费到账记录列表
     */
    @Select("SELECT id, project_id, project_name, funding_source, amount, " +
            "arrival_date, voucher_path, remark, create_time, update_time " +
            "FROM project_fund_arrival WHERE deleted=0 ORDER BY arrival_date DESC")
    List<ProjectFundArrival> selectList();
    
    /**
     * 根据项目ID查询经费到账记录列表
     * @param projectId 项目ID
     * @return 经费到账记录列表
     */
    @Select("SELECT id, project_id, project_name, funding_source, amount, " +
            "arrival_date, voucher_path, remark, create_time, update_time " +
            "FROM project_fund_arrival WHERE project_id=#{projectId} AND deleted=0 " +
            "ORDER BY arrival_date DESC")
    List<ProjectFundArrival> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 逻辑删除经费到账记录
     * @param id 记录ID
     * @return 影响行数
     */
    @Update("UPDATE project_fund_arrival SET deleted=1, update_time=NOW() WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
    
    /**
     * 获取项目已到账的总金额
     * @param projectId 项目ID
     * @return 已到账总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) as total_amount " +
            "FROM project_fund_arrival WHERE project_id=#{projectId} AND deleted=0")
    BigDecimal getTotalArrivedAmount(@Param("projectId") Long projectId);
    
    /**
     * 获取所有项目的到账统计
     * @return 项目到账统计信息列表
     */
    @Select("SELECT project_id, project_name, SUM(amount) as total_amount " +
            "FROM project_fund_arrival WHERE deleted=0 " +
            "GROUP BY project_id, project_name")
    List<Map<String, Object>> getProjectFundArrivalStats();
    
    /**
     * 查询项目各经费来源的到账金额统计
     * @param projectId 项目ID
     * @return 各经费来源的到账金额统计
     */
    @Select("SELECT funding_source, SUM(amount) as total_amount " +
            "FROM project_fund_arrival WHERE project_id=#{projectId} AND deleted=0 " +
            "GROUP BY funding_source")
    List<Map<String, Object>> getFundArrivalBySource(@Param("projectId") Long projectId);
} 