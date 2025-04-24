package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.ExpenseMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.mapper.FundTransferMapper;
import com.vocational.researchfund.mapper.AchievementMapper;
import com.vocational.researchfund.service.DashboardService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private FundTransferMapper fundTransferMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private UserService userService;

    @Override
    public List<Map<String, Object>> getDashboardTasks(Boolean isAdmin) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            
            if (isAdmin) {
                // 管理员可以看到所有待审核的申请
                // 获取待审核的经费申请
                List<Map<String, Object>> pendingExpenses = expenseMapper.getPendingAuditExpenses();
                for (Map<String, Object> expense : pendingExpenses) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", expense.get("id"));
                    task.put("title", "审核经费申请: " + expense.get("title"));
                    task.put("status", "processing");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(3)));
                    task.put("projectName", expense.get("project_name"));
                    task.put("type", "expense");
                    tasks.add(task);
                }
                
                // 获取待审核的项目
                List<Map<String, Object>> pendingProjects = projectMapper.getPendingAuditProjects();
                for (Map<String, Object> project : pendingProjects) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", project.get("id"));
                    task.put("title", "审核项目申请: " + project.get("name"));
                    task.put("status", "urgent");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(5)));
                    task.put("projectName", project.get("name"));
                    task.put("type", "project");
                    tasks.add(task);
                }
                
                // 获取待审核的经费结转
                List<Map<String, Object>> pendingTransfers = fundTransferMapper.getPendingAuditTransfers();
                for (Map<String, Object> transfer : pendingTransfers) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", transfer.get("id"));
                    task.put("title", "审核经费结转: " + transfer.get("title"));
                    task.put("status", "normal");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(7)));
                    task.put("projectName", transfer.get("project_name"));
                    task.put("type", "transfer");
                    tasks.add(task);
                }
            } else {
                // 普通用户只能看到与自己相关的待办事项
                Long userId = currentUser.getId();
                
                // 获取用户的待审核经费申请
                List<Map<String, Object>> myPendingExpenses = expenseMapper.getUserPendingExpenses(userId);
                for (Map<String, Object> expense : myPendingExpenses) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", expense.get("id"));
                    task.put("title", "等待审核的经费申请: " + expense.get("title"));
                    task.put("status", "processing");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(3)));
                    task.put("projectName", expense.get("project_name"));
                    task.put("type", "expense");
                    tasks.add(task);
                }
                
                // 获取用户的待提交报销凭证的经费申请
                List<Map<String, Object>> receiptPendingExpenses = expenseMapper.getUserReceiptPendingExpenses(userId);
                for (Map<String, Object> expense : receiptPendingExpenses) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", expense.get("id"));
                    task.put("title", "需要提交报销凭证: " + expense.get("title"));
                    task.put("status", "urgent");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(2)));
                    task.put("projectName", expense.get("project_name"));
                    task.put("type", "receipt");
                    tasks.add(task);
                }
                
                // 获取用户负责的待结题项目
                List<Map<String, Object>> pendingCompletionProjects = projectMapper.getUserPendingCompletionProjects(userId);
                for (Map<String, Object> project : pendingCompletionProjects) {
                    Map<String, Object> task = new HashMap<>();
                    task.put("id", project.get("id"));
                    task.put("title", "需要提交结题报告: " + project.get("name"));
                    task.put("status", "normal");
                    task.put("deadline", formatDate(LocalDate.now().plusDays(14)));
                    task.put("projectName", project.get("name"));
                    task.put("type", "completion");
                    tasks.add(task);
                }
            }
            
            // 按紧急程度和截止日期排序
            tasks.sort((a, b) -> {
                String statusA = (String) a.get("status");
                String statusB = (String) b.get("status");
                
                // 先按状态排序
                int statusCompare = getStatusPriority(statusA) - getStatusPriority(statusB);
                if (statusCompare != 0) {
                    return statusCompare;
                }
                
                // 再按截止日期排序
                String deadlineA = (String) a.get("deadline");
                String deadlineB = (String) b.get("deadline");
                return deadlineA.compareTo(deadlineB);
            });
            
        } catch (Exception e) {
            logger.error("获取仪表盘待办事项失败", e);
        }
        
        return tasks;
    }
    
    private int getStatusPriority(String status) {
        switch (status) {
            case "urgent": return 1;
            case "processing": return 2;
            case "normal": return 3;
            default: return 4;
        }
    }

    @Override
    public List<Map<String, Object>> getDashboardProjects(Boolean isAdmin) {
        List<Map<String, Object>> projects = new ArrayList<>();
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            
            List<Map<String, Object>> projectsList;
            if (isAdmin) {
                // 管理员获取所有活跃项目
                projectsList = projectMapper.getActiveProjects();
            } else {
                // 普通用户获取自己参与的活跃项目
                projectsList = projectMapper.getUserActiveProjects(currentUser.getId());
            }
            
            // 转换为前端需要的格式
            for (Map<String, Object> project : projectsList) {
                Map<String, Object> formattedProject = new HashMap<>();
                formattedProject.put("id", project.get("id"));
                formattedProject.put("name", project.get("name"));
                formattedProject.put("status", project.get("status"));
                
                // 计算预算使用百分比
                BigDecimal budget = (BigDecimal) project.get("budget");
                BigDecimal usedBudget = (BigDecimal) project.get("used_budget");
                int budgetPercentage = budget.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                        usedBudget.multiply(new BigDecimal(100)).divide(budget, 0, RoundingMode.HALF_UP).intValue();
                
                formattedProject.put("budgetUsed", formatCurrency(usedBudget));
                formattedProject.put("budgetTotal", formatCurrency(budget));
                formattedProject.put("budgetPercentage", budgetPercentage);
                
                projects.add(formattedProject);
            }
            
            // 按预算使用百分比降序排序，优先显示预算消耗较多的项目
            projects.sort((a, b) -> {
                Integer percentageA = (Integer) a.get("budgetPercentage");
                Integer percentageB = (Integer) b.get("budgetPercentage");
                return percentageB.compareTo(percentageA);
            });
            
            // 限制返回最多5个项目
            if (projects.size() > 5) {
                projects = projects.subList(0, 5);
            }
            
        } catch (Exception e) {
            logger.error("获取仪表盘项目概览失败", e);
        }
        
        return projects;
    }

    @Override
    public Map<String, Object> getDashboardStats(Boolean isAdmin) {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            
            if (isAdmin) {
                // 管理员显示全局统计
                stats.put("projectCount", projectMapper.getProjectCount());
                stats.put("pendingExpenseCount", expenseMapper.getPendingExpenseCount());
                stats.put("pendingPaymentCount", expenseMapper.getPendingPaymentCount());
                
                // 计算全局预算使用率
                Map<String, Object> budgetUsage = projectMapper.getTotalBudgetUsage();
                BigDecimal totalBudget = (BigDecimal) budgetUsage.get("total_budget");
                BigDecimal totalUsedBudget = (BigDecimal) budgetUsage.get("total_used_budget");
                int budgetUsageRate = totalBudget.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                        totalUsedBudget.multiply(new BigDecimal(100)).divide(totalBudget, 0, RoundingMode.HALF_UP).intValue();
                
                stats.put("budgetUsageRate", budgetUsageRate);
                
            } else {
                // 普通用户只显示与自己相关的统计
                Long userId = currentUser.getId();
                stats.put("projectCount", projectMapper.getUserProjectCount(userId));
                stats.put("pendingExpenseCount", expenseMapper.getUserPendingExpenseCount(userId));
                stats.put("pendingPaymentCount", expenseMapper.getUserPendingPaymentCount(userId));
                
                // 计算用户相关项目的预算使用率
                Map<String, Object> budgetUsage = projectMapper.getUserBudgetUsage(userId);
                BigDecimal totalBudget = (BigDecimal) budgetUsage.get("total_budget");
                BigDecimal totalUsedBudget = (BigDecimal) budgetUsage.get("total_used_budget");
                int budgetUsageRate = totalBudget.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                        totalUsedBudget.multiply(new BigDecimal(100)).divide(totalBudget, 0, RoundingMode.HALF_UP).intValue();
                
                stats.put("budgetUsageRate", budgetUsageRate);
            }
            
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败", e);
            // 提供默认值
            stats.put("projectCount", 0);
            stats.put("pendingExpenseCount", 0);
            stats.put("pendingPaymentCount", 0);
            stats.put("budgetUsageRate", 0);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getProjectTypeStats(Boolean isAdmin) {
        List<Map<String, Object>> typeStats = new ArrayList<>();
        try {
            // 获取当前用户
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            
            List<Map<String, Object>> rawTypeStats;
            if (isAdmin) {
                // 管理员获取所有项目类型统计
                rawTypeStats = projectMapper.getProjectTypeStats();
            } else {
                // 普通用户获取自己参与的项目类型统计
                rawTypeStats = projectMapper.getUserProjectTypeStats(currentUser.getId());
            }
            
            // 转换为前端需要的格式
            for (Map<String, Object> typeStat : rawTypeStats) {
                Map<String, Object> formattedTypeStat = new HashMap<>();
                String type = (String) typeStat.get("type");
                
                formattedTypeStat.put("name", getProjectTypeLabel(type));
                formattedTypeStat.put("value", typeStat.get("count"));
                
                typeStats.add(formattedTypeStat);
            }
            
        } catch (Exception e) {
            logger.error("获取项目类型统计失败", e);
            // 提供默认数据
            typeStats.add(createTypeStatMap("校级项目", 0));
            typeStats.add(createTypeStatMap("横向项目", 0));
            typeStats.add(createTypeStatMap("纵向项目", 0));
        }
        
        // 如果没有数据，提供默认值
        if (typeStats.isEmpty()) {
            typeStats.add(createTypeStatMap("校级项目", 0));
            typeStats.add(createTypeStatMap("横向项目", 0));
            typeStats.add(createTypeStatMap("纵向项目", 0));
        }
        
        return typeStats;
    }
    
    private Map<String, Object> createTypeStatMap(String name, int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
    
    private String getProjectTypeLabel(String type) {
        switch (type) {
            case "school": return "校级项目";
            case "horizontal": return "横向项目";
            case "vertical": return "纵向项目";
            default: return "其他";
        }
    }

    @Override
    public List<Map<String, Object>> getResearchResults(Boolean isAdmin) {
        logger.info("获取科研成果数量统计, isAdmin={}", isAdmin);
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // 获取当前用户ID
            String userId = null;
            if (!isAdmin) {
                String username = SecurityUtils.getCurrentUsername();
                User currentUser = userService.getUserByUsername(username);
                if (currentUser != null) {
                    userId = currentUser.getId().toString();
                }
            }
            
            // 准备统计查询参数
            Map<String, Object> params = new HashMap<>();
            
            // 获取成果类型统计数据
            Map<String, Integer> typeCounts = new LinkedHashMap<>();
            typeCounts.put("journal", 0);     // 期刊论文
            typeCounts.put("conference", 0);  // 会议论文
            typeCounts.put("patent", 0);      // 专利
            typeCounts.put("book", 0);        // 著作
            typeCounts.put("software", 0);    // 软件著作权
            typeCounts.put("other", 0);       // 其他
            
            // 查询各类型的成果数量
            List<Map<String, Object>> typeStats;
            if (isAdmin) {
                // 管理员查看所有成果统计
                typeStats = achievementMapper.countByType(params);
            } else {
                // 普通用户只查看自己的成果统计
                params.put("creatorId", userId);
                typeStats = achievementMapper.countByTypeAndCreator(userId, params);
            }
            
            // 处理统计结果
            if (typeStats != null && !typeStats.isEmpty()) {
                for (Map<String, Object> stat : typeStats) {
                    String type = (String) stat.get("type");
                    Integer count = ((Number) stat.get("count")).intValue();
                    if (typeCounts.containsKey(type)) {
                        typeCounts.put(type, count);
                    }
                }
            }
            
            // 转换为前端需要的格式
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                String typeName = getAchievementTypeLabel(entry.getKey());
                results.add(createResultMap(typeName, entry.getValue()));
            }
            
        } catch (Exception e) {
            logger.error("获取科研成果统计失败", e);
            // 提供默认空数据
            results.add(createResultMap("期刊论文", 0));
            results.add(createResultMap("会议论文", 0));
            results.add(createResultMap("专利", 0));
            results.add(createResultMap("著作", 0));
            results.add(createResultMap("软件著作权", 0));
            results.add(createResultMap("其他", 0));
        }
        
        return results;
    }
    
    private String getAchievementTypeLabel(String type) {
        switch (type) {
            case "journal": return "期刊论文";
            case "conference": return "会议论文";
            case "patent": return "专利";
            case "book": return "著作";
            case "software": return "软件著作权";
            case "other": return "其他";
            default: return "未知";
        }
    }
    
    private Map<String, Object> createResultMap(String name, int value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
    
    /**
     * 格式化日期为字符串
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 格式化货币金额
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "¥0.00";
        return amount.setScale(2, RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",")
                .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",");
    }
} 