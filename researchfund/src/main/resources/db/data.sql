USE research_fund;

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, sort, status, remark) VALUES 
('管理员', 'ROLE_ADMIN', 1, 0, '管理员角色'),
('科研人员', 'ROLE_RESEARCHER', 2, 0, '科研人员角色');

-- 初始化部门
INSERT INTO sys_department (name, code, parent_id, order_num, status, remark) VALUES 
('财务部', 'FINANCE', 0, 1, 0, '财务部'),
('国际教育学院', 'INTL', 0, 2, 0, '国际教育学院'),
('信息工程学院', 'INFO', 0, 3, 0, '信息工程学院'),
('会计学院', 'ACC', 0, 4, 0, '会计学院'),
('机电工程学院', 'MECH', 0, 5, 0, '机电工程学院'),
('经济管理学院', 'ECON', 0, 4, 0, '经济管理学院'),
('艺术设计学院', 'ART', 0, 5, 0, '艺术设计学院'),
('外国语学院', 'FORE', 0, 6, 0, '外国语学院'),
('马克思主义学院', 'MARX', 0, 7, 0, '马克思主义学院'),
('体育学院', 'SPORT', 0, 8, 0, '体育学院');



-- 初始化用户
-- 密码为：123456
INSERT INTO sys_user (username, password, real_name, phone, email, department_id, status, gender, remark) VALUES 
('admin', '123456', '管理员', '13800138001', 'admin@example.com', 1, 0, 1, '系统管理员'),
('test', '123456', '科研人员', '13800138001', 'test@example.com', 2, 0, 1, '国际教育学院科研人员');


-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1), -- admin - 管理员
(2, 2); -- test - 科研人员






