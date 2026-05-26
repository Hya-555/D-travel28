-- =====================================================
-- 旅游业务管理系统 - 数据库建表脚本
-- 数据库: MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS travel_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE travel_system;

-- =====================================================
-- 1. 员工表
-- =====================================================
CREATE TABLE employee (
    employee_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    name          VARCHAR(50)  NOT NULL                COMMENT '姓名',
    role          VARCHAR(30)  NOT NULL                COMMENT '角色: RECEPTIONIST/COLLECTOR/ROUTE_MANAGER/ADMIN',
    phone         VARCHAR(20)  DEFAULT NULL            COMMENT '电话',
    department    VARCHAR(50)  DEFAULT NULL            COMMENT '部门',
    status        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 0=离职 1=在职',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id),
    INDEX idx_employee_role (role)
) ENGINE=InnoDB COMMENT='员工';

-- =====================================================
-- 2. 旅游路线表
-- =====================================================
CREATE TABLE tour_route (
    route_code    VARCHAR(20)  NOT NULL                COMMENT '路线代码',
    route_name    VARCHAR(100) NOT NULL                COMMENT '路线名称',
    description   TEXT                                  COMMENT '路线描述',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (route_code),
    INDEX idx_route_status (status)
) ENGINE=InnoDB COMMENT='旅游路线';

-- =====================================================
-- 3. 路线变更历史表
-- =====================================================
CREATE TABLE route_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    route_code    VARCHAR(20)  NOT NULL                COMMENT '路线代码',
    change_type   VARCHAR(20)  NOT NULL                COMMENT '变更类型: UPDATE/CANCEL',
    old_value     TEXT                                  COMMENT '变更前值(JSON)',
    new_value     TEXT                                  COMMENT '变更后值(JSON)',
    change_reason VARCHAR(500) DEFAULT NULL            COMMENT '变更原因',
    change_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator_id   BIGINT       DEFAULT NULL            COMMENT '操作员工ID',
    PRIMARY KEY (id),
    INDEX idx_history_route (route_code),
    INDEX idx_history_time (change_time),
    CONSTRAINT fk_history_route FOREIGN KEY (route_code) REFERENCES tour_route(route_code)
) ENGINE=InnoDB COMMENT='路线变更历史';

-- =====================================================
-- 4. 旅游团表
-- =====================================================
CREATE TABLE tour_group (
    group_code    VARCHAR(20)  NOT NULL                COMMENT '旅游团代码',
    route_code    VARCHAR(20)  NOT NULL                COMMENT '所属路线代码',
    departure_date DATE        NOT NULL                COMMENT '出发日期',
    deadline      DATE         NOT NULL                COMMENT '申请截止日期',
    max_capacity  INT          NOT NULL                COMMENT '人数上限',
    current_count INT          NOT NULL DEFAULT 0      COMMENT '当前已报名人数',
    status        VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE/FULL/EXPIRED/CANCELLED',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_code),
    INDEX idx_group_route (route_code),
    INDEX idx_group_departure (departure_date),
    INDEX idx_group_status (status),
    CONSTRAINT fk_group_route FOREIGN KEY (route_code) REFERENCES tour_route(route_code)
) ENGINE=InnoDB COMMENT='旅游团';

-- =====================================================
-- 5. 旅游团价格表
-- =====================================================
CREATE TABLE tour_price (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    group_code    VARCHAR(20)  NOT NULL                COMMENT '旅游团代码',
    adult_price   DECIMAL(10,2) NOT NULL              COMMENT '大人价格(元)',
    child_price   DECIMAL(10,2) NOT NULL              COMMENT '小孩价格(元)',
    discount_desc VARCHAR(500) DEFAULT NULL            COMMENT '优惠措施描述',
    is_published  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已公开: 0=否 1=是',
    set_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    set_by        BIGINT       DEFAULT NULL            COMMENT '设定员工ID',
    PRIMARY KEY (id),
    INDEX idx_price_group (group_code),
    INDEX idx_price_published (is_published),
    CONSTRAINT fk_price_group FOREIGN KEY (group_code) REFERENCES tour_group(group_code)
) ENGINE=InnoDB COMMENT='旅游团价格';

