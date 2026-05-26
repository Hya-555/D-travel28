# 旅游业务管理系统 — 系统设计文档

## 1. 项目概述

### 1.1 项目背景

XXX 旅行社是武汉地区一家提供组团旅行的旅游公司。原有旅游业务申请过程均为手工完成，效率低下。为适应业务发展，开发旅游业务申请信息系统，实现业务流程的数字化管理。

### 1.2 项目目标

- 实现旅游申请在线办理，支持订金自动计算
- 实现参加者信息的在线录入与管理
- 支持每日催款文件（确认书、交款单）自动打印
- 支持取消/变更业务，自动计算手续费
- 支持旅游路线、旅游团、价格的基础数据管理
- 每日自动导出财务数据到外部财务系统

### 1.3 涉及角色

| 角色 | 主要职责 |
|------|---------|
| 前台接待员工 | 顾客接待、申请办理、支付收取、参加者录入、取消办理 |
| 催款员工 | 每日打印旅游确认书和余额交款单并邮寄 |
| 路线管理员工 | 旅游路线设计、旅游活动设定、价格管理 |
| 会计人员 | 在财务系统中处理每日导出的现金数据 |

## 2. 技术架构

### 2.1 技术选型

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 前端框架 | Vue 3 | 3.4+ |
| UI 组件库 | Element Plus | 2.7+ |
| 构建工具 | Vite | 5.4+ |
| HTTP 客户端 | Axios | 1.7+ |

### 2.2 项目结构

```
D-tavel28/
├── docs/                          # 文档
│   ├── 01-uml/                    # UML 图
│   │   ├── 01-use-case-diagram.md
│   │   ├── 02-class-diagram.md
│   │   ├── 03-sequence-diagrams.md
│   │   └── 04-state-activity-diagrams.md
│   ├── 02-database/               # 数据库
│   │   ├── 01-er-diagram.md
│   │   └── 02-create-tables.sql
│   └── 03-design/                 # 设计文档
├── backend/                       # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/travel/
│       ├── TravelApplication.java
│       ├── config/                # 配置（CORS、异常处理、MetaHandler）
│       ├── entity/                # 实体类（11个）
│       ├── mapper/                # Mapper 接口（11个）
│       ├── dto/                   # 数据传输对象（7个）
│       ├── service/               # 服务接口（6个）
│       │   └── impl/              # 服务实现（6个）
│       └── controller/            # REST 控制器（6个）
└── frontend/                      # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/index.js           # API 请求封装
        ├── router/index.js        # 路由配置
        ├── layout/MainLayout.vue  # 主布局
        └── views/                 # 页面（9个）
```

## 3. 核心业务规则

### 3.1 订金计算（表1）

| 距出发天数 | 订金比例 |
|-----------|---------|
| ≥ 60天（2个月） | 总价的 10% |
| 30~59天（1个月~2个月） | 总价的 20% |
| < 30天（< 1个月） | 总价的 100%（全款） |

### 3.2 取消手续费（表3）

| 距出发天数 | 手续费比例 |
|-----------|----------|
| > 30天（1个月以上） | 0%（无手续费） |
| 10~30天（1个月~10天） | 已付金额的 20% |
| 1~9天（10天~1天） | 已付金额的 50% |
| 0天（出发当天） | 已付金额的 100%（不退） |

**退款金额 = 已付金额 - 手续费**

### 3.3 余款支付期限

```
支付期限 = MAX(出发日期 - 30天, 交款单发送日期 + 10天)
```

即：保证从发送交款单到支付期限至少有 10 天。

### 3.4 价格管理规则

- 价格可多次设定，设定后处于"未公开"状态
- 公开后不可再次变更
- 未公开的价格对顾客不可见
- 已公开价格后有新价格需求，需重新设定并公开

## 4. 数据库设计

共 11 张业务表：

| 表名 | 说明 | 主键 |
|------|------|------|
| employee | 员工 | employee_id (自增) |
| tour_route | 旅游路线 | route_code |
| route_history | 路线变更历史 | id (自增) |
| tour_group | 旅游团 | group_code |
| tour_price | 旅游团价格 | id (自增) |
| application | 旅游申请 | application_id (自增) |
| participant | 参加者 | participant_id (自增) |
| payment | 支付记录 | payment_id (自增) |
| receipt | 收据/确认书 | receipt_id (自增) |
| application_cancel | 取消记录 | cancel_id (自增) |
| financial_export | 财务导出记录 | export_id (自增) |

## 5. API 接口清单

### 5.1 申请管理 `/api/application`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/check/{groupCode}` | 检查旅游团是否可申请 |
| POST | `/apply` | 办理旅游申请 |
| POST | `/deposit/{id}` | 支付订金 |
| POST | `/{id}/participant` | 录入参加者 |
| GET | `/{id}/participants` | 查询参加者列表 |
| POST | `/{id}/complete` | 完成申请 |
| GET | `/find` | 按条件查询申请 |
| GET | `/{id}` | 申请详情 |
| GET | `/list` | 申请列表 |

### 5.2 支付管理 `/api/payment`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/balance` | 支付余款 |
| GET | `/list/{applicationId}` | 查询支付记录 |

### 5.3 取消管理 `/api/cancel`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/` | 取消申请/参加者变更 |

### 5.4 收据管理 `/api/receipt`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/deposit/{id}` | 打印订金收据 |
| POST | `/daily` | 每日批量打印 |

### 5.5 路线/旅游团/价格管理 `/api/tour`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/route` | 新建路线 |
| PUT | `/route/{code}` | 变更路线 |
| POST | `/route/{code}/cancel` | 取消路线 |
| GET | `/routes` | 路线列表 |
| POST | `/group` | 创建旅游团 |
| GET | `/groups` | 旅游团列表 |
| GET | `/groups/available` | 可报名旅游团 |
| POST | `/price` | 设定价格 |
| POST | `/price/{id}/publish` | 公开价格 |
| GET | `/prices/{groupCode}` | 价格历史 |

### 5.6 财务导出 `/api/financial`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/export` | 手动触发财务导出 |

## 6. 部署指南

### 6.1 环境要求

- JDK 11+
- MySQL 8.0+
- Node.js 18+
- Maven 3.6+

### 6.2 后端启动

```bash
# 1. 创建数据库
mysql -u root -p < docs/02-database/02-create-tables.sql

# 2. 修改 application.yml 中的数据库连接信息

# 3. 启动后端
cd backend
mvn spring-boot:run
```

### 6.3 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，后端 API 代理到 `http://localhost:8080`。

### 6.4 生产部署

```bash
# 后端打包
cd backend && mvn package -DskipTests

# 前端打包
cd frontend && npm run build

# 将 frontend/dist/ 部署到 Nginx，配置反向代理到后端
```
