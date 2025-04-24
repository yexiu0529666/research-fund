package com.vocational.researchfund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocational.researchfund.entity.Role;
import com.vocational.researchfund.mapper.RoleMapper;
import com.vocational.researchfund.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Override
    public IPage<Role> listRolePage(Integer page, Integer size, Role role) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (role != null) {
            if (StringUtils.hasText(role.getRoleName())) {
                queryWrapper.like(Role::getRoleName, role.getRoleName());
            }
            if (StringUtils.hasText(role.getRoleCode())) {
                queryWrapper.like(Role::getRoleCode, role.getRoleCode());
            }
            if (role.getStatus() != null) {
                queryWrapper.eq(Role::getStatus, role.getStatus());
            }
        }
        
        Page<Role> pageParam = new Page<>(page, size);
        return page(pageParam, queryWrapper);
    }
    
    @Override
    public Role getRoleDetail(Long roleId) {
        return getById(roleId);
    }
    
    @Override
    public Role getRoleByCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, roleCode);
        return getOne(queryWrapper);
    }
    
    @Override
    @Transactional
    public boolean addRole(Role role) {
        // 检查角色代码是否已存在
        Role existRole = getRoleByCode(role.getRoleCode());
        if (existRole != null) {
            throw new RuntimeException("角色代码已存在");
        }
        
        return save(role);
    }
    
    @Override
    @Transactional
    public boolean updateRole(Role role) {
        if (role.getId() == null) {
            throw new RuntimeException("角色ID不能为空");
        }
        
        // 检查角色代码是否已存在（排除自身）
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, role.getRoleCode())
                   .ne(Role::getId, role.getId());
        
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("角色代码已存在");
        }
        
        return updateById(role);
    }
    
    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        return removeById(roleId);
    }
    
    @Override
    public List<Role> getAllRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, 0)  // 只查询状态正常的角色
                   .orderByAsc(Role::getSort); // 按排序字段升序
        
        return list(queryWrapper);
    }
} 