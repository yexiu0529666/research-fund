-- 创建数据库
CREATE DATABASE IF NOT EXISTS research_fund DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE research_fund;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(11) COMMENT '手机号',
    email VARCHAR(50) COMMENT '邮箱',
    department_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    gender TINYINT DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    UNIQUE KEY idx_username (username)
) ENGINE=InnoDB COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色代码',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 0 COMMENT '状态（0正常 1停用）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    UNIQUE KEY idx_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    UNIQUE KEY idx_user_role (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    code VARCHAR(50) COMMENT '部门编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader_id BIGINT COMMENT '负责人',
    phone VARCHAR(11) COMMENT '联系电话',
    status TINYINT DEFAULT 0 COMMENT '部门状态（0正常 1停用）',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注'
) ENGINE=InnoDB COMMENT='部门表';

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    type VARCHAR(20) NOT NULL COMMENT '项目类型：school-校级项目，horizontal-横向项目，vertical-纵向项目',
    leader_id BIGINT NOT NULL COMMENT '项目负责人ID',
    leader_name VARCHAR(50) NOT NULL COMMENT '项目负责人姓名',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    budget DECIMAL(12,2) NOT NULL COMMENT '总预算',
    used_budget DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已用预算',
    status VARCHAR(20) NOT NULL COMMENT '项目状态：planning-筹划中，active-进行中，completed-已完成，suspended-已暂停,pending_completion-待结题,completion_review-待结题审核,archived-已归档',
    audit_status VARCHAR(20) NOT NULL COMMENT '审核状态：audit-待审核，pending-审核中，approved-审核通过，rejected-审核未通过',
    description VARCHAR(1000) COMMENT '项目描述',
    research_content TEXT COMMENT '研究内容',
    expected_results VARCHAR(1000) COMMENT '预期成果',
    file_path VARCHAR(255) NOT NULL COMMENT '申请书路径',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    completion_report_path VARCHAR(255) COMMENT '结题报告文件路径',
    completion_audit_status VARCHAR(50) COMMENT '结题审核状态：pending-待审核, approved-审核通过, rejected-审核不通过',
    completion_audit_comment TEXT COMMENT '结题审核意见',
    completion_report_submit_time DATE COMMENT '结题报告提交时间'
) ENGINE=InnoDB COMMENT='项目表';

-- 项目团队成员表
CREATE TABLE IF NOT EXISTS project_team_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    name VARCHAR(50) NOT NULL COMMENT '成员姓名',
    role VARCHAR(50) NOT NULL COMMENT '成员角色',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB COMMENT='项目团队成员表';

-- 经费申请表
CREATE TABLE IF NOT EXISTS expense_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    title VARCHAR(100) NOT NULL COMMENT '申请标题',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    project_name VARCHAR(100) NOT NULL COMMENT '所属项目名称',
    type VARCHAR(20) NOT NULL COMMENT '申请类型：equipment-设备费,material-材料费,test-测试化验费,travel-差旅费,meeting-会议费,labor-劳务费,consultation-专家咨询费,other-其他费用',
    amount DECIMAL(12,2) NOT NULL COMMENT '申请金额',
    apply_date DATE NOT NULL COMMENT '申请日期',
    purpose VARCHAR(500) NOT NULL COMMENT '预计用途',
    reason VARCHAR(1000) NOT NULL COMMENT '申请理由',
    apply_user_id BIGINT NOT NULL COMMENT '申请人ID',
    apply_user_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核,approved-已批准,rejected-已拒绝,paid-已支付,receipt_pending-待提交报销凭证,repayment_pending-负责人自行还款,completed-已完成,repaid-已还款,receipt_audit-报销凭证审核中',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_user_name VARCHAR(50) COMMENT '审核人姓名',
    audit_time DATETIME COMMENT '审核时间',
    audit_comment VARCHAR(500) COMMENT '审核意见',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    category VARCHAR(20) DEFAULT 'advance' COMMENT '申请类别：advance-经费借款,reimbursement-报销',
    INDEX idx_project_id (project_id),
    INDEX idx_apply_user_id (apply_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='经费申请表';

-- 经费申请附件表
CREATE TABLE IF NOT EXISTS expense_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    expense_id BIGINT NOT NULL COMMENT '经费申请ID',
    name VARCHAR(100) NOT NULL COMMENT '附件名称',
    url VARCHAR(255) NOT NULL COMMENT '附件URL',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT '文件类型',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    INDEX idx_expense_id (expense_id)
) ENGINE=InnoDB COMMENT='经费申请附件表';

-- 经费结转表
CREATE TABLE IF NOT EXISTS `fund_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结转ID',
  `title` varchar(255) NOT NULL COMMENT '结转标题',
  `project_id` bigint(20) NOT NULL COMMENT '所属项目ID',
  `project_name` varchar(255) DEFAULT NULL COMMENT '所属项目名称',
  `amount` decimal(10,2) NOT NULL COMMENT '结转金额',
  `apply_date` date NOT NULL COMMENT '结转申请日期',
  `reason` varchar(1000) DEFAULT NULL COMMENT '结转原因',
  `description` varchar(2000) DEFAULT NULL COMMENT '结转说明',
  `from_year` varchar(10) DEFAULT NULL COMMENT '结转年度（从哪一年）',
  `to_year` varchar(10) DEFAULT NULL COMMENT '结转年度（到哪一年）',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `apply_user_name` varchar(255) DEFAULT NULL COMMENT '申请人姓名',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核,approved-已通过,rejected-已拒绝,completed-已完成',
  `audit_user_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_user_name` varchar(255) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_comment` varchar(1000) DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_apply_user_id` (`apply_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经费结转表';

-- 创建成果奖励表
CREATE TABLE IF NOT EXISTS achievement (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '成果名称',
    type VARCHAR(20) NOT NULL COMMENT '成果类型: journal(期刊论文), conference(会议论文), patent(专利), book(著作), software(软件著作权), other(其他)',
    level VARCHAR(20) COMMENT '成果级别: national(国家级), provincial(省部级), departmental(厅局级), school(校级), other(其他)',
    authors TEXT NOT NULL COMMENT '作者，以JSON数组格式存储',
    award_date DATE COMMENT '获奖日期',
    award_amount DECIMAL(12,2) COMMENT '奖励金额',
    project_id VARCHAR(36) COMMENT '相关项目ID，关联project表',
    description TEXT COMMENT '成果简介',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待审核), approved(已通过), rejected(已拒绝)',
    audit_time DATETIME COMMENT '审核时间',
    audit_user_id VARCHAR(36) COMMENT '审核人ID，关联user表',
    audit_comment TEXT COMMENT '审核意见',
    creator_id VARCHAR(36) NOT NULL COMMENT '创建人ID，关联user表',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remarks TEXT COMMENT '备注',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0(未删除), 1(已删除)'
) COMMENT '成果奖励表';

-- 创建成果奖励附件表
CREATE TABLE IF NOT EXISTS achievement_attachment (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    achievement_id VARCHAR(36) NOT NULL COMMENT '成果奖励ID，关联achievement表',
    name VARCHAR(100) NOT NULL COMMENT '附件名称',
    path VARCHAR(255) NOT NULL COMMENT '附件路径',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT '文件类型',
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0(未删除), 1(已删除)',
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
) COMMENT '成果奖励附件表';