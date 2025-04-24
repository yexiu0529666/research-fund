package com.vocational.researchfund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vocational.researchfund.dto.UserDTO;
import com.vocational.researchfund.entity.Department;
import com.vocational.researchfund.entity.Role;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.UserMapper;
import com.vocational.researchfund.service.DepartmentService;
import com.vocational.researchfund.service.RoleService;
import com.vocational.researchfund.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private RoleService roleService;

    @Override
    public IPage<User> listUserPage(Integer page, Integer size, User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (user != null) {
            if (StringUtils.hasText(user.getUsername())) {
                queryWrapper.like(User::getUsername, user.getUsername());
            }
            if (StringUtils.hasText(user.getRealName())) {
                queryWrapper.like(User::getRealName, user.getRealName());
            }
            if (user.getDepartmentId() != null) {
                queryWrapper.eq(User::getDepartmentId, user.getDepartmentId());
            }
        }
        
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> result = page(pageParam, queryWrapper);
        
        // 填充部门名称
        fillDepartmentName(result.getRecords());
        
        // 填充角色信息
        fillUserRoles(result.getRecords());
        
        return result;
    }

    /**
     * 填充用户的部门名称
     * @param users 用户列表
     */
    private void fillDepartmentName(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        
        for (User user : users) {
            if (user.getDepartmentId() != null) {
                Department department = departmentService.getById(user.getDepartmentId());
                if (department != null) {
                    user.setDepartmentName(department.getName());
                }
            }
        }
    }
    
    /**
     * 填充用户的角色信息
     * @param users 用户列表
     */
    private void fillUserRoles(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        
        // 获取所有角色
        List<Role> allRoles = roleService.getAllRoles();
        Map<String, Role> roleCodeMap = allRoles.stream()
                .collect(Collectors.toMap(Role::getRoleCode, role -> role));
        
        for (User user : users) {
            List<String> roleCodes = listUserRoles(user.getId());
            List<Map<String, Object>> roles = new ArrayList<>();
            List<Long> roleIds = new ArrayList<>();
            
            // 只取第一个角色（假设每个用户只有一个角色）
            if (!roleCodes.isEmpty()) {
                String roleCode = roleCodes.get(0);
                Role role = roleCodeMap.get(roleCode);
                if (role != null) {
                    Map<String, Object> roleMap = new java.util.HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getRoleName());
                    roles.add(roleMap);
                    roleIds.add(role.getId());
                }
            }
            
            user.setRoles(roles);
            user.setRoleIds(roleIds);
        }
    }

    @Override
    public User getUserDetail(Long userId) {
        User user = getById(userId);
        if (user != null) {
            // 填充部门名称
            if (user.getDepartmentId() != null) {
                Department department = departmentService.getById(user.getDepartmentId());
                if (department != null) {
                    user.setDepartmentName(department.getName());
                }
            }
            
            // 填充角色信息
            List<User> users = new ArrayList<>();
            users.add(user);
            fillUserRoles(users);
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User getUserByUsernameAndPassword(String username, String password) {
        // 先查询用户是否存在
        User user = getUserByUsername(username);
        if (user == null) {
            logger.warn("用户不存在: {}", username);
            return null;
        }
        
        // 直接比较明文密码
        if (password.equals(user.getPassword())) {
            logger.info("密码验证成功: {}", username);
            return user;
        }
        
        logger.warn("密码验证失败: {}", username);
        return null;
    }

    @Override
    @Transactional
    public boolean addUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (getUserByUsername(userDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 不再加密密码，直接使用明文
        // user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPassword(userDTO.getPassword());
        
        boolean result = save(user);
        
        // 添加用户角色关联
        if (result && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            try {
                // 获取插入后的用户ID
                Long userId = user.getId();
                
                logger.info("准备保存用户角色关联: userId={}, roleIds={}", userId, userDTO.getRoleIds());
                
                // 添加用户角色关联
                for (Long roleId : userDTO.getRoleIds()) {
                    int rows = userMapper.insertUserRole(userId, roleId);
                    logger.info("插入用户角色关联: userId={}, roleId={}, 影响行数={}", userId, roleId, rows);
                }
                
                // 验证角色是否成功保存
                List<String> savedRoles = userMapper.selectRolesByUserId(userId);
                logger.info("保存后查询到的用户角色: userId={}, roles={}", userId, savedRoles);
                
                logger.info("用户角色保存成功: userId={}, roleIds={}", userId, userDTO.getRoleIds());
            } catch (Exception e) {
                logger.error("保存用户角色失败: {}", e.getMessage(), e);
                throw new RuntimeException("保存用户角色失败: " + e.getMessage());
            }
        } else {
            logger.warn("未设置用户角色或保存用户失败: userId={}, result={}, roleIds={}", 
                     user.getId(), result, userDTO.getRoleIds());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        // 更新用户基本信息
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        boolean result = updateById(user);
        
        // 更新用户角色关联
        if (result && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            try {
                // 1. 删除当前用户的所有角色关联
                userMapper.deleteUserRoles(userDTO.getId());
                
                // 2. 添加新的角色关联
                for (Long roleId : userDTO.getRoleIds()) {
                    userMapper.insertUserRole(userDTO.getId(), roleId);
                }
                
                logger.info("用户角色更新成功: userId={}, roleIds={}", userDTO.getId(), userDTO.getRoleIds());
            } catch (Exception e) {
                logger.error("更新用户角色失败: {}", e.getMessage(), e);
                throw new RuntimeException("更新用户角色失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        try {
            // 首先删除用户角色关联
            userMapper.deleteUserRoles(userId);
            logger.info("删除用户角色关联成功: userId={}", userId);
            
            // 执行物理删除用户
            int result = userMapper.deleteUserPhysically(userId);
            if (result > 0) {
                logger.info("物理删除用户成功: userId={}", userId);
                return true;
            } else {
                logger.warn("物理删除用户失败: userId={}", userId);
                return false;
            }
        } catch (Exception e) {
            logger.error("删除用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }

    @Override
    public List<User> listUsersByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return userMapper.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional
    public boolean resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不再加密密码，直接使用明文
        // user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassword(newPassword);
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码 - 直接比较明文
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        
        // 更新密码 - 不再加密
        user.setPassword(newPassword);
        return updateById(user);
    }

    @Override
    public List<String> listUserRoles(Long userId) {
        try {
            if (userId == null) {
                return java.util.Collections.emptyList();
            }
            List<String> roles = userMapper.selectRolesByUserId(userId);
            System.out.println("获取用户角色成功: userId=" + userId + ", roles=" + roles);
            return roles;
        } catch (Exception e) {
            System.err.println("获取用户角色失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空列表而不是抛出异常
            return java.util.Collections.emptyList();
        }
    }

} 