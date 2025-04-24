package com.vocational.researchfund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vocational.researchfund.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Repository
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据用户名和密码查询用户（用于直接登录验证）
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND password = #{password} AND deleted = 0")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(@Param("id") Long id);
    
    @Select("SELECT * FROM sys_user")
    List<User> findAll();
    
    @Select("SELECT * FROM sys_user WHERE department_id = #{departmentId}")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * 查询用户的角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 0 AND r.deleted = 0")
    List<String> selectRolesByUserId(Long userId);
    
    /**
     * 查询部门下的用户列表
     * 
     * @param departmentId 部门ID
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE department_id = #{departmentId} AND status = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<User> selectUsersByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * 删除用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
    
    /**
     * 新增用户角色关联
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 物理删除用户
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM sys_user WHERE id = #{userId}")
    int deleteUserPhysically(@Param("userId") Long userId);
} 