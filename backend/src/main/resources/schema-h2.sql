-- =====================================================
-- 旅游业务管理系统 - H2 开发数据库建表脚本
-- 兼容 H2 MySQL 模式
-- =====================================================

-- 1. 员工表
CREATE TABLE IF NOT EXISTS employee (
    employee_id   BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)  NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    department    VARCHAR(50)  DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id)
);
CREATE INDEX IF NOT EXISTS idx_employee_role ON employee(role);

-- 2. 旅游路线表
CREATE TABLE IF NOT EXISTS tour_route (
    route_code    VARCHAR(20)  NOT NULL,
    route_name    VARCHAR(100) NOT NULL,
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (route_code)
);
CREATE INDEX IF NOT EXISTS idx_route_status ON tour_route(status);

-- 3. 路线变更历史表
CREATE TABLE IF NOT EXISTS route_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    route_code    VARCHAR(20)  NOT NULL,
    change_type   VARCHAR(20)  NOT NULL,
    old_value     TEXT,
    new_value     TEXT,
    change_reason VARCHAR(500) DEFAULT NULL,
    change_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator_id   BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_history_route ON route_history(route_code);
CREATE INDEX IF NOT EXISTS idx_history_time ON route_history(change_time);

-- 4. 旅游团表
CREATE TABLE IF NOT EXISTS tour_group (
    group_code    VARCHAR(20)  NOT NULL,
    route_code    VARCHAR(20)  NOT NULL,
    departure_date DATE        NOT NULL,
    deadline      DATE         NOT NULL,
    max_capacity  INT          NOT NULL,
    current_count INT          NOT NULL DEFAULT 0,
    status        VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_code)
);
CREATE INDEX IF NOT EXISTS idx_group_route ON tour_group(route_code);
CREATE INDEX IF NOT EXISTS idx_group_departure ON tour_group(departure_date);
CREATE INDEX IF NOT EXISTS idx_group_status ON tour_group(status);

