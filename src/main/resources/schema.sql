-- 设备报修工单服务数据库脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS repair_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE repair_db;

-- 报修工单主表
CREATE TABLE IF NOT EXISTS rep_tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号，格式：WO-年月日-序号',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT NOT NULL COMMENT '故障描述',
    fault_type VARCHAR(50) COMMENT '故障类型：HARDWARE-硬件故障，SOFTWARE-软件故障，NETWORK-网络问题，OTHER-其他',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级：LOW-低，MEDIUM-中，HIGH-高，URGENT-紧急',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '工单状态',
    requester_name VARCHAR(50) NOT NULL COMMENT '报修人姓名',
    requester_phone VARCHAR(20) COMMENT '报修人电话',
    requester_dept VARCHAR(100) COMMENT '报修人部门',
    location VARCHAR(200) COMMENT '故障位置',
    assigned_to VARCHAR(50) COMMENT '处理人姓名',
    assigned_to_id BIGINT COMMENT '处理人ID',
    estimated_completion_time DATETIME COMMENT '预计完成时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_completion_time DATETIME COMMENT '实际完成时间',
    cost DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    image_urls JSON COMMENT '故障图片列表',
    attachment_urls JSON COMMENT '附件列表',
    is_sla_breached TINYINT DEFAULT 0 COMMENT '是否SLA超时：0-否，1-是',
    closed_by BIGINT COMMENT '关闭人ID',
    closed_time DATETIME COMMENT '关闭时间',
    close_comment TEXT COMMENT '关闭说明',
    is_evaluated TINYINT DEFAULT 0 COMMENT '是否已评价：0-否，1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_ticket_no (ticket_no),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_assigned_to_id (assigned_to_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修工单表';

-- 工单状态流转历史表
CREATE TABLE IF NOT EXISTS rep_ticket_status_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    from_status VARCHAR(20) COMMENT '变更前状态',
    to_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    action VARCHAR(50) NOT NULL COMMENT '操作动作',
    comment TEXT COMMENT '操作说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_ticket (ticket_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (ticket_id) REFERENCES rep_tickets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单状态变更日志表';

-- 工单处理过程记录表
CREATE TABLE IF NOT EXISTS rep_ticket_process_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    process_type VARCHAR(50) NOT NULL COMMENT '处理类型：REPAIR-维修，COMMENT-备注，TRANSFER-转单，PAUSE-暂停，RESUME-恢复',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    content TEXT NOT NULL COMMENT '处理内容',
    images JSON COMMENT '处理过程图片',
    duration_minutes INT COMMENT '处理耗时（分钟）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_ticket (ticket_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (ticket_id) REFERENCES rep_tickets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单处理过程记录表';

-- 工单评价表
CREATE TABLE IF NOT EXISTS rep_ticket_evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_id BIGINT NOT NULL UNIQUE COMMENT '工单ID，一个工单只能评价一次',
    evaluator_name VARCHAR(50) COMMENT '评价人姓名',
    overall_score INT NOT NULL COMMENT '总体评分（1-5分）',
    response_speed_score INT COMMENT '响应速度评分（1-5分）',
    service_attitude_score INT COMMENT '服务态度评分（1-5分）',
    technical_level_score INT COMMENT '技术水平评分（1-5分）',
    comment TEXT COMMENT '评价内容',
    reply_content TEXT COMMENT '回复内容',
    replied_at DATETIME COMMENT '回复时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_ticket (ticket_id),
    FOREIGN KEY (ticket_id) REFERENCES rep_tickets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单评价表';

-- 工单撤销记录表
CREATE TABLE IF NOT EXISTS rep_ticket_cancels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    canceler_name VARCHAR(50) NOT NULL COMMENT '撤销人姓名',
    cancel_reason VARCHAR(500) NOT NULL COMMENT '撤销原因',
    cancel_time DATETIME NOT NULL COMMENT '撤销时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    INDEX idx_ticket (ticket_id),
    FOREIGN KEY (ticket_id) REFERENCES rep_tickets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单撤销记录表';
