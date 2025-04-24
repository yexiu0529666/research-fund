package com.vocational.researchfund.config;

import com.vocational.researchfund.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("配置Spring Security安全规则");
        
        http.csrf().disable() // 禁用CSRF
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态会话
            .and()
            .authorizeRequests()
            // 以下路径无需认证
            .antMatchers(HttpMethod.POST, "/api/user/login", "/api/user/register").permitAll()
            .antMatchers("/api/public/**", "/api/department/all", "/api/department/active").permitAll()
            .antMatchers("/api/user/departments", "/api/project/stat/**", "/api/expense/stat/**").permitAll()
            .antMatchers("/api/upload/download").permitAll() // 文件下载接口无需认证
            .antMatchers("/static/**", "/demo/**", "/api-docs/**", "/swagger/**", 
                         "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/favicon.ico").permitAll()
            // OPTIONS请求都放行
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 其他所有请求需要认证
            .anyRequest().authenticated();
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 允许跨域
        http.cors();
        
        logger.info("Spring Security配置完成");
    }

    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 