-- =====================================================
-- 6. 申请表
-- =====================================================
CREATE TABLE application (
    application_id  BIGINT       NOT NULL AUTO_INCREMENT,
    group_code      VARCHAR(20)  NOT NULL              COMMENT '旅游团代码',
    departure_date  DATE         NOT NULL              COMMENT '出发日期',
    contact_name    VARCHAR(50)  NOT NULL              COMMENT '申请责任人姓名',
    contact_phone   VARCHAR(20)  NOT NULL              COMMENT '申请责任人电话',
    adult_count     INT          NOT NULL DEFAULT 0    COMMENT '大人人数',
    child_count     INT          NOT NULL DEFAULT 0    COMMENT '小孩人数',
    deposit_amount  DECIMAL(10,2) NOT NULL DEFAULT 0   COMMENT '订金金额',
    total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0   COMMENT '总费用',
    paid_amount     DECIMAL(10,2) NOT NULL DEFAULT 0   COMMENT '已付金额',
    status          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/DEPOSIT_PAID/PARTICIPANTS_ENTERED/COMPLETED/CANCELLED',
    apply_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME     DEFAULT NULL          COMMENT '完成时间',
    handled_by      BIGINT       DEFAULT NULL          COMMENT '经手员工ID',
    PRIMARY KEY (application_id),
    INDEX idx_app_group (group_code),
    INDEX idx_app_status (status),
    INDEX idx_app_contact (contact_name),
    INDEX idx_app_departure (departure_date),
    INDEX idx_app_time (apply_time),
    CONSTRAINT fk_app_group FOREIGN KEY (group_code) REFERENCES tour_group(group_code)
) ENGINE=InnoDB COMMENT='旅游申请';

-- =====================================================
-- 7. 参加者表
-- =====================================================
CREATE TABLE participant (
    participant_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id   BIGINT       NOT NULL              COMMENT '所属申请ID',
    name             VARCHAR(50)  NOT NULL              COMMENT '姓名',
    gender           VARCHAR(4)   DEFAULT NULL          COMMENT '性别',
    birth_date       DATE         DEFAULT NULL          COMMENT '出生日期',
    phone            VARCHAR(20)  DEFAULT NULL          COMMENT '电话',
    address          VARCHAR(200) DEFAULT NULL          COMMENT '联系地址',
    zip_code         VARCHAR(10)  DEFAULT NULL          COMMENT '邮政编码',
    email            VARCHAR(100) DEFAULT NULL          COMMENT 'Email',
    emergency_contact VARCHAR(50)  DEFAULT NULL         COMMENT '旅途联络人姓名',
    emergency_address VARCHAR(200) DEFAULT NULL         COMMENT '旅途联络地址',
    emergency_phone  VARCHAR(20)  DEFAULT NULL          COMMENT '旅途联络电话',
    relationship     VARCHAR(20)  DEFAULT NULL          COMMENT '与本人关系',
    is_contact_person TINYINT     NOT NULL DEFAULT 0    COMMENT '是否申请责任人: 0=否 1=是',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED/CHANGED',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (participant_id),
    INDEX idx_part_app (application_id),
    INDEX idx_part_name (name),
    CONSTRAINT fk_part_app FOREIGN KEY (application_id) REFERENCES application(application_id)
) ENGINE=InnoDB COMMENT='参加者';

