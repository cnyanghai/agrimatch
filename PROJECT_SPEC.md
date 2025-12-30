# 农汇通 AgriMatch（饲料原料供需采购平台）项目规格说明（可交付给其他 Agent 复刻）

> 目标：这份文档用于**完整描述当前仓库实现**（前端页面、后端接口、数据库表、关键流程与配置），使其他 AI Agent/开发者在不了解代码的情况下，也能按此需求“复刻出同等功能”的项目。

---

## 1. 项目定位与核心卖点

- **定位**：饲料/农产品原料的供需信息发布 + 地图展示 + 社区互动 + 交易闭环（MVP）平台。
- **核心卖点（当前实现）**：
  - 供需双侧发布与列表检索（品类树驱动、参数 JSON 扩展）
  - 公司档案（资质图片、坐标）与地图展示（高德 JS API）
  - 即时聊天（WebSocket）与消息通知
  - 积分充值/兑换与流水
  - **MVP 交易闭环**：供应上/下架、需求多次成交（部分/全部）、成交记录、评价强约束（仅成交双方可互评）
  - **距离/到厂价 Beta**：列表返回 distanceKm / deliveredPrice（简化模型）

---

## 2. 技术栈与架构

### 2.1 前端
- **框架**：Vue 3 + Vite + TypeScript
- **UI**：Element Plus
- **状态管理**：Pinia
- **路由**：vue-router（带登录守卫）
- **HTTP**：axios（统一封装 `src/api/http.ts`）
- **样式**：Tailwind CSS（辅以少量 scoped）

### 2.2 后端
- **框架**：Spring Boot 3.2.x（Java 17）
- **鉴权**：Spring Security + JWT（无状态）
- **ORM**：MyBatis（XML Mapper）
- **数据库**：MySQL
- **静态资源**：`/uploads/**` 映射本地 `uploads/`
- **WebSocket**：`/ws/chat?token=...` 即时聊天

### 2.3 前后端交互方式
- 前端通过 Vite proxy 将 `/api` 代理到后端 `http://localhost:8080`
- axios 请求拦截器自动加 `Authorization: Bearer <token>`（token 存储 localStorage：`agrimatch_token`）

---

## 3. 本地运行方式（复刻必需）

### 3.1 依赖
- Node.js（用于前端）
- MySQL（默认 `localhost:3306`，库名 `nonghuitong`）
- Java 17（用于后端）

### 3.2 后端运行
- 端口：`8080`
- 数据库连接：见 `backend/agrimatch-service/src/main/resources/application.yml`
  - 默认：`root/123123`
  - 启动自动执行 SQL 初始化：
    - `classpath:db/agrimatch_core.sql`
    - `classpath:db/nht_product.sql`
    - `classpath:db/nht_product_parameters.sql`

### 3.3 前端运行
- 端口：`5173`
- `frontend/package.json`：
  - `npm run dev` 开发
  - `npm run build` 构建

### 3.4 环境变量（地图/WS）
- 前端（可选）：
  - `VITE_AMAP_JS_KEY`：高德 JS API Key（MapView 加载脚本用）
  - `VITE_AMAP_SECURITY_JS_CODE`：部分 Key 需要的安全密钥（可选）
  - `VITE_WS_BASE_URL`：WebSocket 基地址（可选；不配默认用当前域名）
- 后端（可选）：
  - `AMAP_WEB_KEY`：高德 Web 服务 Key（如后端地理编码/解析地址需要）
- 运费模型（后端）：
  - `agrimatch.freight.rate-per-ton-km`（默认 0.8 元/吨/公里）

---

## 4. 登录态与权限模型（当前实现口径）

### 4.1 路由与登录守卫
- 登录页：`/login`（public）
- 其他页面：需要 token；无 me 时会请求 `/api/auth/me` 取用户信息，失败会清理 token 并跳转 `/login`

### 4.2 角色切换（UI 维度）
- 顶栏有角色分段控件：
  - `buyer`（采购端）
  - `seller`（供应端）
- 该切换主要影响首页搜索跳转与菜单文案展示（业务数据隔离更多由后端 company/user 维度控制）

