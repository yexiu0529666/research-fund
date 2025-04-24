package com.vocational.researchfund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vocational.researchfund.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 分页查询角色列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param role 查询条件
     * @return 角色列表
     */
    IPage<Role> listRolePage(Integer page, Integer size, Role role);
    
    /**
     * 获取角色详情
     * 
     * @param roleId 角色ID
     * @return 角色详情
     */
    Role getRoleDetail(Long roleId);
    
    /**
     * 根据角色代码查询角色
     * 
     * @param roleCode 角色代码
     * @return 角色信息
     */
    Role getRoleByCode(String roleCode);
    
    /**
     * 添加角色
     * 
     * @param role 角色数据
     * @return 是否成功
     */
    boolean addRole(Role role);
    
    /**
     * 更新角色
     * 
     * @param role 角色数据
     * @return 是否成功
     */
    boolean updateRole(Role role);
    
    /**
     * 删除角色
     * 
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);
    
    /**
     * 获取所有角色列表
     * 
     * @return 角色列表
     */
    List<Role> getAllRoles();
} 