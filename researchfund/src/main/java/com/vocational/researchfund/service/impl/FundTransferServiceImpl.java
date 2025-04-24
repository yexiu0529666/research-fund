package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.dto.FundTransferDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.FundTransferMapper;
import com.vocational.researchfund.service.FundTransferService;
import com.vocational.researchfund.service.ProjectService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Calendar;

/**
 * 经费结转服务实现类
 */
@Service
public class FundTransferServiceImpl implements FundTransferService {
    
    @Autowired
    private FundTransferMapper fundTransferMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Override
    @Transactional
    public FundTransferDTO createTransfer(FundTransferDTO transferDTO) {
        // 设置默认状态为待审核
        transferDTO.setStatus("pending");
        
        // 获取申请人信息
        User currentUser = userService.getUserByUsername(SecurityUtils.getCurrentUsername());
        if (currentUser == null) {
            throw new RuntimeException("未找到当前登录用户信息");
        }
        
        // 设置申请人信息
        transferDTO.setApplyUserId(currentUser.getId());
        transferDTO.setApplyUserName(currentUser.getRealName());
        
        // 如果前端没有传入项目名称，但传入了项目ID，则根据项目ID获取项目名称
        if ((transferDTO.getProjectName() == null || transferDTO.getProjectName().isEmpty()) 
                && transferDTO.getProjectId() != null) {
            try {
                ProjectDTO project = projectService.getProjectById(transferDTO.getProjectId());
                transferDTO.setProjectName(project.getName());
            } catch (Exception e) {
                throw new RuntimeException("创建经费结转失败， " + e.getMessage());
            }
        }
        
        // 设置申请日期
        if (transferDTO.getApplyDate() == null) {
            transferDTO.setApplyDate(LocalDate.now());
        }
        
        // 如果未设置年度，默认设置当前年度和下一年度
        if (transferDTO.getFromYear() == null || transferDTO.getFromYear().isEmpty() ||
            transferDTO.getToYear() == null || transferDTO.getToYear().isEmpty()) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            transferDTO.setFromYear(String.valueOf(currentYear));
            transferDTO.setToYear(String.valueOf(currentYear + 1));
        }
        
        // 插入数据库
        fundTransferMapper.insert(transferDTO);
        
