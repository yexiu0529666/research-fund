package com.vocational.researchfund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vocational.researchfund.dto.UserDTO;
import com.vocational.researchfund.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param user 查询条件
     * @return 用户列表
     */
    IPage<User> listUserPage(Integer page, Integer size, User user);
    
    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    User getUserDetail(Long userId);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
    
    /**
     * 根据用户名和密码查询用户（直接登录验证）
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User getUserByUsernameAndPassword(String username, String password);
    
    /**
     * 添加用户
     * 
     * @param userDTO 用户数据
     * @return 是否成功
     */
    boolean addUser(UserDTO userDTO);
    
    /**
     * 更新用户
     * 
     * @param userDTO 用户数据
     * @return 是否成功
     */
    boolean updateUser(UserDTO userDTO);
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 查询部门下的用户列表
     * 
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<User> listUsersByDepartmentId(Long departmentId);
    
    /**
     * 重置用户密码
     * 
     * @param userId   用户ID
     * @param password 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String password);
    
    /**
     * 更新用户密码
     * 
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 查询用户角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> listUserRoles(Long userId);

} 