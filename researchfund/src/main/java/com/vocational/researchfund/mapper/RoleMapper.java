package com.vocational.researchfund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vocational.researchfund.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 角色Mapper接口
 */
@Repository
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色代码查询角色
     * 
     * @param roleCode 角色代码
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    Role findByRoleCode(@Param("roleCode") String roleCode);
} 