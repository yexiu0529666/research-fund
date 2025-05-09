package com.vocational.researchfund.service.impl;


import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.dto.ProjectDTO;
import com.vocational.researchfund.dto.ProjectDTO.BudgetItemDTO;
import com.vocational.researchfund.dto.ProjectDTO.TeamMemberDTO;
import com.vocational.researchfund.entity.Project;
import com.vocational.researchfund.entity.ProjectBudgetItem;
import com.vocational.researchfund.entity.ProjectFundingSource;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.exception.BusinessException;
import com.vocational.researchfund.mapper.ProjectBudgetItemMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.mapper.ProjectTeamMemberMapper;
import com.vocational.researchfund.repository.ProjectFundingSourceRepository;
import com.vocational.researchfund.service.ProjectService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectTeamMemberMapper projectTeamMemberMapper;
    
    @Autowired
    private ProjectBudgetItemMapper projectBudgetItemMapper;
    
    @Autowired
    private ProjectFundingSourceRepository projectFundingSourceRepository;
    
    @Autowired
    private UserService userService;

    @Override
    public ProjectDTO getProjectById(Long id) {
        if (id == null) {
            throw new BusinessException("项目ID不能为空");
        }
        
        // 查询项目基本信息
        ProjectDTO projectDTO = projectMapper.selectById(id);
        
        if (projectDTO == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 查询项目团队成员
        List<ProjectDTO.TeamMemberDTO> teamMembers = projectTeamMemberMapper.selectByProjectId(id);
        projectDTO.setTeam(teamMembers);
        
        // 查询项目预算科目
        List<ProjectBudgetItem> budgetItems = projectBudgetItemMapper.selectByProjectId(id);
        if (budgetItems != null && !budgetItems.isEmpty()) {
            projectDTO.setBudgetItems(budgetItems.stream()
                    .map(item -> {
                        ProjectDTO.BudgetItemDTO dto = new ProjectDTO.BudgetItemDTO();
                        dto.setId(item.getId());
                        dto.setCategory(item.getCategory());
                        dto.setAmount(item.getAmount());
                        return dto;
                    })
                    .collect(Collectors.toList()));
        }
        
        // 查询项目经费来源
        List<ProjectFundingSource> fundingSources = projectFundingSourceRepository.findByProjectId(id);
        if (fundingSources != null && !fundingSources.isEmpty()) {
            projectDTO.setFundingSources(fundingSources.stream()
                    .map(ProjectFundingSource::getSource)
                    .collect(Collectors.toList()));
        } else if (projectDTO.getFundingSource() != null) {
            // 兼容旧数据，如果没有查到多个经费来源，但有单个经费来源，则将其转换为列表
            projectDTO.setFundingSources(Collections.singletonList(projectDTO.getFundingSource()));
        }
        
        return projectDTO;
    }

    @Override
    public PageDTO<ProjectDTO> getProjectList(Map<String, Object> params, int pageNum, int pageSize) {
        // 从数据库查询项目列表
        List<ProjectDTO> allProjects = projectMapper.selectList();
        
        // 根据查询参数过滤项目
        List<ProjectDTO> filteredProjects = allProjects;
        if (params != null && !params.isEmpty()) {
            filteredProjects = allProjects.stream()
                    .filter(p -> {
                        boolean match = true;
                        if (params.containsKey("name")) {
                            match = match && p.getName() != null && p.getName().contains((String) params.get("name"));
                        }
                        if (params.containsKey("type")) {
                            match = match && p.getType() != null && p.getType().equals(params.get("type"));
                        }
                        if (params.containsKey("status")) {
                            match = match && p.getStatus() != null && p.getStatus().equals(params.get("status"));
                        }
                        if (params.containsKey("auditStatus")) {
                            match = match && p.getAuditStatus() != null && p.getAuditStatus().equals(params.get("auditStatus"));
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }
        
        // 计算总记录数
        long total = filteredProjects.size();
        
        // 分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            // 如果起始索引超出总数，返回空列表
            return new PageDTO<>(new ArrayList<>(), total, pageNum, pageSize);
        }
        
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<ProjectDTO> pagedProjects = filteredProjects.subList(fromIndex, toIndex);
        
        // 返回分页结果
        return new PageDTO<>(pagedProjects, total, pageNum, pageSize);
    }

    @Override
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        // 设置默认状态
        projectDTO.setStatus("planning");
        projectDTO.setAuditStatus("audit");
        
        // 设置默认值
        if (projectDTO.getUsedBudget() == null) {
            projectDTO.setUsedBudget(BigDecimal.ZERO);
        }
        
        // 处理经费来源，保持向下兼容性
        if (projectDTO.getFundingSources() != null && !projectDTO.getFundingSources().isEmpty()) {
            // 使用第一个经费来源作为主经费来源，保持向下兼容
            String fundingSources = String.join(",", projectDTO.getFundingSources());
            projectDTO.setFundingSource(fundingSources);
        }
        
        // 插入数据库
        int rows = projectMapper.insert(projectDTO);
        
        System.out.println("项目创建结果: " + (rows > 0 ? "成功" : "失败"));
        System.out.println("生成的项目ID: " + projectDTO.getId());
        
        // 保存多个经费来源
        if (projectDTO.getFundingSources() != null && projectDTO.getFundingSources().size() > 0) {
            projectFundingSourceRepository.batchInsert(
                projectDTO.getId(), 
                projectDTO.getFundingSources(),
                SecurityUtils.getCurrentUserId()
            );
        }
        
        // 保存项目团队成员
        if (projectDTO.getTeam() != null && !projectDTO.getTeam().isEmpty()) {
            int teamRows = projectTeamMemberMapper.batchInsert(projectDTO.getTeam(), projectDTO.getId());
            System.out.println("保存项目团队成员数量: " + teamRows);
        }
        
        // 保存项目预算科目
        if (projectDTO.getBudgetItems() != null && !projectDTO.getBudgetItems().isEmpty()) {
            List<ProjectBudgetItem> budgetItems = projectDTO.getBudgetItems().stream()
                    .map(dto -> {
                        ProjectBudgetItem item = new ProjectBudgetItem();
                        item.setProjectId(projectDTO.getId());
                        item.setCategory(dto.getCategory());
                        item.setAmount(dto.getAmount());
                        item.setCreateBy(SecurityUtils.getCurrentUserId());
                        item.setUpdateBy(SecurityUtils.getCurrentUserId());
                        item.setDeleted(0);
                        return item;
                    })
                    .collect(Collectors.toList());
            int budgetRows = projectBudgetItemMapper.batchInsert(budgetItems);
            System.out.println("保存项目预算科目数量: " + budgetRows);
        }
        
        return projectDTO;
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        // 检查项目是否存在
        ProjectDTO existingProject = getProjectById(id);
        
        // 设置ID
        projectDTO.setId(id);
        
        // 保留一些字段不被更新
        projectDTO.setLeaderId(existingProject.getLeaderId());
        projectDTO.setLeaderName(existingProject.getLeaderName());
        projectDTO.setAuditStatus(existingProject.getAuditStatus());
        projectDTO.setUsedBudget(existingProject.getUsedBudget());
        
        // 处理经费来源，保持向下兼容性
        if (projectDTO.getFundingSources() != null && !projectDTO.getFundingSources().isEmpty()) {
            // 使用第一个经费来源作为主经费来源，保持向下兼容
            projectDTO.setFundingSource(projectDTO.getFundingSources().get(0));
        }
        
        // 更新数据库
        projectMapper.update(projectDTO);
        
        // 更新多个经费来源
        if (projectDTO.getFundingSources() != null) {
            // 先删除原有经费来源
            projectFundingSourceRepository.deleteByProjectId(id);
            
            // 再添加新经费来源
            if (!projectDTO.getFundingSources().isEmpty()) {
                projectFundingSourceRepository.batchInsert(
                    id, 
                    projectDTO.getFundingSources(),
                    SecurityUtils.getCurrentUserId()
                );
            }
        }
        
        // 更新项目团队成员
        if (projectDTO.getTeam() != null) {
            // 先删除原有成员
            projectTeamMemberMapper.deleteByProjectId(id);
            
            // 再添加新成员
            if (!projectDTO.getTeam().isEmpty()) {
                projectTeamMemberMapper.batchInsert(projectDTO.getTeam(), id);
            }
        }
        
        // 更新项目预算科目
        if (projectDTO.getBudgetItems() != null) {
            // 先删除原有预算科目
            projectBudgetItemMapper.deleteByProjectId(id);
            
            // 再添加新预算科目
            if (!projectDTO.getBudgetItems().isEmpty()) {
                List<ProjectBudgetItem> budgetItems = projectDTO.getBudgetItems().stream()
                        .map(dto -> {
                            ProjectBudgetItem item = new ProjectBudgetItem();
                            item.setProjectId(id);
                            item.setCategory(dto.getCategory());
                            item.setAmount(dto.getAmount());
                            item.setCreateBy(SecurityUtils.getCurrentUserId());
                            item.setUpdateBy(SecurityUtils.getCurrentUserId());
                            item.setDeleted(0);
                            return item;
                        })
                        .collect(Collectors.toList());
                projectBudgetItemMapper.batchInsert(budgetItems);
            }
        }
        
        return projectDTO;
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        // 检查项目是否存在
        getProjectById(id);
        
        // 删除项目团队成员
        projectTeamMemberMapper.deleteByProjectId(id);
        
        // 删除项目预算科目
        projectBudgetItemMapper.deleteByProjectId(id);
        
        // 逻辑删除项目
        projectMapper.deleteById(id);
    }

    @Override
    @Transactional
    public ProjectDTO auditProject(Long id, String auditStatus, String comment) {
        // 获取项目
        ProjectDTO project = getProjectById(id);
        
        // 更新审核状态
        project.setAuditStatus(auditStatus);


        // 如果审核未通过，项目状态改为已暂停
        if ("rejected".equals(auditStatus)) {
            project.setStatus("suspended");
        }else {
            project.setStatus("active");
        }
        
        // 更新数据库
        projectMapper.update(project);
        
        return project;
    }

    @Override
    @Transactional
    public ProjectDTO confirmProject(Long id) {
        // 获取项目信息
        ProjectDTO project = getProjectById(id);
        
        // 校验项目状态
        if (!"planning".equals(project.getStatus())) {
            throw new RuntimeException("只有筹划中的项目可以确认立项");
        }
        
        if (!"audit".equals(project.getAuditStatus())) {
            throw new RuntimeException("该项目已提交审核，无法重复操作");
        }
        

        project.setAuditStatus("pending");
        
        // 更新数据库
        projectMapper.update(project);
        
        // 返回更新后的项目
        return getProjectById(id);
    }

    @Override
    public PageDTO<ProjectDTO> getCurrentUserProjects(Map<String, Object> params, int pageNum, int pageSize) {
        // 获取当前用户ID
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        Long currentUserId = currentUser.getId();
        
        // 查询当前用户的项目
        List<ProjectDTO> allProjects = projectMapper.selectByLeaderId(currentUserId);
        
        // 根据查询参数过滤项目
        List<ProjectDTO> filteredProjects = allProjects;
        if (params != null && !params.isEmpty()) {
            filteredProjects = allProjects.stream()
                    .filter(p -> {
                        boolean match = true;
                        if (params.containsKey("name")) {
                            match = match && p.getName() != null && p.getName().contains((String) params.get("name"));
                        }
                        if (params.containsKey("type")) {
                            match = match && p.getType() != null && p.getType().equals(params.get("type"));
                        }
                        if (params.containsKey("status")) {
                            match = match && p.getStatus() != null && p.getStatus().equals(params.get("status"));
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }
        
        // 计算总记录数
        long total = filteredProjects.size();
        
        // 分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            // 如果起始索引超出总数，返回空列表
            return new PageDTO<>(new ArrayList<>(), total, pageNum, pageSize);
        }
        
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<ProjectDTO> pagedProjects = filteredProjects.subList(fromIndex, toIndex);
        
        // 返回分页结果
        return new PageDTO<>(pagedProjects, total, pageNum, pageSize);
    }

    @Override
    public List<ProjectDTO> getAvailableProjectsForExpense() {
        // 获取当前用户ID
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        Long currentUserId = currentUser.getId();
        
        // 查询当前用户的项目，筛选出状态为audit且未删除的项目
        List<ProjectDTO> allUserProjects = projectMapper.selectByLeaderId(currentUserId);
        
        // 过滤掉不符合条件的项目
        return allUserProjects.stream()
                .filter(p -> "active".equals(p.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void checkAndUpdateExpiredProjects() {
        // 查询所有已过期且状态为active的项目
        List<ProjectDTO> expiredProjects = projectMapper.selectExpiredActiveProjects();
        
        if (expiredProjects != null && !expiredProjects.isEmpty()) {
            // 遍历过期项目，更新状态为待结题
            for (ProjectDTO project : expiredProjects) {
                projectMapper.updateToPendingCompletion(project.getId());
                System.out.println(String.format("项目 [ID=%d, 名称=%s] 已过期，状态更新为待结题",
                        project.getId(), project.getName()));
            }
        }
    }
    
    @Override
    @Transactional
    public ProjectDTO submitCompletionReport(Long id, String reportPath) {
        // 获取项目信息
        ProjectDTO project = getProjectById(id);
        
        // 校验项目状态
        if (!"pending_completion".equals(project.getStatus())) {
            throw new RuntimeException("只有待结题状态的项目才能提交结题报告");
        }
        
        // 校验当前用户是否为项目负责人
        String username = SecurityUtils.getCurrentUsername();
        User currentUser = userService.getUserByUsername(username);
        if (!project.getLeaderId().equals(currentUser.getId())) {
            throw new RuntimeException("只有项目负责人才能提交结题报告");
        }
        
        // 更新结题报告信息
        projectMapper.updateCompletionReport(id, reportPath);
        
        // 返回更新后的项目
        return getProjectById(id);
    }
    
    @Override
    @Transactional
    public ProjectDTO auditProjectCompletion(Long id, String auditStatus, String comment) {
        // 获取项目信息
        ProjectDTO project = getProjectById(id);
        
        // 校验项目状态
        if (!"completion_review".equals(project.getStatus())) {
            throw new RuntimeException("只有待结题审核状态的项目才能进行结题审核");
        }
        
        // 校验审核状态参数
        if (!"approved".equals(auditStatus) && !"rejected".equals(auditStatus)) {
            throw new RuntimeException("无效的审核状态，只能为 approved 或 rejected");
        }
        
        // 更新结题审核状态和意见
        projectMapper.updateCompletionAuditStatus(id, auditStatus, comment);
        
        // 返回更新后的项目
        return getProjectById(id);
    }
    
    @Override
    @Transactional
    public ProjectDTO updateUsedBudget(Long id, BigDecimal amount) {
        // 检查项目是否存在
        ProjectDTO project = getProjectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在，无法更新经费");
        }
        
        // 更新已用预算
        projectMapper.updateUsedBudget(id, amount);
        
        // 记录日志
        System.out.println(String.format("项目ID=%d, 名称=%s 的经费预算已更新，金额变动: %s，更新前已用预算: %s", 
                id, project.getName(), amount, project.getUsedBudget()));
        
        // 返回更新后的项目
        return getProjectById(id);
    }
    
    @Override
    public List<ProjectDTO> getProjectsByStatus(String status) {
        // 直接使用Mapper查询指定状态的项目
        List<ProjectDTO> projects = projectMapper.selectByStatus(status);
        
        // 为每个项目加载经费来源信息
        for (ProjectDTO project : projects) {
            // 查询项目经费来源
            List<ProjectFundingSource> fundingSources = projectFundingSourceRepository.findByProjectId(project.getId());
            if (fundingSources != null && !fundingSources.isEmpty()) {
                project.setFundingSources(fundingSources.stream()
                        .map(ProjectFundingSource::getSource)
                        .collect(Collectors.toList()));
            } else if (project.getFundingSource() != null) {
                // 兼容旧数据，如果没有查到多个经费来源，但有单个经费来源，则将其转换为列表
                project.setFundingSources(Collections.singletonList(project.getFundingSource()));
            }
        }
        
        return projects;
    }
} 