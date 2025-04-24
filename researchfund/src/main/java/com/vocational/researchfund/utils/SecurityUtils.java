package com.vocational.researchfund.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.vocational.researchfund.security.SecurityUser;
import org.springframework.stereotype.Component;


/**
 * 安全工具类
 */
@Component
@Slf4j
public class SecurityUtils {


    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户的真实姓名
     * @return 真实姓名
     */
    public static String getCurrentUserRealName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                // 在UserDetails实现类中存储了用户的真实姓名
                // 这里假设使用了自定义的UserDetails实现，实际需要根据项目具体实现调整
                if (userDetails instanceof SecurityUser) {
                    return ((SecurityUser) userDetails).getRealName();
                }
            }
            // 如果无法获取真实姓名，则返回用户名
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                // 在UserDetails实现类中存储了用户ID
                // 这里假设使用了自定义的UserDetails实现，实际需要根据项目具体实现调整
                if (userDetails instanceof SecurityUser) {
                    return ((SecurityUser) userDetails).getId();
                }
            }
        }
        throw new UsernameNotFoundException("当前用户未登录或未找到用户ID");
    }

} 