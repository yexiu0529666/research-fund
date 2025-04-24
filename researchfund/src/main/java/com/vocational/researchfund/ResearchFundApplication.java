package com.vocational.researchfund;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.vocational.researchfund.mapper")
@EnableScheduling
public class ResearchFundApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchFundApplication.class, args);
    }

} 