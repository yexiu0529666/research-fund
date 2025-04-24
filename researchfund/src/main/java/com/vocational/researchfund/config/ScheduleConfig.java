package com.vocational.researchfund.config;

import com.vocational.researchfund.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Configuration
public class ScheduleConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);
    
    @Autowired
    private ExpenseService expenseService;
    
    /**
     * 每天凌晨2点检查借款申请状态
     * 将已支付的借款更新为待提交报销凭证状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAdvanceExpensesStatus() {
        logger.info("开始执行借款状态检查定时任务...");
        try {
            expenseService.checkAdvanceExpensesStatus();
            logger.info("借款状态检查定时任务执行完成");
        } catch (Exception e) {
            logger.error("借款状态检查定时任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每天凌晨3点检查待提交报销凭证的申请
     * 对已超过项目结束时间的转为负责人自行还款状态
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkReceiptPendingExpenses() {
        logger.info("开始执行报销凭证检查定时任务...");
        try {
            expenseService.checkReceiptPendingExpenses();
            logger.info("报销凭证检查定时任务执行完成");
        } catch (Exception e) {
            logger.error("报销凭证检查定时任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每10秒执行一次检查，用于测试
     * 仅在开发环境使用
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void testCheckExpenseStatus() {
        logger.info("开始测试执行经费状态检查定时任务...");
        try {
            // 检查借款状态
            expenseService.checkAdvanceExpensesStatus();
            // 检查报销凭证状态
            expenseService.checkReceiptPendingExpenses();
            logger.info("测试经费状态检查任务执行完成");
        } catch (Exception e) {
            logger.error("测试经费状态检查任务执行失败: {}", e.getMessage(), e);
        }
    }
} 