-- 5. 旅游团价格表
CREATE TABLE IF NOT EXISTS tour_price (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    group_code    VARCHAR(20)  NOT NULL,
    adult_price   DECIMAL(10,2) NOT NULL,
    child_price   DECIMAL(10,2) NOT NULL,
    discount_desc VARCHAR(500) DEFAULT NULL,
    is_published  TINYINT      NOT NULL DEFAULT 0,
    set_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    set_by        BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_price_group ON tour_price(group_code);
CREATE INDEX IF NOT EXISTS idx_price_published ON tour_price(is_published);

-- 6. 申请表
CREATE TABLE IF NOT EXISTS application (
    application_id  BIGINT       NOT NULL AUTO_INCREMENT,
    group_code      VARCHAR(20)  NOT NULL,
    departure_date  DATE         NOT NULL,
    contact_name    VARCHAR(50)  NOT NULL,
    contact_phone   VARCHAR(20)  NOT NULL,
    adult_count     INT          NOT NULL DEFAULT 0,
    child_count     INT          NOT NULL DEFAULT 0,
    deposit_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0,
    paid_amount     DECIMAL(10,2) NOT NULL DEFAULT 0,
    status          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    apply_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME     DEFAULT NULL,
    handled_by      BIGINT       DEFAULT NULL,
    PRIMARY KEY (application_id)
);
CREATE INDEX IF NOT EXISTS idx_app_group ON application(group_code);
CREATE INDEX IF NOT EXISTS idx_app_status ON application(status);
CREATE INDEX IF NOT EXISTS idx_app_contact ON application(contact_name);
CREATE INDEX IF NOT EXISTS idx_app_departure ON application(departure_date);
CREATE INDEX IF NOT EXISTS idx_app_time ON application(apply_time);

-- 7. 参加者表
CREATE TABLE IF NOT EXISTS participant (
    participant_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id   BIGINT       NOT NULL,
    name             VARCHAR(50)  NOT NULL,
    gender           VARCHAR(4)   DEFAULT NULL,
    birth_date       DATE         DEFAULT NULL,
    phone            VARCHAR(20)  DEFAULT NULL,
    address          VARCHAR(200) DEFAULT NULL,
    zip_code         VARCHAR(10)  DEFAULT NULL,
    email            VARCHAR(100) DEFAULT NULL,
    emergency_contact VARCHAR(50)  DEFAULT NULL,
    emergency_address VARCHAR(200) DEFAULT NULL,
    emergency_phone  VARCHAR(20)  DEFAULT NULL,
    relationship     VARCHAR(20)  DEFAULT NULL,
    is_contact_person TINYINT     NOT NULL DEFAULT 0,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (participant_id)
);
CREATE INDEX IF NOT EXISTS idx_part_app ON participant(application_id);
CREATE INDEX IF NOT EXISTS idx_part_name ON participant(name);

-- 8. 支付记录表
CREATE TABLE IF NOT EXISTS payment (
    payment_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT     NOT NULL,
    payment_no   VARCHAR(30)  NOT NULL,
    payment_type VARCHAR(20)  NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    pay_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PAID',
    received_by  BIGINT       DEFAULT NULL,
    PRIMARY KEY (payment_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_no ON payment(payment_no);
CREATE INDEX IF NOT EXISTS idx_pay_app ON payment(application_id);
CREATE INDEX IF NOT EXISTS idx_pay_time ON payment(pay_time);
CREATE INDEX IF NOT EXISTS idx_pay_type ON payment(payment_type);

-- 9. 收据/确认书打印记录表
CREATE TABLE IF NOT EXISTS receipt (
    receipt_id   BIGINT       NOT NULL AUTO_INCREMENT,
    application_id BIGINT     NOT NULL,
    receipt_no   VARCHAR(30)  NOT NULL,
    receipt_type VARCHAR(30)  NOT NULL,
    print_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    printed_by   BIGINT       DEFAULT NULL,
    PRIMARY KEY (receipt_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_receipt_no ON receipt(receipt_no);
CREATE INDEX IF NOT EXISTS idx_rec_app ON receipt(application_id);
CREATE INDEX IF NOT EXISTS idx_rec_time ON receipt(print_time);

-- 10. 取消/变更记录表
CREATE TABLE IF NOT EXISTS application_cancel (
    cancel_id                 BIGINT       NOT NULL AUTO_INCREMENT,
    application_id            BIGINT       NOT NULL,
    participant_id            BIGINT       DEFAULT NULL,
    cancel_type               VARCHAR(30)  NOT NULL,
    reason                    VARCHAR(500) DEFAULT NULL,
    handling_fee              DECIMAL(10,2) NOT NULL DEFAULT 0,
    refund_amount             DECIMAL(10,2) NOT NULL DEFAULT 0,
    new_contact_name          VARCHAR(50)  DEFAULT NULL,
    new_contact_participant_id BIGINT      DEFAULT NULL,
    cancel_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_by                BIGINT       DEFAULT NULL,
    PRIMARY KEY (cancel_id)
);
CREATE INDEX IF NOT EXISTS idx_cancel_app ON application_cancel(application_id);
CREATE INDEX IF NOT EXISTS idx_cancel_part ON application_cancel(participant_id);
CREATE INDEX IF NOT EXISTS idx_cancel_time ON application_cancel(cancel_time);

-- 11. 财务状况导出记录表
CREATE TABLE IF NOT EXISTS financial_export (
    export_id    BIGINT       NOT NULL AUTO_INCREMENT,
    export_date  DATE         NOT NULL,
    data_type    VARCHAR(30)  NOT NULL,
    content      LONGTEXT     DEFAULT NULL,
    export_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (export_id)
);
CREATE INDEX IF NOT EXISTS idx_export_date ON financial_export(export_date);

-- 初始数据
INSERT INTO employee (name, role, phone, department) VALUES
('系统管理员', 'ADMIN', '000-0000-0000', '管理部');
