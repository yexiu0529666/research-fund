package com.vocational.researchfund.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全用户，实现UserDetails接口
 */
public class SecurityUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 权限列表
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 账号是否未过期
     */
    private boolean accountNonExpired = true;

    /**
     * 账号是否未锁定
     */
    private boolean accountNonLocked = true;

    /**
     * 凭证是否未过期
     */
    private boolean credentialsNonExpired = true;

    /**
     * 账号是否启用
     */
    private boolean enabled = true;

    /**
     * 构造函数
     */
    public SecurityUser() {
    }

    /**
     * 构造函数
     *
     * @param id       用户ID
     * @param username 用户名
     * @param password 密码
     * @param realName 真实姓名
     * @param roles    角色列表
     */
    public SecurityUser(Long id, String username, String password, String realName, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
} 