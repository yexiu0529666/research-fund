package com.vocational.researchfund.service.impl;

import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.dto.ProjectFundArrivalDTO;
import com.vocational.researchfund.entity.ProjectFundArrival;
import com.vocational.researchfund.mapper.ProjectFundArrivalMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.service.ProjectFundArrivalService;
import com.vocational.researchfund.service.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目经费到账服务实现类
 */
@Service
public class ProjectFundArrivalServiceImpl implements ProjectFundArrivalService {

    @Autowired
    private ProjectFundArrivalMapper projectFundArrivalMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectService projectService;

    /**
     * 创建项目经费到账记录
     * @param fundArrivalDTO 经费到账DTO
     * @return 创建后的经费到账记录
     */
    @Override
    @Transactional
    public ProjectFundArrivalDTO createFundArrival(ProjectFundArrivalDTO fundArrivalDTO) {
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(fundArrivalDTO.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 验证项目状态是否为进行中
        if (!"active".equals(project.getStatus())) {
            throw new RuntimeException("只有进行中的项目可以添加到账信息");
        }
        
        // 验证到账凭证是否上传
        if (fundArrivalDTO.getVoucherPath() == null || fundArrivalDTO.getVoucherPath().trim().isEmpty()) {
            throw new RuntimeException("请上传到账凭证");
        }
        
        // 获取项目已到账总金额
        BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(fundArrivalDTO.getProjectId());
        
        // 验证总到账金额是否超过项目总预算
        if (totalArrivedAmount.add(fundArrivalDTO.getAmount()).compareTo(project.getBudget()) > 0) {
            throw new RuntimeException("到账金额超过项目总预算");
        }
        
        // 创建实体并保存
        ProjectFundArrival fundArrival = new ProjectFundArrival();
        fundArrival.setProjectId(fundArrivalDTO.getProjectId());
        fundArrival.setProjectName(project.getName());
        fundArrival.setFundingSource(fundArrivalDTO.getFundingSource());
        fundArrival.setAmount(fundArrivalDTO.getAmount());
        fundArrival.setArrivalDate(fundArrivalDTO.getArrivalDate() != null ? 
                                   fundArrivalDTO.getArrivalDate() : LocalDate.now());
        fundArrival.setVoucherPath(fundArrivalDTO.getVoucherPath());
        fundArrival.setRemark(fundArrivalDTO.getRemark());
        
        projectFundArrivalMapper.insert(fundArrival);
        
        // 更新DTO并返回
        BeanUtils.copyProperties(fundArrival, fundArrivalDTO);
        fundArrivalDTO.setProjectBudget(project.getBudget());
        fundArrivalDTO.setTotalArrivedAmount(totalArrivedAmount.add(fundArrivalDTO.getAmount()));
        fundArrivalDTO.setPendingAmount(project.getBudget().subtract(fundArrivalDTO.getTotalArrivedAmount()));
        
        return fundArrivalDTO;
    }

    /**
     * 更新项目经费到账记录
     * @param id 记录ID
     * @param fundArrivalDTO 经费到账DTO
     * @return 更新后的经费到账记录
     */
    @Override
    @Transactional
    public ProjectFundArrivalDTO updateFundArrival(Long id, ProjectFundArrivalDTO fundArrivalDTO) {
        ProjectFundArrival existingFundArrival = projectFundArrivalMapper.selectById(id);
        if (existingFundArrival == null) {
            throw new RuntimeException("经费到账记录不存在");
        }
        
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(existingFundArrival.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        // 验证项目状态是否为进行中
        if (!"active".equals(project.getStatus())) {
            throw new RuntimeException("只有进行中的项目可以修改到账信息");
        }
        
        // 验证到账凭证是否上传
        if (fundArrivalDTO.getVoucherPath() == null || fundArrivalDTO.getVoucherPath().trim().isEmpty()) {
            throw new RuntimeException("请上传到账凭证");
        }
        
        // 计算已到账总金额（不包含当前记录）
        BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(existingFundArrival.getProjectId())
                                       .subtract(existingFundArrival.getAmount());
        
        // 验证总到账金额是否超过项目总预算
        if (totalArrivedAmount.add(fundArrivalDTO.getAmount()).compareTo(project.getBudget()) > 0) {
            throw new RuntimeException("到账金额超过项目总预算");
        }
        
        // 更新实体
        existingFundArrival.setFundingSource(fundArrivalDTO.getFundingSource());
        existingFundArrival.setAmount(fundArrivalDTO.getAmount());
        existingFundArrival.setArrivalDate(fundArrivalDTO.getArrivalDate());
        existingFundArrival.setVoucherPath(fundArrivalDTO.getVoucherPath());
        existingFundArrival.setRemark(fundArrivalDTO.getRemark());
        
        projectFundArrivalMapper.update(existingFundArrival);
        
        // 更新DTO并返回
        BeanUtils.copyProperties(existingFundArrival, fundArrivalDTO);
        fundArrivalDTO.setProjectBudget(project.getBudget());
        fundArrivalDTO.setTotalArrivedAmount(totalArrivedAmount.add(fundArrivalDTO.getAmount()));
        fundArrivalDTO.setPendingAmount(project.getBudget().subtract(fundArrivalDTO.getTotalArrivedAmount()));
        
        return fundArrivalDTO;
    }

    /**
     * 根据ID获取项目经费到账记录
     * @param id 记录ID
     * @return 经费到账记录
     */
    @Override
    public ProjectFundArrivalDTO getFundArrivalById(Long id) {
        ProjectFundArrival fundArrival = projectFundArrivalMapper.selectById(id);
        if (fundArrival == null) {
            return null;
        }
        
        ProjectFundArrivalDTO fundArrivalDTO = new ProjectFundArrivalDTO();
        BeanUtils.copyProperties(fundArrival, fundArrivalDTO);
        
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(fundArrival.getProjectId());
        if (project != null) {
            fundArrivalDTO.setProjectBudget(project.getBudget());
            
            // 获取已到账总金额
            BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(fundArrival.getProjectId());
            fundArrivalDTO.setTotalArrivedAmount(totalArrivedAmount);
            fundArrivalDTO.setPendingAmount(project.getBudget().subtract(totalArrivedAmount));
        }
        
        return fundArrivalDTO;
    }

    /**
     * 获取项目经费到账记录列表
     * @param params 查询参数
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页后的经费到账记录列表
     */
    @Override
    public PageDTO<ProjectFundArrivalDTO> getFundArrivalList(Map<String, Object> params, int pageNum, int pageSize) {
        List<ProjectFundArrival> allFundArrivals;
        
        // 根据条件查询数据
        if (params != null && params.containsKey("projectId")) {
            Long projectId = Long.valueOf(params.get("projectId").toString());
            allFundArrivals = projectFundArrivalMapper.selectByProjectId(projectId);
        } else {
            allFundArrivals = projectFundArrivalMapper.selectList();
        }
        
        // 计算总记录数
        long total = allFundArrivals.size();
        
        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            // 如果起始索引超出总数，返回空列表
            return new PageDTO<>(new ArrayList<>(), total, pageNum, pageSize);
        }
        
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<ProjectFundArrival> pagedFundArrivals = allFundArrivals.subList(fromIndex, toIndex);
        
        // 转换为DTO
        List<ProjectFundArrivalDTO> fundArrivalDTOList = pagedFundArrivals.stream().map(fundArrival -> {
            ProjectFundArrivalDTO dto = new ProjectFundArrivalDTO();
            BeanUtils.copyProperties(fundArrival, dto);
            
            // 获取项目信息
            ProjectDTO project = projectService.getProjectById(fundArrival.getProjectId());
            if (project != null) {
                dto.setProjectBudget(project.getBudget());
                
                // 获取已到账总金额
                BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(fundArrival.getProjectId());
                dto.setTotalArrivedAmount(totalArrivedAmount);
                dto.setPendingAmount(project.getBudget().subtract(totalArrivedAmount));
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 创建分页DTO
        PageDTO<ProjectFundArrivalDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(fundArrivalDTOList);
        pageDTO.setTotal(total);
        pageDTO.setPageNum(pageNum);
        pageDTO.setPageSize(pageSize);
        pageDTO.setPages((int) Math.ceil((double) total / pageSize));
        
        return pageDTO;
    }

    /**
     * 获取项目的经费到账记录列表
     * @param projectId 项目ID
     * @return 经费到账记录列表
     */
    @Override
    public List<ProjectFundArrivalDTO> getFundArrivalsByProjectId(Long projectId) {
        List<ProjectFundArrival> fundArrivals = projectFundArrivalMapper.selectByProjectId(projectId);
        
        // 获取项目信息
        ProjectDTO project = projectService.getProjectById(projectId);
        if (project == null) {
            return new ArrayList<>();
        }
        
        // 获取已到账总金额
        BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(projectId);
        
        return fundArrivals.stream().map(fundArrival -> {
            ProjectFundArrivalDTO dto = new ProjectFundArrivalDTO();
            BeanUtils.copyProperties(fundArrival, dto);
            dto.setProjectBudget(project.getBudget());
            dto.setTotalArrivedAmount(totalArrivedAmount);
            dto.setPendingAmount(project.getBudget().subtract(totalArrivedAmount));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 删除项目经费到账记录
     * @param id 记录ID
     */
    @Override
    @Transactional
    public void deleteFundArrival(Long id) {
        ProjectFundArrival fundArrival = projectFundArrivalMapper.selectById(id);
        if (fundArrival == null) {
            throw new RuntimeException("经费到账记录不存在");
        }
        
        // 获取项目信息，验证项目状态
        ProjectDTO project = projectService.getProjectById(fundArrival.getProjectId());
        if (project != null && !"active".equals(project.getStatus())) {
            throw new RuntimeException("只有进行中的项目可以删除到账信息");
        }
        
        projectFundArrivalMapper.deleteById(id);
    }

    /**
     * 获取项目已到账的总金额
     * @param projectId 项目ID
     * @return 已到账总金额
     */
    @Override
    public BigDecimal getProjectTotalArrivedAmount(Long projectId) {
        return projectFundArrivalMapper.getTotalArrivedAmount(projectId);
    }

    /**
     * 获取项目各经费来源的到账金额统计
     * @param projectId 项目ID
     * @return 各经费来源的到账金额统计
     */
    @Override
    public List<Map<String, Object>> getProjectFundArrivalBySource(Long projectId) {
        List<Map<String, Object>> sourceStats = projectFundArrivalMapper.getFundArrivalBySource(projectId);
        
        // 增强数据，添加经费来源的标签
        return sourceStats.stream().map(stat -> {
            Map<String, Object> enhancedStat = new HashMap<>(stat);
            String source = (String) stat.get("funding_source");
            String label = getFundingSourceLabel(source);
            enhancedStat.put("label", label);
            return enhancedStat;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取经费来源的标签
     * @param source 经费来源
     * @return 经费来源的标签
     */
    private String getFundingSourceLabel(String source) {
        if ("fiscal".equals(source)) {
            return "财政经费";
        } else if ("school".equals(source)) {
            return "校配套经费";
        } else if ("other".equals(source)) {
            return "其他经费";
        }
        return source;
    }

    /**
     * 获取可以添加经费到账的项目列表（进行中的项目）
     * @return 可添加经费到账的项目列表
     */
    @Override
    public List<Map<String, Object>> getAvailableProjectsForFundArrival() {
        List<ProjectDTO> activeProjects = projectService.getProjectsByStatus("active");
        
        return activeProjects.stream().map(project -> {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("id", project.getId());
            projectMap.put("name", project.getName());
            projectMap.put("budget", project.getBudget());
            
            // 获取已到账总金额
            BigDecimal totalArrivedAmount = getProjectTotalArrivedAmount(project.getId());
            projectMap.put("totalArrivedAmount", totalArrivedAmount);
            projectMap.put("pendingAmount", project.getBudget().subtract(totalArrivedAmount));
            
            // 添加经费来源信息
            if (project.getFundingSources() != null && !project.getFundingSources().isEmpty()) {
                projectMap.put("fundingSources", project.getFundingSources());
            } else if (project.getFundingSource() != null) {
                // 兼容旧数据，如果没有fundingSources但有fundingSource
                projectMap.put("fundingSources", Collections.singletonList(project.getFundingSource()));
            }
            projectMap.put("fundingSource", project.getFundingSource());
            
            return projectMap;
        }).collect(Collectors.toList());
    }
} 