        return getTransferById(transferDTO.getId());
    }
    
    @Override
    @Transactional
    public FundTransferDTO updateTransfer(Long id, FundTransferDTO transferDTO) {
        // 检查经费结转是否存在
        FundTransferDTO existingTransfer = fundTransferMapper.selectById(id);
        if (existingTransfer == null) {
            throw new RuntimeException("经费结转不存在");
        }
        
        // 检查状态，只有待审核状态的经费结转才能修改
        if (!"pending".equals(existingTransfer.getStatus())) {
            throw new RuntimeException("只有待审核状态的经费结转才能修改");
        }
        
        // 获取当前用户信息
        User currentUser = userService.getUserByUsername(SecurityUtils.getCurrentUsername());
        if (currentUser == null) {
            throw new RuntimeException("未找到当前登录用户信息");
        }
        
        // 设置ID
        transferDTO.setId(id);
        
        // 保留原有的申请人信息和状态
        transferDTO.setApplyUserId(existingTransfer.getApplyUserId());
        transferDTO.setApplyUserName(existingTransfer.getApplyUserName());
        transferDTO.setStatus(existingTransfer.getStatus());
        
        // 如果前端没有传入项目名称，但传入了项目ID，则根据项目ID获取项目名称
        if ((transferDTO.getProjectName() == null || transferDTO.getProjectName().isEmpty()) 
                && transferDTO.getProjectId() != null) {
            try {
                ProjectDTO project = projectService.getProjectById(transferDTO.getProjectId());
                transferDTO.setProjectName(project.getName());
            } catch (Exception e) {
                throw new RuntimeException("更新经费结转失败， " + e.getMessage());
            }
        }
        
        // 更新数据库
        fundTransferMapper.update(transferDTO);
        
        return getTransferById(id);
    }
    
    @Override
    @Transactional
    public FundTransferDTO auditTransfer(Long id, String status, String comment) {
        // 检查经费结转是否存在
        FundTransferDTO transfer = fundTransferMapper.selectById(id);
        if (transfer == null) {
            throw new RuntimeException("经费结转不存在");
        }
        
        // 检查状态是否为待审核
        if (!"pending".equals(transfer.getStatus())) {
            throw new RuntimeException("只有待审核状态的经费结转才能审核");
        }
        
        // 检查审核状态参数
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new RuntimeException("无效的审核状态");
        }
        
        // 获取当前用户
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        
        // 如果审核通过，先检查项目的剩余经费是否足够
        if ("approved".equals(status)) {
            try {
                // 获取项目信息
                ProjectDTO project = projectService.getProjectById(transfer.getProjectId());
                if (project == null) {
                    throw new RuntimeException("项目不存在，无法更新经费结余");
                }
                
                // 检查剩余经费是否充足
                // 计算项目剩余预算 = 总预算 - 已用预算
                java.math.BigDecimal remainingBudget = project.getBudget().subtract(project.getUsedBudget());
                
                // 判断剩余预算是否足够支付结转金额
                if (remainingBudget.compareTo(transfer.getAmount()) < 0) {
                    throw new RuntimeException("项目剩余经费不足，无法进行结转。项目剩余预算：" + remainingBudget + "，结转申请金额：" + transfer.getAmount());
                }
            } catch (Exception e) {
                if (e.getMessage().contains("剩余经费不足")) {
                    throw e; // 直接重新抛出剩余经费不足的异常
                } else {
                    throw new RuntimeException("检查项目经费失败: " + e.getMessage());
                }
            }
        }
        
        // 更新审核状态
        fundTransferMapper.updateStatus(id, status, currentUser.getId(), currentUser.getRealName(), comment);
        
        // 如果审核通过，更新项目的经费结余
        if ("approved".equals(status)) {
            try {
                // 获取项目信息
                ProjectDTO project = projectService.getProjectById(transfer.getProjectId());
                
                // 更新项目已使用预算，增加结转金额
                projectService.updateUsedBudget(project.getId(), transfer.getAmount());
                
                System.out.println(String.format("项目ID=%d, 名称=%s 的经费结余已更新，结转金额: %s", 
                    project.getId(), project.getName(), transfer.getAmount()));
            } catch (Exception e) {
                throw new RuntimeException("更新项目经费结余失败: " + e.getMessage());
            }
        }
        
        return getTransferById(id);
    }
    
    @Override
    public FundTransferDTO getTransferById(Long id) {
        return fundTransferMapper.selectById(id);
    }
    
    @Override
    public PageDTO<FundTransferDTO> getCurrentUserTransfers(Map<String, Object> params, int pageNum, int pageSize) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 查询当前用户的所有经费结转
        List<FundTransferDTO> transfers = fundTransferMapper.selectByUserId(userId);
        
        // 过滤
        transfers = filterTransfers(transfers, params);
        
        // 分页
        return paginateTransfers(transfers, pageNum, pageSize);
    }
    
    @Override
    public PageDTO<FundTransferDTO> getAllTransfers(Map<String, Object> params, int pageNum, int pageSize) {
        // 查询所有经费结转
        List<FundTransferDTO> transfers = fundTransferMapper.selectAll();
        
        // 过滤
        transfers = filterTransfers(transfers, params);
        
        // 分页
        return paginateTransfers(transfers, pageNum, pageSize);
    }
    
    @Override
    @Transactional
    public void deleteTransfer(Long id) {
        // 检查经费结转是否存在
        FundTransferDTO existingTransfer = getTransferById(id);
        if (existingTransfer == null) {
            throw new RuntimeException("经费结转不存在");
        }
        
        // 删除经费结转
        fundTransferMapper.deleteById(id);
    }
    
    /**
     * 根据参数过滤经费结转列表
     * @param transfers 经费结转列表
     * @param params 查询参数
     * @return 过滤后的经费结转列表
     */
    private List<FundTransferDTO> filterTransfers(List<FundTransferDTO> transfers, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return transfers;
        }
        
        // 使用Stream API过滤
        return transfers.stream().filter(transfer -> {
            // 按标题过滤
            if (params.containsKey("title") && params.get("title") != null) {
                String title = params.get("title").toString();
                if (!transfer.getTitle().contains(title)) {
                    return false;
                }
            }
            
            // 按项目ID过滤
            if (params.containsKey("projectId") && params.get("projectId") != null) {
                String projectId = params.get("projectId").toString();
                if (!projectId.equals(transfer.getProjectId().toString())) {
                    return false;
                }
            }
            
            // 按状态过滤
            if (params.containsKey("status") && params.get("status") != null) {
                String status = params.get("status").toString();
                if (!status.isEmpty() && !status.equals(transfer.getStatus())) {
                    return false;
                }
            }
            
            // 按结转年度过滤
            if (params.containsKey("fromYear") && params.get("fromYear") != null) {
                String fromYear = params.get("fromYear").toString();
                if (!fromYear.equals(transfer.getFromYear())) {
                    return false;
                }
            }
            
            if (params.containsKey("toYear") && params.get("toYear") != null) {
                String toYear = params.get("toYear").toString();
                if (!toYear.equals(transfer.getToYear())) {
                    return false;
                }
            }
            
            return true;
        }).collect(Collectors.toList());
    }
    
    /**
     * 分页处理经费结转列表
     * @param transfers 经费结转列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页后的经费结转列表
     */
    private PageDTO<FundTransferDTO> paginateTransfers(List<FundTransferDTO> transfers, int pageNum, int pageSize) {
        // 计算总记录数
        long total = transfers.size();
        
        // 计算起始索引和结束索引
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, transfers.size());
        
        // 如果起始索引超出范围，返回空列表
        if (fromIndex >= transfers.size()) {
            return new PageDTO<>(new ArrayList<>(), total, pageNum, pageSize);
        }
        
        // 提取分页数据
        List<FundTransferDTO> pageData = transfers.subList(fromIndex, toIndex);
        
        // 创建并返回分页对象
        return new PageDTO<>(pageData, total, pageNum, pageSize);
    }
} 