package com.vocational.researchfund.service;

import com.vocational.researchfund.dto.FundTransferDTO;
import com.vocational.researchfund.dto.PageDTO;

import java.util.Map;

/**
 * 经费结转服务接口
 */
public interface FundTransferService {
    
    /**
     * 创建经费结转
     * @param transferDTO 经费结转DTO
     * @return 创建后的经费结转
     */
    FundTransferDTO createTransfer(FundTransferDTO transferDTO);
    
    /**
     * 更新经费结转
     * @param id 经费结转ID
     * @param transferDTO 经费结转DTO
     * @return 更新后的经费结转
     */
    FundTransferDTO updateTransfer(Long id, FundTransferDTO transferDTO);
    
    /**
     * 审核经费结转
     * @param id 经费结转ID
     * @param status 审核状态
     * @param comment 审核意见
     * @return 审核后的经费结转
     */
    FundTransferDTO auditTransfer(Long id, String status, String comment);
    
    /**
     * 获取经费结转详情
     * @param id 经费结转ID
     * @return 经费结转详情
     */
    FundTransferDTO getTransferById(Long id);
    
    /**
     * 获取当前用户的经费结转列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费结转列表
     */
    PageDTO<FundTransferDTO> getCurrentUserTransfers(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 获取所有经费结转列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费结转列表
     */
    PageDTO<FundTransferDTO> getAllTransfers(Map<String, Object> params, int pageNum, int pageSize);
    
    /**
     * 删除经费结转
     * @param id 经费结转ID
     */
    void deleteTransfer(Long id);
} 