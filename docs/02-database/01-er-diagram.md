# ER 图 — 旅游业务管理系统

```mermaid
erDiagram
    EMPLOYEE ||--o{ APPLICATION : 办理
    EMPLOYEE ||--o{ PAYMENT : 经手
    EMPLOYEE ||--o{ APPLICATION_CANCEL : 办理取消
    EMPLOYEE ||--o{ TOUR_ROUTE : 管理

    TOUR_ROUTE ||--o{ ROUTE_HISTORY : 变更历史
    TOUR_ROUTE ||--o{ TOUR_GROUP : 所属路线

    TOUR_GROUP ||--o{ TOUR_PRICE : 价格设定
    TOUR_GROUP ||--o{ APPLICATION : 被申请

    APPLICATION ||--o{ PARTICIPANT : 包含
    APPLICATION ||--o{ PAYMENT : 支付记录
    APPLICATION ||--o{ RECEIPT : 收据
    APPLICATION ||--o{ APPLICATION_CANCEL : 取消记录
    APPLICATION ||--o{ FINANCIAL_EXPORT : 导出

    EMPLOYEE {
        bigint employee_id PK
        varchar name
        varchar role
        varchar phone
        varchar department
        tinyint status
        datetime created_at
        datetime updated_at
    }

    TOUR_ROUTE {
        varchar route_code PK
        varchar route_name
        text description
        varchar status
        datetime created_at
        datetime updated_at
    }

    ROUTE_HISTORY {
        bigint id PK
        varchar route_code FK
        varchar old_value
        varchar new_value
        varchar change_type
        varchar change_reason
        datetime change_time
        bigint operator_id FK
    }

    TOUR_GROUP {
        varchar group_code PK
        varchar route_code FK
        date departure_date
        date deadline
        int max_capacity
        int current_count
        varchar status
        datetime created_at
        datetime updated_at
    }

    TOUR_PRICE {
        bigint id PK
        varchar group_code FK
        decimal adult_price
        decimal child_price
        varchar discount_desc
        tinyint is_published
        datetime set_time
        bigint set_by FK
    }

    APPLICATION {
        bigint application_id PK
        varchar group_code FK
        date departure_date
        varchar contact_name
        varchar contact_phone
        int adult_count
        int child_count
        decimal deposit_amount
        decimal total_amount
        decimal paid_amount
        varchar status
        datetime apply_time
        datetime complete_time
        bigint handled_by FK
    }

    PARTICIPANT {
        bigint participant_id PK
        bigint application_id FK
        varchar name
        varchar gender
        date birth_date
        varchar phone
        varchar address
        varchar zip_code
        varchar email
        varchar emergency_contact
        varchar emergency_address
        varchar emergency_phone
        varchar relationship
        tinyint is_contact_person
        varchar status
        datetime created_at
    }

    PAYMENT {
        bigint payment_id PK
        bigint application_id FK
        varchar payment_no UK
        varchar payment_type
        decimal amount
        datetime pay_time
        varchar status
        bigint received_by FK
    }

    RECEIPT {
        bigint receipt_id PK
        bigint application_id FK
        varchar receipt_no UK
        varchar receipt_type
        datetime print_time
        bigint printed_by FK
    }

    APPLICATION_CANCEL {
        bigint cancel_id PK
        bigint application_id FK
        bigint participant_id
        varchar cancel_type
        varchar reason
        decimal handling_fee
        decimal refund_amount
        varchar new_contact_name
        bigint new_contact_participant_id
        datetime cancel_time
        bigint handled_by FK
    }

    FINANCIAL_EXPORT {
        bigint export_id PK
        date export_date
        varchar data_type
        text content
        datetime export_time
    }
```

## 实体关系说明

| 关系 | 类型 | 说明 |
|------|------|------|
| TOUR_ROUTE → ROUTE_HISTORY | 1:N | 一条路线有多条变更历史 |
| TOUR_ROUTE → TOUR_GROUP | 1:N | 一条路线下可有多个旅游团 |
| TOUR_GROUP → TOUR_PRICE | 1:N | 一个旅游团价格可多次设定 |
| TOUR_GROUP → APPLICATION | 1:N | 一个旅游团可被多次申请 |
| APPLICATION → PARTICIPANT | 1:N | 一个申请包含多个参加者 |
| APPLICATION → PAYMENT | 1:N | 一个申请有多笔支付(订金+余款) |
| APPLICATION → RECEIPT | 1:N | 一个申请有多张收据/确认书 |
| APPLICATION → APPLICATION_CANCEL | 1:N | 一个申请可有多次取消/变更 |
```