### 4.3 数据权限（关键点）
- **需求**：
  - 创建：必须登录且用户已绑定 companyId
  - 更新/删除：必须为“发布人本人”（user_id 校验）
  - 过期需求不展示（expire_time）
- **供应**：
  - 创建：必须登录且用户已绑定 companyId
  - 更新/删除：必须为“发布人本人”
  - 列表默认只展示“上架”(status=0)，可筛选下架
- **成交（Deal）**：
  - 仅“需求发布人”可确认成交
  - 需求支持多次成交（部分/全部）
- **评价（Eval）**：
  - 仅成交双方可互评
  - 必须携带 `dealId`；同一成交同一评价人只能评一次

---

## 5. 前端页面（登录后各模块）功能说明

以下为路由与页面一一对应的“产品功能”口径（以 `frontend/src/router/index.ts` 为准）。

### 5.1 `/` 首页（HomeView）
**用途**：平台概览与快速入口（未登录是 landing page，登录后仍可作为概览页）。

**未登录时**
- 顶部展示品牌、导航入口（采购大厅/供应信息/行研观点）与“登录/注册”按钮

**登录后**
- 侧边栏出现（见 App.vue），提供各模块入口
- 顶栏提供角色切换与账号下拉

**核心模块**
- **品类导航**：从 `/api/products/tree` 拉取品类树（左侧分类）
- **全站搜索**：
  - 若当前角色为采购端（buyer）：搜索跳转到 `/supply?categoryName=...`
  - 若当前角色为供应端（seller）：搜索跳转到 `/requirements?categoryName=...`
- **平台统计**：调用 `/api/home/stats` 展示用户/需求/供应数量
- **供求大厅**：
  - 最新供应：`/api/supplies`（按 create_time desc，取前 6）
  - 最新需求：`/api/requirements`（按 create_time desc，取前 6）
- **行研观点**：`/api/posts`（取前 4）
- **“市场热度/新闻”**：页面含 mock 数据（非后端）

### 5.2 `/login` 登录/注册（LoginView）
**用途**：账号登录与注册，并获取 JWT。

- **登录**：用户名+密码 -> `/api/auth/login`，返回 token 存储到 localStorage，再调用 `/api/auth/me`，最后跳转 `/`
- **注册**：用户名+密码+昵称+公司名（可选）-> `/api/auth/register`，返回 token，自动登录并跳转 `/`

### 5.3 `/profile` 我的档案（ProfileView）
**用途**：管理个人信息、角色、公司档案、以及评价（当前为 MVP 入口）。

子模块：
- **我的账号**
  - 查看/更新昵称、手机、微信、companyId、payInfoJson
  - 角色开关：isBuyer/isSeller（保存到后端）
- **公司档案**
  - 创建/更新公司信息（名称、执照号、联系人、电话、微信、地址、lat/lng、多点 locationsJson、bankInfoJson）
  - 营业执照图片上传并预览（后端 `/uploads/**`）
  - 地理编码：`geocodeCompany`（将地址解析成坐标；需要后端支持与 Key）
- **信用评价（MVP）**
  - 提交评价：需要填写 `dealId` + 选择被评价公司 + 星级/文字
  - 展示“我的公司收到的评价列表”

> 注意：评价能力已改为强约束（必须成交双方），当前 UI 用“输入 dealId”方式完成 MVP；后续可升级为“从成交列表选择一条成交后评价”。

### 5.4 `/requirements` 采购需求（RequirementDemoView）
**用途**：发布采购需求、保存需求模板、并查看需求列表。

发布需求：
- 选择品类（TwoLevelCategoryPicker；依赖品类树）
- 数量、包装（散装/袋装）、付款方式（现款/账期）
- 动态参数：
  - 根据 productId 调 `/api/products/{productId}/params` 获取参数配置
  - 下拉型参数支持“新增选项”（调用 `/api/products/params/{paramId}/options`）
  - 组装 paramsJson（包含 params + custom）