-- =====================================================
-- 8. 支付记录表
-- =====================================================
CREATE TABLE payment (
    payment_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT     NOT NULL                COMMENT '所属申请ID',
    payment_no   VARCHAR(30)  NOT NULL                COMMENT '交款单编号/支付流水号',
    payment_type VARCHAR(20)  NOT NULL                COMMENT '类型: DEPOSIT(订金)/BALANCE(余款)',
    amount       DECIMAL(10,2) NOT NULL               COMMENT '金额(元)',
    pay_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PAID' COMMENT '状态: PAID/REFUNDED',
    received_by  BIGINT       DEFAULT NULL            COMMENT '收款员工ID',
    PRIMARY KEY (payment_id),
    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_pay_app (application_id),
    INDEX idx_pay_time (pay_time),
    INDEX idx_pay_type (payment_type),
    CONSTRAINT fk_pay_app FOREIGN KEY (application_id) REFERENCES application(application_id)
) ENGINE=InnoDB COMMENT='支付记录';

-- =====================================================
-- 9. 收据/确认书打印记录表
-- =====================================================
CREATE TABLE receipt (
    receipt_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT     NOT NULL                COMMENT '所属申请ID',
    receipt_no   VARCHAR(30)  NOT NULL                COMMENT '收据/确认书编号',
    receipt_type VARCHAR(30)  NOT NULL                COMMENT '类型: DEPOSIT_RECEIPT/CONFIRMATION/PAYMENT_SLIP',
    print_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    printed_by   BIGINT       DEFAULT NULL            COMMENT '打印员工ID',
    PRIMARY KEY (receipt_id),
    UNIQUE KEY uk_receipt_no (receipt_no),
    INDEX idx_rec_app (application_id),
    INDEX idx_rec_time (print_time),
    CONSTRAINT fk_rec_app FOREIGN KEY (application_id) REFERENCES application(application_id)
) ENGINE=InnoDB COMMENT='收据/确认书打印记录';

-- =====================================================
-- 10. 取消/变更记录表
-- =====================================================
CREATE TABLE application_cancel (
    cancel_id                 BIGINT       NOT NULL AUTO_INCREMENT,
    application_id            BIGINT       NOT NULL          COMMENT '所属申请ID',
    participant_id            BIGINT       DEFAULT NULL      COMMENT '被取消的参加者ID(NULL=整个申请取消)',
    cancel_type               VARCHAR(30)  NOT NULL          COMMENT '类型: PARTICIPANT_CHANGE/PARTICIPANT_REMOVE/FULL_CANCEL',
    reason                    VARCHAR(500) DEFAULT NULL      COMMENT '取消原因',
    handling_fee              DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '取消手续费(元)',
    refund_amount             DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '退款金额(元)',
    new_contact_name          VARCHAR(50)  DEFAULT NULL      COMMENT '新责任人姓名',
    new_contact_participant_id BIGINT      DEFAULT NULL      COMMENT '新责任人参加者ID',
    cancel_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_by                BIGINT       DEFAULT NULL      COMMENT '经手员工ID',
    PRIMARY KEY (cancel_id),
    INDEX idx_cancel_app (application_id),
    INDEX idx_cancel_part (participant_id),
    INDEX idx_cancel_time (cancel_time),
    CONSTRAINT fk_cancel_app FOREIGN KEY (application_id) REFERENCES application(application_id)
) ENGINE=InnoDB COMMENT='取消/变更记录';

-- =====================================================
-- 11. 财务状况导出记录表
-- =====================================================
CREATE TABLE financial_export (
    export_id    BIGINT       NOT NULL AUTO_INCREMENT,
    export_date  DATE         NOT NULL                COMMENT '导出日期',
    data_type    VARCHAR(30)  NOT NULL                COMMENT '数据类型: DEPOSIT/BALANCE',
    content      LONGTEXT     DEFAULT NULL            COMMENT '导出数据内容(JSON)',
    export_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (export_id),
    INDEX idx_export_date (export_date)
) ENGINE=InnoDB COMMENT='财务状况导出记录';

-- =====================================================
-- 初始数据: 系统管理员工
-- =====================================================
INSERT INTO employee (name, role, phone, department) VALUES
('系统管理员', 'ADMIN', '000-0000-0000', '管理部');
```
