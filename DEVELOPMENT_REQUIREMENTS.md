# 项目开发需求清单（当前版本）

## 1. 项目目标（当前阶段）
“农汇通 AgriMatch - 本地演示”：
- 已跑通：注册/登录鉴权、用户/公司档案、品类树、采购需求、需求模板、供应发布、帖子/积分/通知/聊天、首页统计
- 继续迭代：距离/到厂价排序（高德距离）、智能匹配、地图能力、业务闭环（成交/评价/合同）等

## 2. 已落地需求（当前可用功能）

### 2.1 账号与鉴权
- 注册：创建账号并自动登录（返回 JWT）
- 登录：账号+密码登录（返回 JWT）
- 登录态：前端本地存储 token，并自动在请求头加入 Bearer Token
- 获取当前用户：`GET /api/auth/me`

### 2.2 用户档案（我的账号）
- 查询我的用户信息：`GET /api/users/me`
- 更新我的用户信息：`PUT /api/users/me`
- 切换角色（采购/供应）：`PUT /api/users/me/roles`

### 2.3 公司档案（企业资料）
- 创建公司：`POST /api/companies`
- 查询我的公司：`GET /api/companies/my`
- 更新公司：`PUT /api/companies/{id}`
- 上传营业执照/资质图片：`POST /api/companies/{id}/license`
- 图片静态访问：后端通过 `/uploads/**` 映射本地 `uploads` 目录，前端可预览

### 2.4 品类体系（产品树）
- 获取品类树：`GET /api/products/tree`
- 搜索品类：`GET /api/products/search?keyword=xxx`
- 自定义品类：`POST /api/products/custom`（需要登录）
- 品类参数（用于供应发布表单动态字段）：`GET /api/products/{productId}/params`
- 品类参数下拉新增选项：`POST /api/products/params/{paramId}/options`（需要登录）

### 2.5 采购需求（发布与列表）
- 发布需求：`POST /api/requirements`
- 需求详情：`GET /api/requirements/{id}`
- 需求列表：`GET /api/requirements`（companyId/userId/categoryName/status/orderBy/order）
- 更新需求：`PUT /api/requirements/{id}`
- 删除需求（逻辑删除）：`DELETE /api/requirements/{id}`
- 发布时效：`expireMinutes -> expireTime`；过期需求在列表/详情均不展示

### 2.6 需求模板（常用模板）
- 保存模板：`POST /api/requirement-templates`（登录）
- 我的模板：`GET /api/requirement-templates`（登录）
- 删除模板：`DELETE /api/requirement-templates/{id}`（登录）

### 2.7 供应发布（发布与列表）
- 发布供应：`POST /api/supplies`（登录，要求用户已绑定 companyId）
- 供应详情：`GET /api/supplies/{id}`（当前为登录可访问）
- 供应列表：`GET /api/supplies`（companyId/userId/categoryName/orderBy/order）
- 更新供应：`PUT /api/supplies/{id}`（登录，仅本人可更新）
- 删除供应（逻辑删除）：`DELETE /api/supplies/{id}`（登录，仅本人可删除）
- 排序支持：`create_time` / `ex_factory_price`；`distance`/`delivered_price` 目前为占位

### 2.8 帖子/互动
- 帖子发布/列表/详情/删除：`/api/posts`
- 点赞/评论等社交能力：`/api/posts/*`（以接口实现为准）

### 2.9 积分
- 我的余额：`GET /api/points/me`
- 充值/兑换：`POST /api/points/recharge`、`POST /api/points/redeem`
- 明细：`GET /api/points/tx`

### 2.10 消息通知
- 我的通知列表：`GET /api/notify/my`
- 标记已读：`POST /api/notify/my/read/{id}`
- 全部已读：`POST /api/notify/my/read-all`

### 2.11 即时聊天
- 会话列表/历史/已读：`/api/chat/*`
- WebSocket：`/ws/chat?token=...`

### 2.12 首页与基础指标
- 健康检查：`GET /api/health`
- 首页统计：`GET /api/home/stats`（用户数/需求数/供应数）

## 3. 未完成 / 占位需求（已出现“占位/规划”信号）

### 3.1 距离/到厂价排序（高德距离）
现状：
- 需求列表/供应列表里“distance/到厂价”排序仍是占位（SQL 回退到 create_time 或 ex_factory_price）
缺口：
- 距离计算与缓存策略（高德/自研 Haversine）
- 列表排序与分页策略（避免全量计算）

### 3.2 智能匹配
- 按到厂价/距离/时间排序的综合评分与权重配置
- 采购需求与供应发布的匹配结果页/推送

### 3.3 地图（高德）
- 点位聚合/浮窗/导航
- 地图选点回填（需求采购点/供应发货点）

### 3.4 业务闭环
- 成交/订单/合同模板（库表已预置时的接口与页面落地）
- 评价体系（库表已预置时的接口与页面落地）

## 4. 运行与联调假设（影响需求验收）
- 前端端口：5173
- 后端端口：8080
- 前端通过 Vite proxy 将 `/api` 转发到 `http://localhost:8080`
- 后端 CORS 允许 `http://localhost:5173` 访问 `/api/**`
- 数据库：MySQL，默认连接 `localhost:3306`，库名 `nonghuitong`（启动自动初始化 SQL）


