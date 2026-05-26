# 类图 — 旅游业务管理系统

```mermaid
classDiagram
    direction TB

    %% ====== 核心业务类 ======

    class TourRoute {
        -String routeCode
        -String routeName
        -String description
        -String status
        +getPrice() Price
        +getHistory() List~RouteHistory~
    }

    class RouteHistory {
        -Long id
        -String routeCode
        -String oldValue
        -String newValue
        -LocalDateTime changeTime
        -String changeReason
    }

    class TourGroup {
        -String groupCode
        -String routeCode
        -LocalDate departureDate
        -LocalDate deadline
        -Integer maxCapacity
        -Integer currentCount
        -String status
        +isAvailable() boolean
        +checkDeadline() boolean
        +hasCapacity() boolean
    }

    class Price {
        -Long id
        -String groupCode
        -BigDecimal adultPrice
        -BigDecimal childPrice
        -String discountDesc
        -LocalDateTime setTime
        -Boolean isPublished
    }

    class Application {
        -Long applicationId
        -String groupCode
        -LocalDate departureDate
        -String contactName
        -String contactPhone
        -Integer adultCount
        -Integer childCount
        -BigDecimal deposit
        -BigDecimal totalPrice
        -BigDecimal paidAmount
        -String status
        -LocalDateTime applyTime
        +calculateDeposit() BigDecimal
        +calculateTotal() BigDecimal
    }

    class Participant {
        -Long participantId
        -Long applicationId
        -String name
        -String gender
        -LocalDate birthDate
        -String phone
        -String address
        -String zipCode
        -String email
        -String emergencyContact
        -String emergencyAddress
        -String emergencyPhone
        -String relationship
        -Boolean isContactPerson
        -String status
    }

    class Payment {
        -Long paymentId
        -Long applicationId
        -String paymentNo
        -String paymentType
        -BigDecimal amount
        -LocalDateTime payTime
        -String status
    }

    class Receipt {
        -Long receiptId
        -Long applicationId
        -String receiptNo
        -String receiptType
        -LocalDateTime printTime
    }

    class CancelRecord {
        -Long cancelId
        -Long applicationId
        -Long participantId
        -String cancelType
        -BigDecimal handlingFee
        -BigDecimal refundAmount
        -String newContactName
        -String reason
        -LocalDateTime cancelTime
    }

    class FinancialExport {
        -Long exportId
        -LocalDate exportDate
        -String dataType
        -String content
        -LocalDateTime exportTime
    }

    class Employee {
        -Long employeeId
        -String name
        -String role
        -String phone
        -String department
    }

    %% ====== 枚举类 ======

    class ApplicationStatus {
        <<enumeration>>
        DRAFT
        DEPOSIT_PAID
        PARTICIPANTS_ENTERED
        COMPLETED
        CANCELLED
    }

    class PaymentType {
        <<enumeration>>
        DEPOSIT
        BALANCE
    }

    class CancelType {
        <<enumeration>>
        PARTICIPANT_CHANGE
        PARTICIPANT_REMOVE
        FULL_CANCEL
    }

    class GroupStatus {
        <<enumeration>>
        AVAILABLE
        FULL
        EXPIRED
        CANCELLED
    }

    %% ====== 关系 ======

    TourRoute "1" --> "*" RouteHistory : 变更历史
    TourGroup "1" --> "1" TourRoute : 所属路线
    TourGroup "1" --> "*" Price : 价格变更
    TourGroup "1" --> "*" Application : 被申请
    Application "1" --> "*" Participant : 包含参加者
    Application "1" --> "*" Payment : 支付记录
    Application "1" --> "*" Receipt : 收据
    Application "1" --> "*" CancelRecord : 取消记录
    Application --> ApplicationStatus : 状态
    Payment --> PaymentType : 类型
    CancelRecord --> CancelType : 类型
    TourGroup --> GroupStatus : 状态

    Employee "1" --> "*" Application : 办理
    Employee "1" --> "*" Payment : 经手
    Employee "1" --> "*" CancelRecord : 办理
    Employee "1" --> "*" TourRoute : 管理

    Application "1" --> "*" FinancialExport : 导出
```

## 核心类职责

| 类 | 职责 |
|----|------|
| **TourRoute** | 旅游路线，支持版本变更历史，状态变更（非删除） |
| **TourGroup** | 旅游团，校验截止日期和人数限额 |
| **Application** | 旅游申请，计算订金、总价，管理参加者 |
| **Participant** | 参加者信息，含紧急联系人 |
| **Payment** | 支付记录（订金/余款） |
| **Receipt** | 收据和确认书打印记录 |
| **CancelRecord** | 取消/变更记录，含手续费计算 |
| **Price** | 旅游团价格，可多次设定，公开后不可变更 |
| **FinancialExport** | 每日财务数据导出 |
