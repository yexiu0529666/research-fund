package com.vocational.researchfund.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Component
public class JwtTokenUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    @Value("${researchfund.jwt.secret}")
    private String secret;
    
    @Value("${researchfund.jwt.expire}")
    private long expire;
    
    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从token中获取指定的claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从token中获取所有的claim
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    
    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        logger.debug("为用户 {} 生成Token", userDetails.getUsername());
        return doGenerateToken(claims, userDetails.getUsername());
    }
    
    /**
     * 生成token
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire * 1000);
        logger.debug("生成的Token将在 {} 过期", expiryDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * 验证token并返回用户名
     * 如果验证失败，返回null
     */
    public String validateToken(String token) {
        try {
            String username = getUsernameFromToken(token);
            logger.debug("从Token中提取的用户名: {}", username);
            
            if (isTokenExpired(token)) {
                logger.warn("Token已过期");
                return null;
            }
            
            logger.debug("Token验证成功，用户: {}", username);
            return username;
        } catch (ExpiredJwtException e) {
            logger.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.error("Token验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Token处理异常: {}", e.getMessage(), e);
            throw new JwtException("Token验证发生未知错误", e);
        }
    }
    
    /**
     * 验证token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            
            if (isValid) {
                logger.debug("Token验证成功，用户: {}", username);
            } else {
                logger.warn("Token验证失败，用户名不匹配或Token已过期");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("验证Token时发生异常: {}", e.getMessage(), e);
            return false;
        }
    }
} 