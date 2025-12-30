# 项目开发框架与开发逻辑说明

## 1. 总览
该项目为“前后端分离”的本地演示工程：
- `frontend/`：Vue3 + Vite + TypeScript 的 SPA
- `backend/`：Spring Boot 3.2 + MyBatis + MySQL 的 REST API（Maven 多模块）

## 2. 目录结构（关键）
- `backend/`
  - `pom.xml`（父工程：Java 17，Spring Boot 3.2.5）
  - `agrimatch-common/`（通用：统一返回、异常等）
  - `agrimatch-service/`（主服务：controller/service/mapper/security/config）
- `frontend/`
  - `src/api/`（按业务分文件封装 API）
  - `src/store/`（Pinia：auth/app）
  - `src/router/`（路由与登录守卫）
  - `src/views/`（页面）
  - `vite.config.ts`（本地 proxy：`/api -> http://localhost:8080`）

## 3. 前端框架与关键约定

### 3.1 技术栈
- Vue 3 + TypeScript + Vite
- UI：Element Plus
- 状态：Pinia
- 路由：vue-router
- 请求：axios

### 3.2 API 访问与登录态
- axios 实例：`src/api/http.ts`
  - request interceptor：若 Pinia 中存在 token，则自动加头：
    - `Authorization: Bearer <token>`
- token 存储：`localStorage` key = `agrimatch_token`
- 本地联调：
  - axios `baseURL` 为空，依赖 Vite proxy
  - `vite.config.ts`：`/api` 转发到 `http://localhost:8080`

### 3.3 路由守卫
- `/login` 为 public 页面
- 其他页面：
  - 无 token -> 跳转 `/login`
  - 有 token 但无 me -> 调用 `/api/auth/me` 拉取当前用户
  - 拉取失败 -> 清理 token 并跳转 `/login`

## 4. 后端框架与关键约定

### 4.1 技术栈
- Java 17
- Spring Boot 3.2.5
- Spring Web + Validation
- Spring Security（无状态）
- JWT：jjwt 0.12.5
- MyBatis + MySQL

### 4.2 分层结构（典型）
- controller：REST 接口（`/api/*`）
- service：业务逻辑
- mapper + `mapper/*.xml`：MyBatis SQL
- domain：表实体
- dto：请求/响应 DTO

### 4.3 统一返回与异常
- 统一返回体：`Result<T>`（code/message/data）
  - `code=0` 表示成功
- 全局异常处理：`GlobalExceptionHandler`
  - `ApiException` -> 返回对应 code/message
  - 参数校验异常 -> 参数错误
  - 未捕获异常 -> 服务器错误

### 4.4 鉴权与安全
- `SecurityConfig`：
  - `/api/auth/**` 放行
  - `/api/**` 必须 authenticated
  - Stateless（不使用 session）
  - `JwtAuthFilter`：解析 Bearer Token，将 `LoginUser(userId,userName)` 写入 `SecurityContext`
- 业务获取 userId：
  - `SecurityUtil.requireUserId(authentication)`

### 4.5 跨域与文件访问
- CORS：仅对 `/api/**` 放开
  - 允许来源：`http://localhost:5173`、`http://127.0.0.1:5173`
- 文件上传：
  - `FileStorageService` 保存到本地 `uploads/`
  - `WebCorsConfig` 将 `/uploads/**` 映射到本地 `uploads` 目录，实现静态访问

## 5. 数据库与初始化逻辑

### 5.1 连接与初始化
`application.yml`：
- datasource: `jdbc:mysql://localhost:3306/nonghuitong ...`
- `spring.sql.init.mode=always`
- `schema-locations`：
  - `classpath:db/agrimatch_core.sql`
  - `classpath:db/nht_product.sql`
- `continue-on-error=true`（脚本重复执行时尽量不阻塞启动）

### 5.2 核心表与用途（摘要）
- `sys_user`：用户基础信息（BCrypt 密码、角色、company_id）
- `bus_company`：公司档案与资质、坐标/多点信息
- `bus_requirement`：采购需求（含过期/逻辑删除）
- `bus_requirement_template`：需求模板
- `bus_supply`：供应发布
- `nht_product`：品类树
- 其它：帖子/通知/积分/聊天等（以实际表/mapper 为准）

## 6. 端到端业务链路（开发逻辑示例）

### 6.1 注册/登录
- 前端：
  - register/login -> `/api/auth/register` 或 `/api/auth/login`
  - 保存 token -> fetchMe -> 跳转首页
- 后端：
  - register：创建 sys_user（password BCrypt），生成 JWT
  - login：校验密码匹配后生成 JWT

### 6.2 “我的档案”与“公司档案”
- 前端 ProfileView：
  - `getMe` -> 填充账号档案
  - `getMyCompany` -> 填充公司档案
  - `uploadCompanyLicense` -> 后端保存文件并更新公司 `license_img_url`

### 6.3 采购需求与模板
- 发布需求：
  - 前端组装字段（品类、数量、付款、paramsJson、expireMinutes、坐标等）
  - 后端将 expireMinutes 转为 expireTime（过期后 SQL 不展示）
- 模板：
  - 前端保存 JSON 快照为 templateJson
  - 后端按 owner_user_id 进行“我的模板”隔离

### 6.4 供应发布
- 前端 SupplyView：
  - 选择二级品类 -> 拉取“品类参数” -> 组装 paramsJson -> 发布供应
- 后端：
  - 创建供应要求已登录且用户已绑定 companyId
  - 更新/删除应校验登录用户与数据所有权（避免越权）


