package com.vocational.researchfund.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.entity.Role;
import com.vocational.researchfund.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 获取角色列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param roleName 角色名称（可选）
     * @param roleCode 角色代码（可选）
     * @param status   状态（可选）
     * @return 角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Map<String, Object>> getRoleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        try {
            logger.info("获取角色列表请求: pageNum={}, pageSize={}, roleName={}, roleCode={}, status={}",
                    pageNum, pageSize, roleName, roleCode, status);
            
            // 构建查询条件
            Role role = new Role();
            role.setRoleName(roleName);
            role.setRoleCode(roleCode);
            role.setStatus(status);
            
            // 查询数据
            IPage<Role> rolePage = roleService.listRolePage(pageNum, pageSize, role);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("rows", rolePage.getRecords());
            result.put("total", rolePage.getTotal());
            result.put("pages", rolePage.getPages());
            result.put("pageSize", rolePage.getSize());
            result.put("pageNum", rolePage.getCurrent());
            
            logger.info("获取角色列表成功: 共{}条记录", rolePage.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取角色列表异常: {}", e.getMessage(), e);
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    @GetMapping("/all")
    public Result<List<Role>> getAllRoles() {
        try {
            logger.info("获取所有角色请求");
            
            // 查询所有角色
            List<Role> roles = roleService.getAllRoles();
            
            logger.info("获取所有角色成功: 共{}条记录", roles.size());
            return Result.success(roles);
        } catch (Exception e) {
            logger.error("获取所有角色异常: {}", e.getMessage(), e);
            return Result.error("获取所有角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Role> getRoleDetail(@PathVariable Long id) {
        try {
            logger.info("获取角色详情请求: id={}", id);
            
            Role role = roleService.getRoleDetail(id);
            if (role == null) {
                logger.warn("获取角色详情失败: 角色不存在 - {}", id);
                return Result.error("角色不存在");
            }
            
            logger.info("获取角色详情成功: id={}", id);
            return Result.success(role);
        } catch (Exception e) {
            logger.error("获取角色详情异常: {}", e.getMessage(), e);
            return Result.error("获取角色详情失败: " + e.getMessage());
        }
    }
    
    /**
     * a添加角色
     *
     * @param role 角色信息
     * @return 添加结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> addRole(@RequestBody Role role) {
        try {
            logger.info("添加角色请求: {}", role);
            
            boolean success = roleService.addRole(role);
            if (success) {
                logger.info("添加角色成功: {}", role.getRoleName());
                return Result.success();
            } else {
                logger.warn("添加角色失败: {}", role.getRoleName());
                return Result.error("添加角色失败");
            }
        } catch (Exception e) {
            logger.error("添加角色异常: {}", e.getMessage(), e);
            return Result.error("添加角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新角色
     *
     * @param id   角色ID
     * @param role 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            logger.info("更新角色请求: id={}, role={}", id, role);
            
            role.setId(id);
            boolean success = roleService.updateRole(role);
            if (success) {
                logger.info("更新角色成功: id={}", id);
                return Result.success();
            } else {
                logger.warn("更新角色失败: id={}", id);
                return Result.error("更新角色失败");
            }
        } catch (Exception e) {
            logger.error("更新角色异常: {}", e.getMessage(), e);
            return Result.error("更新角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            logger.info("删除角色请求: id={}", id);
            
            boolean success = roleService.deleteRole(id);
            if (success) {
                logger.info("删除角色成功: id={}", id);
                return Result.success();
            } else {
                logger.warn("删除角色失败: id={}", id);
                return Result.error("删除角色失败");
            }
        } catch (Exception e) {
            logger.error("删除角色异常: {}", e.getMessage(), e);
            return Result.error("删除角色失败: " + e.getMessage());
        }
    }
}