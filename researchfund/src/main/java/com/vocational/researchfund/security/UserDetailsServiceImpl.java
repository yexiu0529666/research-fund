package com.vocational.researchfund.security;

import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义UserDetailsService实现类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 获取用户角色
        List<String> roleList = new ArrayList<>();
        if (user.getRoles() != null) {
            roleList = user.getRoles().stream()
                    .map(roleMap -> (String) roleMap.get("roleCode"))
                    .collect(Collectors.toList());
        }

        // 创建SecurityUser
        return new SecurityUser(
                user.getId(), 
                user.getUsername(), 
                user.getPassword(), 
                user.getRealName(), 
                roleList
        );
    }
} 