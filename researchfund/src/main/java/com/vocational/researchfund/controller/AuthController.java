package com.vocational.researchfund.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.dto.UserDTO;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.entity.Department;
import com.vocational.researchfund.entity.Role;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.service.DepartmentService;
import com.vocational.researchfund.service.RoleService;
import com.vocational.researchfund.utils.JwtTokenUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/user")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("登录请求: 用户名={},密码={},长度={}",
                    loginRequest.getUsername(), loginRequest.getPassword(),loginRequest.getPassword().length());
            
            // 查找用户
            User user = userService.getUserByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());
            if (user == null) {
                logger.warn("登录失败: 用户不存在或密码错误 - {}", loginRequest.getUsername());
                return Result.error("用户名或密码错误");
            }
            
            logger.info("登录成功: 用户名={}, ID={}", user.getUsername(), user.getId());
            
            // 获取角色
            List<String> roles;
            try {
                roles = userService.listUserRoles(user.getId());
                if (roles == null || roles.isEmpty()) {
                    if ("admin".equals(user.getUsername())) {
                        roles = Arrays.asList("admin");
                    } else {
                        roles = Arrays.asList("user");
                    }
                }
            } catch (Exception e) {
                logger.warn("获取角色失败，使用默认角色: {}", e.getMessage());
                roles = Arrays.asList("user");
            }
            
            // 构建UserDetails对象用于生成令牌
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password("")  // 已验证，不需要密码
                    .authorities(roles.stream()
                            .map(role -> "ROLE_" + role)
                            .collect(Collectors.toList())
                            .toArray(new String[0]))
                    .build();
            
            // 生成JWT令牌
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.debug("生成令牌: {}...", token.substring(0, Math.min(token.length(), 20)));
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            data.put("roles", roles);
            
            return Result.success(data);
            
        } catch (Exception e) {
            logger.error("登录异常: {}", e.getMessage(), e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        
        // 检查用户名是否已存在
        User existUser = userService.getUserByUsername(registerRequest.getUsername());
        if (existUser != null) {
            logger.warn("注册失败: 用户名已存在 - {}", registerRequest.getUsername());
            return Result.error("用户名已存在");
        }
        // 创建用户
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerRequest.getUsername());
        userDTO.setPassword(registerRequest.getPassword());
        userDTO.setRealName(registerRequest.getRealName());
        userDTO.setPhone(registerRequest.getPhone());
        userDTO.setEmail(registerRequest.getEmail());
        userDTO.setDepartmentId(registerRequest.getDepartmentId());
        userDTO.setGender(registerRequest.getGender());
        userDTO.setStatus(0); // 正常状态，允许直接登录
        
        // 设置角色，如果前端没有传递则使用默认角色
        if (registerRequest.getRoleIds() != null && !registerRequest.getRoleIds().isEmpty()) {
            userDTO.setRoleIds(registerRequest.getRoleIds());
            logger.info("用户注册设置角色: username={}, roleIds={}", registerRequest.getUsername(), registerRequest.getRoleIds());
        } else {
            // 设置默认角色为科研人员
            userDTO.setRoleIds(Arrays.asList(2L));
            logger.info("用户注册设置默认角色: username={}, defaultRoleId=3", registerRequest.getUsername());
        }
        
        // 保存用户
        boolean result = userService.addUser(userDTO);
        if (result) {
            logger.info("用户注册成功: {}", registerRequest.getUsername());
            return Result.success();
        } else {
            logger.error("用户注册失败: {}", registerRequest.getUsername());
            return Result.error("注册失败");
        }
    }
    
    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "unknown";
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }
            logger.info("用户登出: {}", username);
        } else {
            logger.warn("用户尝试登出，但未认证");
        }
        
        // 清除Security上下文
        SecurityContextHolder.clearContext();
        
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("修改密码失败: 用户未登录");
                return Result.error("请先登录");
            }
            
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                logger.error("修改密码失败: 找不到用户 - {}", username);
                return Result.error("找不到用户信息");
            }
            
            // 验证旧密码并更新密码
            boolean success = userService.updatePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
            if (success) {
                logger.info("密码修改成功: {}", username);
                return Result.success();
            } else {
                logger.warn("密码修改失败: {}", username);
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            logger.error("修改密码异常: {}", e.getMessage(), e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置密码（管理员使用）
     */
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // 检查当前用户是否有权限
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("重置密码失败: 用户未登录");
                return Result.error("请先登录");
            }
            
            // 检查是否有管理员权限
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_admin"));
            if (!isAdmin) {
                logger.warn("重置密码失败: 用户无权限 - {}", authentication.getName());
                return Result.error("无权限执行此操作");
            }
            
            // 重置密码
            boolean success = userService.resetPassword(request.getUserId(), request.getNewPassword());
            if (success) {
                logger.info("密码重置成功: 用户ID={}", request.getUserId());
                return Result.success();
            } else {
                logger.warn("密码重置失败: 用户ID={}", request.getUserId());
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            logger.error("重置密码异常: {}", e.getMessage(), e);
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @RequestMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("获取用户信息失败: 用户未登录");
                return Result.error("请先登录");
            }
            
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                logger.error("获取用户信息失败: 找不到用户 - {}", username);
                return Result.error("找不到用户信息");
            }
            
            // 获取角色
            List<String> roles = userService.listUserRoles(user.getId());
            if (roles == null || roles.isEmpty()) {
                if ("admin".equals(user.getUsername())) {
                    roles = Arrays.asList("admin");
                } else {
                    roles = Arrays.asList("user");
                }
            }
            
            // 构建权限列表
            List<String> permissions = new ArrayList<>();
            // 根据角色添加相应的权限
            if (roles.contains("admin")) {
                permissions.add("*:*:*"); // 管理员拥有所有权限
            } else if (roles.contains("manager")) {
                permissions.add("expense:audit");
                permissions.add("payment:pay");
            }
            
            // 获取部门信息
            Department department = null;
            if (user.getDepartmentId() != null) {
                department = departmentService.getById(user.getDepartmentId());
                // 设置部门名称
                if (department != null) {
                    user.setDepartmentName(department.getName());
                }
            }
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("roles", roles);
            data.put("permissions", permissions);
            data.put("department", department);
            
            return Result.success(data);
            
        } catch (Exception e) {
            logger.error("获取用户信息异常: {}", e.getMessage(), e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("更新用户信息失败: 用户未登录");
                return Result.error("请先登录");
            }
            
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                logger.error("更新用户信息失败: 找不到当前用户 - {}", username);
                return Result.error("用户信息不存在");
            }
            
            // 检查是否有权限更新此用户信息
            boolean isAdmin = userService.listUserRoles(currentUser.getId()).contains("ROLE_ADMIN");
            if (!currentUser.getId().equals(id) && !isAdmin) {
                logger.warn("更新用户信息失败: 无权限 - userId={}, currentUser={}", id, currentUser.getId());
                return Result.error("无权限修改其他用户信息");
            }
            
            // 设置用户ID
            userDTO.setId(id);
            
            // 更新用户信息
            boolean success = userService.updateUser(userDTO);
            if (success) {
                logger.info("用户信息更新成功: userId={}", id);
                return Result.success();
            } else {
                logger.warn("用户信息更新失败: userId={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            logger.error("更新用户信息异常: {}", e.getMessage(), e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("删除用户失败: 用户未登录");
                return Result.error("请先登录");
            }
            
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                logger.error("删除用户失败: 找不到当前用户 - {}", username);
                return Result.error("用户信息不存在");
            }
            
            // 检查是否有权限删除用户
            boolean isAdmin = userService.listUserRoles(currentUser.getId()).contains("ROLE_ADMIN");
            if (!isAdmin) {
                logger.warn("删除用户失败: 无权限 - userId={}, currentUser={}", id, currentUser.getId());
                return Result.error("无权限删除用户");
            }
            
            // 检查是否删除admin账户
            User targetUser = userService.getById(id);
            if (targetUser != null && "admin".equals(targetUser.getUsername())) {
                logger.warn("删除用户失败: 尝试删除admin账户 - userId={}", id);
                return Result.error("不能删除管理员账户");
            }
            
            // 删除用户和关联的角色信息
            boolean success = userService.deleteUser(id);
            if (success) {
                logger.info("用户删除成功: userId={}", id);
                return Result.success();
            } else {
                logger.warn("用户删除失败: userId={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除用户异常: {}", e.getMessage(), e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    /**
     * 注册请求
     */
    @Data
    public static class RegisterRequest {
        
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
        
        @NotBlank(message = "姓名不能为空")
        private String realName;
        
        private String phone;
        
        private String email;
        
        private Long departmentId;
        
        private Integer gender = 0;
        
        private List<Long> roleIds;
    }
    
    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
    
    /**
     * 重置密码请求
     */
    @Data
    public static class ResetPasswordRequest {
        
        private Long userId;
        
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    /**
     * 获取用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param realName 真实姓名（可选）
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {
        try {
            logger.info("获取用户列表请求: pageNum={}, pageSize={}, username={}, realName={}",
                    pageNum, pageSize, username, realName);

            // 构建查询条件
            User user = new User();
            user.setUsername(username);
            user.setRealName(realName);

            // 查询数据
            IPage<User> userPage = userService.listUserPage(pageNum, pageSize, user);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("rows", userPage.getRecords());
            result.put("total", userPage.getRecords().size());
            result.put("pages", userPage.getPages());
            result.put("pageSize", userPage.getSize());
            result.put("pageNum", userPage.getCurrent());

            logger.info("获取用户列表成功: 共{}条记录", userPage.getRecords().size());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取用户列表异常: {}", e.getMessage(), e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }
} 