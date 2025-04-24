package com.vocational.researchfund.common;

/**
 * 系统常量
 */
public class Constants {
    
    /**
     * 项目状态
     */
    public interface ProjectStatus {
        /**
         * 申请中
         */
        Integer APPLYING = 1;
        
        /**
         * 已立项
         */
        Integer APPROVED = 2;
        
        /**
         * 已拨款
         */
        Integer FUNDED = 3;
        
        /**
         * 执行中
         */
        Integer EXECUTING = 4;
        
        /**
         * 结题中
         */
        Integer FINALIZING = 5;
        
        /**
         * 已结题
         */
        Integer COMPLETED = 6;
        
        /**
         * 已驳回
         */
        Integer REJECTED = 7;
    }
    
    /**
     * 项目类型
     */
    public interface ProjectType {
        /**
         * 横向项目
         */
        Integer HORIZONTAL = 1;
        
        /**
         * 纵向项目
         */
        Integer VERTICAL = 2;
    }
    
    /**
     * 用户角色
     */
    public interface UserRole {
        /**
         * 管理员
         */
        String ADMIN = "ROLE_ADMIN";
        
        /**
         * 财务
         */
        String FINANCE = "ROLE_FINANCE";
        
        /**
         * 科研人员
         */
        String RESEARCHER = "ROLE_RESEARCHER";
        
        /**
         * 科研主管
         */
        String RESEARCH_DIRECTOR = "ROLE_RESEARCH_DIRECTOR";
    }
    
    /**
     * 审核状态
     */
    public interface AuditStatus {
        /**
         * 待审核
         */
        Integer PENDING = 1;
        
        /**
         * 已通过
         */
        Integer APPROVED = 2;
        
        /**
         * 已驳回
         */
        Integer REJECTED = 3;
    }
} 