package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.dto.AttachmentDTO;
import com.vocational.researchfund.dto.ExpenseDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.AttachmentMapper;
import com.vocational.researchfund.mapper.ExpenseMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.service.ExpenseService;
import com.vocational.researchfund.service.ProjectService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 经费申请服务实现类
 */
@Service
public class ExpenseServiceImpl implements ExpenseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpenseServiceImpl.class);
    
    @Autowired
    private ExpenseMapper expenseMapper;
    
    @Autowired
    private AttachmentMapper attachmentMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Override
    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        // 设置默认状态为待审核
        expenseDTO.setStatus("pending");
        
        // 设置默认类别为经费借款，如果没有提供
        if (expenseDTO.getCategory() == null || expenseDTO.getCategory().isEmpty()) {
            expenseDTO.setCategory("advance");
        }
        
        // 获取申请人信息
        User currentUser = userService.getUserByUsername(SecurityUtils.getCurrentUsername());
        if (currentUser == null) {
            throw new RuntimeException("未找到当前登录用户信息");
        }
        
        // 设置申请人信息
        expenseDTO.setApplyUserId(currentUser.getId());
        expenseDTO.setApplyUserName(currentUser.getRealName());
        
        // 如果前端没有传入项目名称，但传入了项目ID，则根据项目ID获取项目名称
        if ((expenseDTO.getProjectName() == null || expenseDTO.getProjectName().isEmpty()) 
                && expenseDTO.getProjectId() != null) {
            try {
                ProjectDTO project = projectService.getProjectById(expenseDTO.getProjectId());
                expenseDTO.setProjectName(project.getName());
                
                // 检查申请的经费类型是否在项目预算科目中
                validateExpenseTypeAndBudget(project, expenseDTO.getType(), expenseDTO.getAmount(), null);
            } catch (Exception e) {
                throw new RuntimeException("申请经费失败， " + e.getMessage());
            }
        }
        
        // 设置申请日期
        if (expenseDTO.getApplyDate() == null) {
            expenseDTO.setApplyDate(LocalDate.now());
        }
        
        // 设置创建信息
        expenseDTO.setCreateTime(LocalDateTime.now());
        
        // 插入数据库
        expenseMapper.insert(expenseDTO);
        
        // 保存附件
        if (expenseDTO.getAttachments() != null && !expenseDTO.getAttachments().isEmpty()) {
            // 先设置经费申请ID
            for (AttachmentDTO attachment : expenseDTO.getAttachments()) {
                attachment.setExpenseId(expenseDTO.getId());
            }
            // 批量插入附件
            attachmentMapper.batchInsert(expenseDTO.getAttachments(), expenseDTO.getId());
        }
        
        return getExpenseById(expenseDTO.getId());
    }
    
    @Override
    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        // 检查经费申请是否存在
        ExpenseDTO existingExpense = expenseMapper.selectById(id);
        if (existingExpense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 检查状态，只有待审核状态的经费申请才能修改
        if (!"pending".equals(existingExpense.getStatus())) {
            throw new RuntimeException("只有待审核状态的经费申请才能修改");
        }
        
        // 获取当前用户信息
        User currentUser = userService.getUserByUsername(SecurityUtils.getCurrentUsername());
        if (currentUser == null) {
            throw new RuntimeException("未找到当前登录用户信息");
        }
        
        // 设置ID
        expenseDTO.setId(id);
        
        // 保留原有的申请人信息和状态
        expenseDTO.setApplyUserId(existingExpense.getApplyUserId());
        expenseDTO.setApplyUserName(existingExpense.getApplyUserName());
        expenseDTO.setStatus(existingExpense.getStatus());
        
        
        // 如果更新了项目ID、经费类型或金额，需要重新检查项目预算
        boolean projectChanged = expenseDTO.getProjectId() != null && !expenseDTO.getProjectId().equals(existingExpense.getProjectId());
        boolean typeChanged = expenseDTO.getType() != null && !expenseDTO.getType().equals(existingExpense.getType());
        boolean amountChanged = expenseDTO.getAmount() != null && expenseDTO.getAmount().compareTo(existingExpense.getAmount()) != 0;
        
        if (projectChanged || typeChanged || amountChanged) {
            try {
                Long projectId = expenseDTO.getProjectId() != null ? expenseDTO.getProjectId() : existingExpense.getProjectId();
                String type = expenseDTO.getType() != null ? expenseDTO.getType() : existingExpense.getType();
                BigDecimal amount = expenseDTO.getAmount() != null ? expenseDTO.getAmount() : existingExpense.getAmount();
                
                ProjectDTO project = projectService.getProjectById(projectId);
                
                // 设置项目名称
                if (projectChanged || expenseDTO.getProjectName() == null || expenseDTO.getProjectName().isEmpty()) {
                    expenseDTO.setProjectName(project.getName());
                }
                
                // 检查申请的经费类型是否在项目预算科目中，并验证预算是否足够
                validateExpenseTypeAndBudget(project, type, amount, 
                        (!projectChanged && !typeChanged) ? existingExpense : null);
            } catch (Exception e) {
                throw new RuntimeException("修改经费申请失败， " + e.getMessage());
            }
        }
        
        // 更新数据库
        expenseMapper.update(expenseDTO);
        
        // 更新附件：先删除原有附件，再添加新附件
        if (expenseDTO.getAttachments() != null) {
            // 删除原有附件
            attachmentMapper.deleteByExpenseId(id);
            
            // 添加新附件
            if (!expenseDTO.getAttachments().isEmpty()) {
                // 设置经费申请ID
                for (AttachmentDTO attachment : expenseDTO.getAttachments()) {
                    attachment.setExpenseId(id);
                }
                // 批量插入附件
                attachmentMapper.batchInsert(expenseDTO.getAttachments(), id);
            }
        }
        
        return getExpenseById(id);
    }
    
    /**
     * 验证经费申请类型和金额是否符合项目预算要求
     * @param project 项目
     * @param expenseType 申请类型
     * @param amount 申请金额
     * @param existingExpense 已存在的经费申请（用于更新时计算差额）
     * @throws RuntimeException 如果类型不符合或超出预算时抛出异常
     */
    private void validateExpenseTypeAndBudget(ProjectDTO project, String expenseType, BigDecimal amount, 
                                             ExpenseDTO existingExpense) {
        if (project == null || expenseType == null || amount == null) {
            throw new RuntimeException("参数不完整，无法验证预算");
        }
        
        // 获取项目的所有预算科目
        List<ProjectDTO.BudgetItemDTO> budgetItems = project.getBudgetItems();
        if (budgetItems == null || budgetItems.isEmpty()) {
            throw new RuntimeException("项目未设置预算科目，无法申请经费");
        }
        
        // 获取项目已使用的经费（按类型）
        List<Map<String, Object>> expenseStats = expenseMapper.getExpenseStatsByProject(project.getId());
        Map<String, BigDecimal> usedAmountByType = new HashMap<>();
        
        // 将经费使用情况转换为Map便于查找
        if (expenseStats != null) {
            for (Map<String, Object> stat : expenseStats) {
                String type = (String) stat.get("type");
                BigDecimal usedAmount = (BigDecimal) stat.get("amount");
                if (type != null && usedAmount != null) {
                    usedAmountByType.put(type, usedAmount);
                }
            }
        }
        
        // 找到与申请类型对应的预算科目
        String categoryName = null;
        BigDecimal budgetAmount = null;
        
        // 经费类型与预算科目名称的映射关系
        Map<String, String> typeToCategoryMap = new HashMap<>();
        typeToCategoryMap.put("equipment", "设备费");
        typeToCategoryMap.put("material", "材料费");
        typeToCategoryMap.put("test", "测试化验费");
        typeToCategoryMap.put("travel", "差旅费");
        typeToCategoryMap.put("meeting", "会议费");
        typeToCategoryMap.put("labor", "劳务费");
        typeToCategoryMap.put("consultation", "专家咨询费");
        typeToCategoryMap.put("other", "其他费用");
        
        String expectedCategory = typeToCategoryMap.get(expenseType);
        
        for (ProjectDTO.BudgetItemDTO item : budgetItems) {
            if (item.getCategory().equals(expectedCategory)) {
                categoryName = item.getCategory();
                budgetAmount = item.getAmount();
                break;
            }
        }
        
        if (categoryName == null || budgetAmount == null) {
            throw new RuntimeException("申请的经费类型 [" + expenseType + "] 不在项目预算科目中，请选择项目预算中已有的科目");
        }
        
        // 计算已使用的预算
        BigDecimal usedAmount = usedAmountByType.getOrDefault(expenseType, BigDecimal.ZERO);
        
        // 如果是更新，且类型未变，则需要减去原有申请的金额
        if (existingExpense != null && expenseType.equals(existingExpense.getType())) {
            usedAmount = usedAmount.subtract(existingExpense.getAmount());
        }
        
        // 计算剩余预算和申请后的使用预算
        BigDecimal remainingBudget = budgetAmount.subtract(usedAmount);
        BigDecimal newUsedAmount = usedAmount.add(amount);
        
        // 检查是否超出预算
        if (amount.compareTo(remainingBudget) > 0) {
            throw new RuntimeException("申请金额 [" + amount + "] 超过项目预算科目 [" + categoryName + "] 的剩余预算 [" + remainingBudget + "]");
        }
        
        // 检查是否超过总预算
        if (newUsedAmount.compareTo(budgetAmount) > 0) {
            throw new RuntimeException("申请后该科目总使用金额 [" + newUsedAmount + "] 将超过预算金额 [" + budgetAmount + "]");
        }
    }
    
    @Override
    @Transactional
    public ExpenseDTO auditExpense(Long id, String status, String comment) {
        // 检查经费申请是否存在
        ExpenseDTO existingExpense = getExpenseById(id);
        if (existingExpense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 如果状态为已批准，需要检查项目剩余预算是否足够
        if ("approved".equals(status)) {
            // 获取项目信息
            ProjectDTO project;
            try {
                project = projectService.getProjectById(existingExpense.getProjectId());
                if (project == null) {
                    throw new RuntimeException("项目不存在");
                }
            } catch (Exception e) {
                throw new RuntimeException("获取项目信息失败: " + e.getMessage());
            }
            
            // 计算审批此申请后的已用经费
            java.math.BigDecimal newUsedBudget = project.getUsedBudget().add(existingExpense.getAmount());
            
            // 检查是否超过总预算
            if (newUsedBudget.compareTo(project.getBudget()) > 0) {
                java.math.BigDecimal remainingBudget = project.getBudget().subtract(project.getUsedBudget());
                throw new RuntimeException("经费申请金额超过项目剩余预算，无法批准。剩余预算：" + remainingBudget);
            }
        }
        
        // 获取当前用户信息
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        Long currentUserId = currentUser.getId();
        String currentUserName = currentUser.getRealName();
        
        // 更新审核状态
        expenseMapper.updateStatus(id, status, currentUserId, currentUserName, comment);
        
        return getExpenseById(id);
    }
    
    @Override
    public ExpenseDTO getExpenseById(Long id) {
        // 查询经费申请
        ExpenseDTO expense = expenseMapper.selectById(id);
        if (expense == null) {
            return null;
        }
        
        // 查询附件
        List<AttachmentDTO> attachments = attachmentMapper.selectByExpenseId(id);
        expense.setAttachments(attachments);
        
        return expense;
    }
    
    @Override
    public PageDTO<ExpenseDTO> getCurrentUserExpenses(Map<String, Object> params, int pageNum, int pageSize) {
        // 获取当前用户ID
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        Long currentUserId = currentUser.getId();
        
        // 查询当前用户的所有经费申请
        List<ExpenseDTO> allExpenses = expenseMapper.selectByUserId(currentUserId);
        
        // 筛选符合条件的经费申请
        List<ExpenseDTO> filteredExpenses = filterExpenses(allExpenses, params);
        
        // 分页处理
        return paginateExpenses(filteredExpenses, pageNum, pageSize);
    }
    
    @Override
    public PageDTO<ExpenseDTO> getAllExpenses(Map<String, Object> params, int pageNum, int pageSize) {
        // 查询所有经费申请
        List<ExpenseDTO> allExpenses = expenseMapper.selectAll();
        
        // 筛选符合条件的经费申请
        List<ExpenseDTO> filteredExpenses = filterExpenses(allExpenses, params);
        
        // 分页处理
        return paginateExpenses(filteredExpenses, pageNum, pageSize);
    }
    
    @Override
    @Transactional
    public void deleteExpense(Long id) {
        // 检查经费申请是否存在
        ExpenseDTO existingExpense = getExpenseById(id);
        if (existingExpense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 删除附件
        attachmentMapper.deleteByExpenseId(id);
        
        // 删除经费申请
        expenseMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public ExpenseDTO payExpense(Long id) {
        // 检查经费申请是否存在
        ExpenseDTO expense = getExpenseById(id);
        if (expense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 检查经费申请状态是否为已批准
        if (!"approved".equals(expense.getStatus())) {
            throw new RuntimeException("只有已批准的经费申请才能进行支付操作");
        }
        
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(expense.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 计算支付此申请后的已用经费
        java.math.BigDecimal newUsedBudget = project.getUsedBudget().add(expense.getAmount());
        
        // 检查是否超过总预算
        if (newUsedBudget.compareTo(project.getBudget()) > 0) {
            throw new RuntimeException("经费申请金额超过项目剩余预算，无法支付");
        }
        
        // 更新经费申请状态
        String newStatus = "paid";
        
        // 如果是借款类型，直接设置为待提交报销凭证状态
        if ("advance".equals(expense.getCategory())) {
            newStatus = "receipt_pending";
        }
        
        expenseMapper.updateStatus(id, newStatus, null, null, "系统自动更新：经费已支付" + 
                                  ("receipt_pending".equals(newStatus) ? "，请在项目结束前提交报销凭证" : ""));
        
        // 更新项目已使用经费
        projectMapper.updateUsedBudget(expense.getProjectId(), expense.getAmount());
        

        // 查询所有已支付的借款类型经费申请
        checkAdvanceExpensesStatus();
        // 返回更新后的经费申请
        return getExpenseById(id);
    }
    
    @Override
    @Transactional
    public ExpenseDTO updateToReceiptPending(Long id) {
        // 检查经费申请是否存在
        ExpenseDTO expense = getExpenseById(id);
        if (expense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 检查是否为借款类型且已支付状态
        if (!"advance".equals(expense.getCategory())) {
            throw new RuntimeException("只有借款类型的经费申请才能更新为待提交报销凭证状态");
        }
        
        if (!"paid".equals(expense.getStatus())) {
            throw new RuntimeException("只有已支付状态的借款申请才能更新为待提交报销凭证状态");
        }
        
        // 更新状态为待提交报销凭证
        expenseMapper.updateStatus(id, "receipt_pending", null, null, "系统自动更新：请在项目结束前提交报销凭证");
        
        return getExpenseById(id);
    }
    
    @Override
    @Transactional
    public ExpenseDTO updateToRepaymentPending(Long id) {
        // 检查经费申请是否存在
        ExpenseDTO expense = getExpenseById(id);
        if (expense == null) {
            throw new RuntimeException("经费申请不存在");
        }
        
        // 检查是否为借款类型且待提交报销凭证状态
        if (!"advance".equals(expense.getCategory())) {
            throw new RuntimeException("只有借款类型的经费申请才能更新为负责人自行还款状态");
        }
        
        if (!"receipt_pending".equals(expense.getStatus())) {
            throw new RuntimeException("只有待提交报销凭证状态的借款申请才能更新为负责人自行还款状态");
        }
        
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(expense.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 更新状态为负责人自行还款
        expenseMapper.updateStatus(id, "repayment_pending", null, null, 
            "系统自动更新：项目已结束，未提交报销凭证，请负责人自行还款");
        
        return getExpenseById(id);
    }
    
    @Override
    public void checkAdvanceExpensesStatus() {
        logger.info("开始检查已支付的借款类型经费申请");
        // 查询所有已支付的借款类型经费申请
        List<ExpenseDTO> paidAdvances = expenseMapper.selectPaidAdvanceExpenses();
        
        logger.info("找到 {} 条已支付的借款申请", paidAdvances.size());
        // 将所有已支付的借款更新为待提交报销凭证状态
        for (ExpenseDTO expense : paidAdvances) {
            try {
                updateToReceiptPending(expense.getId());
                logger.info("已将经费申请ID {} 更新为待提交报销凭证状态", expense.getId());
            } catch (Exception e) {
                logger.error("更新经费申请ID {} 状态失败: {}", expense.getId(), e.getMessage());
            }
        }
    }
    
    @Override
    public void checkReceiptPendingExpenses() {
        logger.info("开始检查待提交报销凭证的经费申请");
        // 查询所有待提交报销凭证的经费申请
        List<ExpenseDTO> receiptPendingExpenses = expenseMapper.selectReceiptPendingExpenses();
        
        logger.info("找到 {} 条待提交报销凭证的借款申请", receiptPendingExpenses.size());
        // 获取当前日期
        LocalDate today = LocalDate.now();
        
        // 检查项目是否已结束，如果已结束，更新为负责人自行还款状态
        for (ExpenseDTO expense : receiptPendingExpenses) {
            try {
                // 直接使用查询结果中的项目结束日期
                if (expense.getProjectEndDate() != null && today.isAfter(expense.getProjectEndDate())) {
                    updateToRepaymentPending(expense.getId());
                    logger.info("已将经费申请ID {} 更新为负责人自行还款状态，因为项目已结束（结束日期：{}）", 
                               expense.getId(), expense.getProjectEndDate());
                } else {
                    logger.debug("经费申请ID {} 对应项目未结束或结束日期为空（结束日期：{}）", 
                               expense.getId(), expense.getProjectEndDate());
                }
            } catch (Exception e) {
                logger.error("更新经费申请ID {} 状态失败: {}", expense.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 根据查询参数筛选经费申请
     * @param expenses 经费申请列表
     * @param params 查询参数
     * @return 筛选后的经费申请列表
     */
    private List<ExpenseDTO> filterExpenses(List<ExpenseDTO> expenses, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return expenses;
        }
        
        return expenses.stream()
                .filter(expense -> {
                    boolean match = true;
                    
                    // 按标题筛选
                    if (params.containsKey("title")) {
                        String title = (String) params.get("title");
                        if (title != null && !title.isEmpty()) {
                            match = match && expense.getTitle().contains(title);
                        }
                    }
                    
                    // 按项目ID筛选
                    if (params.containsKey("projectId")) {
                        Object projectIdObj = params.get("projectId");
                        if (projectIdObj != null) {
                            Long projectId = null;
                            if (projectIdObj instanceof String) {
                                try {
                                    projectId = Long.parseLong((String) projectIdObj);
                                } catch (NumberFormatException e) {
                                    // 忽略转换错误
                                }
                            } else if (projectIdObj instanceof Number) {
                                projectId = ((Number) projectIdObj).longValue();
                            }
                            
                            if (projectId != null) {
                                match = match && expense.getProjectId().equals(projectId);
                            }
                        }
                    }
                    
                    // 按类型筛选
                    if (params.containsKey("type")) {
                        String type = (String) params.get("type");
                        if (type != null && !type.isEmpty()) {
                            match = match && expense.getType().equals(type);
                        }
                    }
                    
                    // 按状态筛选
                    if (params.containsKey("status")) {
                        String status = (String) params.get("status");
                        if (status != null && !status.isEmpty()) {
                            match = match && expense.getStatus().equals(status);
                        }
                    }
                    
                    // 按类别筛选
                    if (params.containsKey("category")) {
                        String category = (String) params.get("category");
                        if (category != null && !category.isEmpty()) {
                            match = match && expense.getCategory().equals(category);
                        }
                    }
                    
                    return match;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 分页处理经费申请列表
     * @param expenses 经费申请列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费申请列表
     */
    private PageDTO<ExpenseDTO> paginateExpenses(List<ExpenseDTO> expenses, int pageNum, int pageSize) {
        // 计算总记录数
        long total = expenses.size();
        
        // 分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            // 如果起始索引超出总数，返回空列表
            return new PageDTO<>(new ArrayList<>(), total, pageNum, pageSize);
        }
        
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<ExpenseDTO> pagedExpenses = expenses.subList(fromIndex, toIndex);
        
        // 为每个经费申请查询附件
        for (ExpenseDTO expense : pagedExpenses) {
            List<AttachmentDTO> attachments = attachmentMapper.selectByExpenseId(expense.getId());
            expense.setAttachments(attachments);
        }
        
        // 返回分页结果
        return new PageDTO<>(pagedExpenses, total, pageNum, pageSize);
    }

    /**
     * 获取项目的经费支出列表
     * @param projectId 项目ID
     * @return 经费支出列表
     */
    @Override
    public List<ExpenseDTO> getExpensesByProjectId(Long projectId) {
        if (projectId == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        
        // 调用Mapper查询数据库
        return expenseMapper.selectByProjectId(projectId);
    }
} 