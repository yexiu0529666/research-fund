package com.vocational.researchfund.security;

import com.vocational.researchfund.utils.JwtTokenUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
    
    // 定义不需要验证的URL模式
    private static final List<String> EXCLUDE_URL_PATTERNS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/public/**",
            "/api/department/all",
            "/api/department/active",
            "/api/user/departments", 
            "/api/project/stat/**",
            "/api/expense/stat/**",
            "/api/upload/download",
            "/static/**",
            "/demo/**",
            "/api-docs/**",
            "/swagger/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/favicon.ico"
    );
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取请求信息
        String contextPath = request.getContextPath();
        String fullRequestURI = request.getRequestURI();
        String requestURI = URL_PATH_HELPER.getPathWithinApplication(request);
        String method = request.getMethod();
        
        // 去除上下文路径，获取相对路径
        if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }
        
        logger.debug("处理请求: {} {} [完整路径: {}, 上下文路径: {}]", 
                method, requestURI, fullRequestURI, contextPath);
        
        // 判断是否需要跳过验证
        boolean skipAuth = false;
        
        // 1. 检查是否是OPTIONS请求
        if (HttpMethod.OPTIONS.matches(method)) {
            logger.debug("跳过OPTIONS预检请求: {}", requestURI);
            skipAuth = true;
        }
        
        // 2. 检查是否是登录/注册请求
        else if (HttpMethod.POST.matches(method) && 
                (requestURI.equals("/api/user/login") || requestURI.equals("/api/user/register"))) {
            logger.debug("跳过登录/注册请求: {}", requestURI);
            skipAuth = true;
        }
        
        // 3. 检查是否是其他免认证的URL
        else {
            for (String pattern : EXCLUDE_URL_PATTERNS) {
                if (pathMatcher.match(pattern, requestURI)) {
                    logger.debug("匹配到白名单路径: {} -> {}", pattern, requestURI);
                    skipAuth = true;
                    break;
                }
            }
        }
        
        // 如果需要跳过验证，直接放行
        if (skipAuth) {
            logger.debug("跳过JWT认证: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 记录调试信息
        if (requestURI.contains("login") || requestURI.contains("register")) {
            logger.warn("登录/注册路径未被排除，请检查配置: {}", requestURI);
            logger.warn("当前白名单路径:");
            for (String pattern : EXCLUDE_URL_PATTERNS) {
                logger.warn("  - {}", pattern);
            }
        }

        try {
            // 从请求头中获取Authorization字段
            String authHeader = request.getHeader("Authorization");
            
            // 检查token是否存在且格式正确
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("缺少或格式不正确的Authorization头: [{}], 请求路径: {}", 
                        authHeader != null ? authHeader : "null", requestURI);
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\": 401, \"message\": \"未授权，请先登录\"}");
                return;
            }

            // 提取token
            String token = authHeader.substring(7);
            logger.debug("提取的Token: {}...", token.length() > 20 ? token.substring(0, 20) : token);

            // 验证token并获取用户名
            String username = jwtTokenUtil.validateToken(token);
            if (username != null) {
                // 从数据库中获取用户对象
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("从Token加载用户: {}", username);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息放入Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("用户 {} 认证成功", username);

                // 继续过滤器链
                filterChain.doFilter(request, response);
            } else {
                logger.warn("无效的Token");
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\": 401, \"message\": \"登录已过期，请重新登录\"}");
            }
        } catch (JwtException e) {
            logger.error("Token验证失败: {}", e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 401, \"message\": \"登录凭证无效，请重新登录\"}");
        } catch (Exception e) {
            logger.error("认证过程异常: {}", e.getMessage(), e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"code\": 500, \"message\": \"服务器错误\"}");
        }
    }
} 