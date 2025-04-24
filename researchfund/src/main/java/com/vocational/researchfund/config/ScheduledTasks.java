package com.vocational.researchfund.config;

import com.vocational.researchfund.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Configuration
@EnableScheduling
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private ProjectService projectService;

    /**
     * 每天凌晨1点检查已过期项目并更新状态
     * 将已超过结束日期但状态仍为active的项目更新为待结题状态
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredProjects() {
        logger.info("开始执行检查过期项目定时任务");
        try {
            projectService.checkAndUpdateExpiredProjects();
            logger.info("过期项目检查完成");
        } catch (Exception e) {
            logger.error("过期项目检查失败", e);
        }
    }


    /**
     * 测试 10s
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void checkExpiredProjects10() {
        logger.info("开始执行检查过期项目定时任务");
        try {
            projectService.checkAndUpdateExpiredProjects();
            logger.info("过期项目检查完成");
        } catch (Exception e) {
            logger.error("过期项目检查失败", e);
        }
    }
} 