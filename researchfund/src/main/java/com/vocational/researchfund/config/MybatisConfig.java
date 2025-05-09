package com.vocational.researchfund.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan(basePackages = {"com.vocational.researchfund.mapper", "com.vocational.researchfund.repository"})
public class MybatisConfig {
    // 配置已经通过注解完成
} 