- 发布时效：
  - expireMinutes -> 后端转换为 expireTime
  - 过期后列表/详情不展示
- 发布接口：`POST /api/requirements`

需求模板：
- 保存模板：`POST /api/requirement-templates`（保存发布字段快照）
- 我的模板：`GET /api/requirement-templates`
- 删除模板：`DELETE /api/requirement-templates/{id}`

列表与筛选：
- 按品类模糊、按状态筛选
- 排序：create_time / distance（distance 为 Beta：后端用坐标估算并在内存排序）
- 列表字段包含：
  - quantity（总量）
  - remainingQuantity（剩余量 = 总量 - 已成交量汇总）
  - distanceKm（Beta）
  - status：0发布 / 1部分成交 / 2下架 / 3全部成交

### 5.5 `/supply` 供应发布（SupplyView）
**用途**：发布供应、管理上架/下架、查看供应列表，并从供应侧发起成交（MVP）。

发布供应：
- 选择品类、出厂价、交付方式、发货地址、价格规则 JSON、动态参数 paramsJson
- `POST /api/supplies`

供应上/下架（新增）：
- 列表默认只看上架
- 可筛选 status=0/1
- 列表操作：点击“下架/上架” -> `PUT /api/supplies/{id}`，传 `{ status: 1 }` 或 `{ status: 0 }`

到厂价/距离（Beta）：
- 列表展示：
  - distanceKm：按“当前用户公司坐标 -> 供应公司坐标”估算
  - deliveredPrice：`exFactoryPrice + distanceKm * rate-per-ton-km`
- 排序项：`distance` / `delivered_price`（Beta：后端内存排序）

成交（Deal，MVP入口）：
- 供应列表每行有“成交”按钮，弹窗输入：
  - requirementId（需求ID）
  - quantity（成交数量）
  - finalExFactoryPrice（可选，默认供应出厂价）
  - deliveryMode（可选，默认供应交付方式）
- 确认成交：`POST /api/deals`
  - 成功返回 `dealId`（弹窗提示）

### 5.6 `/map` 全域地图（MapView）
**用途**：展示所有公司点位及其供需发布统计。

- 拉取数据：`GET /api/map/companies?keyword=...`
- 支持：
  - 关键字过滤
  - 仅看“有供应/有需求”
  - 地图 marker 点击展示浮窗（公司名/地址/供需数/品类）
  - 右侧公司列表“定位”
- 依赖前端环境变量：`VITE_AMAP_JS_KEY`

### 5.7 `/posts` 观点帖子（PostsView）
**用途**：社区内容，支持发布/浏览/点赞/评论。

- 发布帖子：`POST /api/posts`
- 列表：`GET /api/posts?keyword&orderBy&order`
- 删除（逻辑删除）：`DELETE /api/posts/{id}`
- 点赞：`POST /api/posts/{id}/like`（或同路由下的互动接口，以后端为准）
- 评论：
  - 列表：`GET /api/posts/{id}/comments`
  - 新增：`POST /api/posts/{id}/comments`
- 页面有评论弹窗，支持刷新评论并更新 commentCount

### 5.8 `/points` 积分（PointsView）
**用途**：余额与流水演示，支持充值/兑换。

- 我的余额：`GET /api/points/me`
- 充值：`POST /api/points/recharge`
- 兑换：`POST /api/points/redeem`
- 流水：`GET /api/points/tx`
- 兑换比例提示：1 积分 = 1 元（演示口径）

### 5.9 `/notify` 消息通知（NotifyView）
**用途**：展示点赞/评论等通知，并支持标记已读与跳转链接。

- 列表：`GET /api/notify/my`
- 标记已读：`POST /api/notify/my/read/{id}`
- 全部已读：`POST /api/notify/my/read-all`
- 点击“标记并查看”后如果通知含 link，会跳转到对应前端路由

### 5.10 `/chat` 即时聊天（ChatView）
**用途**：实时会话与历史消息。

- WebSocket：`/ws/chat?token=...`
  - 收到消息后刷新当前会话或 peers
