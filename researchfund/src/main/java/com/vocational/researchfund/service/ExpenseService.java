package com.vocational.researchfund.service;

import com.vocational.researchfund.dto.ExpenseDTO;
import com.vocational.researchfund.dto.PageDTO;

import java.util.Map;

/**
 * 经费申请服务接口
 */
public interface ExpenseService {
    
    /**
     * 创建经费申请
     * @param expenseDTO 经费申请DTO
     * @return 创建后的经费申请
     */
    ExpenseDTO createExpense(ExpenseDTO expenseDTO);
    
    /**
     * 更新经费申请
     * @param id 经费申请ID
     * @param expenseDTO 经费申请DTO
     * @return 更新后的经费申请
     */
    ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO);
    
    /**
     * 审核经费申请
     * @param id 经费申请ID
     * @param status 审核状态
     * @param comment 审核意见
     * @return 审核结果，成功时包含经费申请详情
     */
    ExpenseDTO auditExpense(Long id, String status, String comment);
    
    /**
     * 获取经费申请详情
     * @param id 经费申请ID
     * @return 经费申请详情
     */
    ExpenseDTO getExpenseById(Long id);
    
    /**
     * 获取当前用户的经费申请列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费申请列表
     */
    PageDTO<ExpenseDTO> getCurrentUserExpenses(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 获取所有经费申请列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费申请列表
     */
    PageDTO<ExpenseDTO> getAllExpenses(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 删除经费申请
     * @param id 经费申请ID
     */
    void deleteExpense(Long id);
    
    /**
     * 支付经费申请
     * @param id 经费申请ID
     * @return 支付后的经费申请
     */
    ExpenseDTO payExpense(Long id);

    /**
     * 更新借款类型经费申请的状态为待提交报销凭证
     * @param id 经费申请ID
     * @return 更新后的经费申请
     */
    ExpenseDTO updateToReceiptPending(Long id);

    /**
     * 更新经费申请状态为负责人自行还款
     * @param id 经费申请ID
     * @return 更新后的经费申请
     */
    ExpenseDTO updateToRepaymentPending(Long id);

    /**
     * 检查所有已支付的借款申请，将满足条件的转为待提交报销凭证
     * 此方法由定时任务调用
     */
    void checkAdvanceExpensesStatus();

    /**
     * 检查所有待提交报销凭证的申请，对已超过项目结束时间的转为负责人自行还款
     * 此方法由定时任务调用
     */
    void checkReceiptPendingExpenses();
} 