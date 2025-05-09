USE research_fund;

-- 为项目表添加经费来源字段
ALTER TABLE project 
ADD COLUMN funding_source VARCHAR(20) DEFAULT NULL COMMENT '经费来源：fiscal-财政经费，school-校配套经费，other-其他经费' 
AFTER type;

-- 创建项目预算科目表
CREATE TABLE IF NOT EXISTS project_budget_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    category VARCHAR(50) NOT NULL COMMENT '预算科目：设备费、材料费、测试化验费、差旅费、会议费、劳务费、专家咨询费、其他费用',
    amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    remark VARCHAR(255) COMMENT '备注',
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB COMMENT='项目预算科目表';

-- 创建项目经费来源表（支持多个经费来源）
CREATE TABLE IF NOT EXISTS project_funding_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    source VARCHAR(20) NOT NULL COMMENT '经费来源：fiscal-财政经费，school-校配套经费，other-其他经费',
    create_by BIGINT COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0代表未删除，1代表已删除）',
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB COMMENT='项目经费来源表';

-- 创建项目经费到账记录表
CREATE TABLE IF NOT EXISTS `project_fund_arrival` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `funding_source` varchar(50) NOT NULL COMMENT '经费来源：fiscal-财政经费，school-校配套经费，other-其他经费',
  `amount` decimal(12,2) NOT NULL COMMENT '到账金额',
  `arrival_date` date NOT NULL COMMENT '到账日期',
  `voucher_path` varchar(255) DEFAULT NULL COMMENT '到账凭证文件路径',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_arrival_date` (`arrival_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目经费到账记录表'; 