- HTTP：
  - 会话列表：`GET /api/chat/peers`
  - 历史：`GET /api/chat/history?peerUserId=&limit=`
  - 标记已读：`POST /api/chat/read?peerUserId=...`
- 支持搜索用户并发起会话（调用 `GET /api/users/search`）

---

## 6. 后端 API 清单（按模块）

> 返回体统一为 `Result<T>`：`code=0` 成功，`message` 提示，`data` 数据。

### 6.1 鉴权
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### 6.2 用户
- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/me/roles`
- `GET /api/users/search?keyword=&limit=`

### 6.3 公司
- `POST /api/companies`
- `GET /api/companies/my`
- `PUT /api/companies/{id}`
- `POST /api/companies/{id}/license`（文件上传）
- `POST /api/companies/{id}/geocode`（如存在）
- `GET /api/companies/search?keyword=&limit=`

### 6.4 品类
- `GET /api/products/tree`
- `GET /api/products/search?keyword=`
- `POST /api/products/custom`
- `GET /api/products/{productId}/params`
- `POST /api/products/params/{paramId}/options`

### 6.5 采购需求
- `POST /api/requirements`
- `GET /api/requirements/{id}`
- `GET /api/requirements?companyId&userId&categoryName&status&orderBy&order`
- `PUT /api/requirements/{id}`（仅发布人）
- `DELETE /api/requirements/{id}`（仅发布人，逻辑删除）

### 6.6 需求模板
- `POST /api/requirement-templates`
- `GET /api/requirement-templates`
- `DELETE /api/requirement-templates/{id}`

### 6.7 供应
- `POST /api/supplies`
- `GET /api/supplies/{id}`
- `GET /api/supplies?companyId&userId&categoryName&status&orderBy&order`
- `PUT /api/supplies/{id}`（仅发布人；含 status 上下架）
- `DELETE /api/supplies/{id}`（仅发布人，逻辑删除）

### 6.8 成交（MVP）
- `POST /api/deals`：确认成交（仅需求发布人；需绑定 supplyId；需求支持多次成交）
- `GET /api/deals/{id}`：成交详情
- `GET /api/deals?requirementId=...`：按需求查看成交列表

### 6.9 评价（MVP强约束）
- `POST /api/evals`：必须传 `dealId`；仅成交双方互评；同一成交同一评价人只能评一次
- `GET /api/evals/company/{companyId}`：查看公司收到的评价

### 6.10 地图
- `GET /api/map/companies?keyword=...`

### 6.11 帖子/互动
- `POST /api/posts`
- `GET /api/posts`
- `DELETE /api/posts/{id}`
- 点赞/评论：`/api/posts/*`（以实现为准）

### 6.12 积分
- `GET /api/points/me`
- `POST /api/points/recharge`
- `POST /api/points/redeem`
- `GET /api/points/tx`

### 6.13 通知
- `GET /api/notify/my`
- `POST /api/notify/my/read/{id}`
- `POST /api/notify/my/read-all`

### 6.14 首页统计
- `GET /api/home/stats`

---

## 7. 数据库表结构（只列“复刻必须理解”的核心表）

> 初始化脚本：`backend/agrimatch-service/src/main/resources/db/agrimatch_core.sql`

### 7.1 用户与公司
- `sys_user`
  - user_id, user_name, password(BCrypt), nick_name, phonenumber, wechat
  - company_id（绑定公司）
  - is_buyer/is_seller/user_type
  - pay_info_json
- `bus_company`
  - company_name, license_no, license_img_url
  - contacts/phone/wechat
  - address + lat/lng（地图定位核心）
  - locations_json（多点）
  - bank_info_json

### 7.2 供需
- `bus_requirement`
  - company_id/user_id/category_name
  - quantity/packaging/payment_method
  - params_json（动态参数）
  - expire_minutes/expire_time（过期不展示）
  - purchase_lat/purchase_lng/purchase_address
  - status（当前：0发布 1部分成交 2下架 3全部成交）
- `bus_supply`
  - company_id/user_id/category_name
  - ex_factory_price/ship_address/delivery_mode
  - price_rules_json/params_json
  - status（0上架 1下架）

### 7.3 成交（新增）
- `bus_deal`
  - requirement_id/supply_id
  - buyer_company_id/seller_company_id
  - buyer_user_id/seller_user_id
  - quantity/final_ex_factory_price/delivery_mode
  - distance_km/freight_rate_per_ton_km/delivered_price（Beta）
  - status（1已确认 2已取消；当前仅使用 1）

### 7.4 评价（加强约束）
- `bus_order_eval`
  - deal_id（关联成交）
  - from_user_id/to_company_id/stars/comment/images_json
  - unique(deal_id, from_user_id)（防重复评价）

### 7.5 其他模块
- `nht_product` / `nht_product_parameters`：品类树与参数
- `bus_points_account` / `bus_points_tx`：积分
- `bus_notify`：通知
- `bus_chat_message`：聊天消息
- `bus_post` / `bus_post_like` / `bus_post_comment`：帖子与互动
- `bus_contract_template`：合同模板（库已预置；MVP未在页面中完成闭环）

---

## 8. 关键业务流程（可作为验收脚本）

### 8.1 从零到可成交闭环（推荐验收路径）
1. 注册账号（A、B 两个用户）
2. 在 `/profile` 创建公司并绑定 companyId（A 为采购公司，B 为供应公司）
3. B 在 `/supply` 发布供应（上架）
4. A 在 `/requirements` 发布需求（填写总数量）
5. A 在 `/supply` 供应列表找到 B 的供应，点击“成交”，输入 requirementId 与成交数量（可多次成交）
6. 验收：
   - 需求列表显示 remainingQuantity 正确减少
   - status 从 0 -> 1（部分成交）-> 3（全部成交）
7. A 或 B 在 `/profile` 的“信用评价”中填写 dealId，并选择“对方公司”，提交评价
8. 验收：
   - 只有成交双方能评（第三方会被拒绝）
   - 同一成交同一评价人只能评一次

### 8.2 距离/到厂价 Beta（需要坐标）
1. 在公司档案填写 lat/lng（买方与卖方都填）
2. 刷新供应列表
3. 验收：
   - distanceKm 有值
   - deliveredPrice 有值
   - 排序选择 distance 或 delivered_price 后，列表排序生效（内存排序）

### 8.3 地图（需要前端 Key）
1. 配置 `VITE_AMAP_JS_KEY`
2. 在公司档案填写 lat/lng 或地址并触发 geocode
3. 打开 `/map`
4. 验收 marker 与浮窗信息

---

## 9. UI 结构与导航（用于复刻“页面布局”）

- 未登录：
  - 首页顶部 nav + 登录/注册按钮
  - 访问受保护页面会跳转 `/login`
- 登录后：
  - 左侧 Sidebar（蓝底白字）：概览、我的档案、采购需求、全域地图、供应发布、帖子、积分、通知、聊天
  - 顶部 Topbar：角色切换（采购/供应）+ 用户下拉（档案/退出）

---

## 10. 当前版本范围边界（明确“不做什么”，避免复刻偏离）

以下内容在仓库中属于规划或部分占位，不视为“必须完成”：
- 高德真实路径规划距离/运费模板（目前用 Haversine + 固定 rate 的 Beta）
- 完整竞价/投标大厅
- 电子合同签署/结算闭环（合同模板表已存在，但未形成完整流程）
- 更细颗粒权限体系（管理员/企业多员工等）

---

## 11. 复刻实现提示（给 Agent 的执行建议）

- **先实现鉴权与通用 Result/异常**，确保所有 /api/** 受保护，/api/auth/** 放行。
- **先做库表与基础 CRUD**（用户/公司/品类/供需），再做帖子/积分/通知/聊天。
- **交易闭环按本文 MVP**：
  - `bus_deal` 作为成交事实表
  - 需求支持多次成交：用 `sum(deal.quantity)` 计算 remainingQuantity 与状态 0/1/3
  - 评价必须 `dealId` 并做“成交双方”校验
- 列表中的 distance/deliveredPrice 先用 Beta 算法（无需外部 Key）


