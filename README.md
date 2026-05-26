# 旅游业务管理系统

XXX 旅行社旅游业务申请信息系统。替代手工流程，实现旅游申请、支付、取消、催款、路线管理的完整数字化。

## 快速开始

### 后端（Spring Boot）

```bash
cd backend
# 先导入 docs/02-database/02-create-tables.sql 到 MySQL
# 修改 src/main/resources/application.yml 数据库连接
mvn spring-boot:run
```

### 前端（Vue 3）

```bash
cd frontend
npm install
npm run dev
```

前端 http://localhost:3000 ，后端 http://localhost:8080

## 项目结构

```
├── docs/                        # 文档
│   ├── 01-uml/                  # UML 图（用例、类图、时序、状态活动图）
│   ├── 02-database/             # ER 图 + 建表 SQL
│   └── 03-design/               # 系统设计文档
├── backend/                     # Spring Boot 后端
└── frontend/                    # Vue 3 前端
```

## 核心功能

- **旅游申请** — 查询旅游团 → 校验截止日期和人数 → 自动计算订金 → 支付 → 打印收据
- **参加者录入** — 收到申请书后录入各参加者信息 → 确认申请完成
- **收/催款** — 支付余款；每日打印旅游确认书和余额交款单并邮寄
- **取消变更** — 支持部分/全部取消，自动计算手续费和退款
- **路线管理** — 路线增改、历史留痕；旅游团创建；价格设定与公开

## 关键业务规则

- **订金**: 距出发 ≥60天→10%，30~59天→20%，<30天→全款
- **取消手续费**: >30天→0%，10~30天→20%，1~9天→50%，0天→100%
- **余款期限**: max(出发日期-30天, 交款单发送日+10天)
- **价格**: 设定后非公开，公开后不可变更
- **路线**: 不删除，状态变更，保留变更历史

## 技术栈

| 层 | 技术 |
|----|-----|
| 后端 | Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL 8.0 |
| 前端 | Vue 3 + Element Plus + Vite + Axios |
