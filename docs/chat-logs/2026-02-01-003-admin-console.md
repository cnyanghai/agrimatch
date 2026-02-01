# 聊天记录：管理后台控制台

- **日期**：2026-02-01
- **序号**：003
- **主要任务**：实现完整的管理后台控制台，包含仪表盘、用户管理、企业管理、信息审核、话题管理

## 对话摘要

基于上一轮对话中设计并批准的管理后台方案，实现了完整的管理控制台系统。包括独立的管理布局、6个管理模块的前后端完整实现。

## 完成的工作

1. **基础设施**
   - 创建 `AdminUtil.java` — 共享管理员权限验证工具
   - 创建 `AdminLayout.vue` — 管理后台独立布局（深色侧边栏 + 主内容区）
   - 创建 `src/api/admin.ts` — 全部管理端 API 函数与接口定义
   - 更新 `router/index.ts` — 嵌套 `/admin` 路由 + `requiresAdmin` 路由守卫
   - 更新 `App.vue` — 侧边栏「发卡管理」改为「管理后台」链接到 `/admin`

2. **后端 — DTOs**
   - `AdminDashboardResponse.java` — 8项统计数据
   - `AdminUserResponse.java` — 用户列表含企业名
   - `AdminCompanyResponse.java` — 企业列表含认证状态

3. **后端 — Mapper**
   - `AdminMapper.java` — 接口定义（20+ 方法）
   - `AdminMapper.xml` — 全部 SQL 查询（仪表盘统计、用户/企业/供应/采购/话题 CRUD）

4. **后端 — Controllers**
   - `AdminDashboardController.java` — `GET /api/admin/dashboard`
   - `AdminUserController.java` — 用户列表/切换管理员/启用禁用
   - `AdminCompanyController.java` — 企业列表/认证/拒绝
   - `AdminModerationController.java` — 供应+采购 下架/恢复
   - `AdminPostController.java` — 话题列表/删除
   - 重构 `AdminPointsController.java` — 使用共享 `AdminUtil`

5. **前端 — 管理页面**
   - `AdminDashboardView.vue` — 2×4 统计卡片网格
   - `AdminUsersView.vue` — 搜索 + 分页列表 + 管理员/禁用操作
   - `AdminCompaniesView.vue` — 搜索 + 认证状态筛选 + 认证/拒绝操作
   - `AdminListingsView.vue` — 供应/采购 Tab + 搜索 + 下架/恢复操作
   - `AdminPostsView.vue` — 搜索 + 分页列表 + 删除操作

6. **数据库迁移**
   - `V20260201_admin_console.sql` — `bus_company.verified_status` 字段（已执行）

## 修改的文件

### 新建文件
- `backend/.../admin/AdminUtil.java`
- `backend/.../admin/dto/AdminDashboardResponse.java`
- `backend/.../admin/dto/AdminUserResponse.java`
- `backend/.../admin/dto/AdminCompanyResponse.java`
- `backend/.../admin/mapper/AdminMapper.java`
- `backend/.../admin/controller/AdminDashboardController.java`
- `backend/.../admin/controller/AdminUserController.java`
- `backend/.../admin/controller/AdminCompanyController.java`
- `backend/.../admin/controller/AdminModerationController.java`
- `backend/.../admin/controller/AdminPostController.java`
- `backend/.../resources/mapper/AdminMapper.xml`
- `backend/.../resources/db/V20260201_admin_console.sql`
- `frontend/src/api/admin.ts`
- `frontend/src/views/admin/AdminLayout.vue`
- `frontend/src/views/admin/AdminDashboardView.vue`
- `frontend/src/views/admin/AdminUsersView.vue`
- `frontend/src/views/admin/AdminCompaniesView.vue`
- `frontend/src/views/admin/AdminListingsView.vue`
- `frontend/src/views/admin/AdminPostsView.vue`

### 修改文件
- `backend/.../points/controller/AdminPointsController.java` — 使用共享 AdminUtil
- `frontend/src/router/index.ts` — 嵌套管理路由 + 管理员守卫
- `frontend/src/App.vue` — 侧边栏管理入口

## 技术决策

- 管理布局独立于用户侧，`/admin` 下使用 `AdminLayout.vue` 作为嵌套路由容器
- 深色侧边栏（slate-900）与用户侧白色侧边栏视觉区分
- 所有管理端 API 统一使用 `AdminUtil.requireAdmin()` 进行权限检查
- 企业认证使用 `verified_status` 字段：0=未审核, 1=已认证, 2=已拒绝
- 供应/采购下架通过 `status=2` 实现，恢复回 `status=0`
- 话题删除采用软删除（`is_deleted=1`）
- 前端管理员守卫在 `router.beforeEach` 中检查 `auth.isAdmin`

## API 端点总览

```
GET  /api/admin/dashboard
GET  /api/admin/users?keyword=&page=&size=
PUT  /api/admin/users/{id}/toggle-admin
PUT  /api/admin/users/{id}/toggle-status
GET  /api/admin/companies?keyword=&status=&page=&size=
PUT  /api/admin/companies/{id}/verify
PUT  /api/admin/companies/{id}/reject
GET  /api/admin/supplies?keyword=&page=&size=
PUT  /api/admin/supplies/{id}/takedown
PUT  /api/admin/supplies/{id}/restore
GET  /api/admin/requirements?keyword=&page=&size=
PUT  /api/admin/requirements/{id}/takedown
PUT  /api/admin/requirements/{id}/restore
GET  /api/admin/posts?keyword=&page=&size=
DELETE /api/admin/posts/{id}
GET  /api/admin/jd-redeems?status=
POST /api/admin/jd-redeems/{id}/fulfill
POST /api/admin/jd-redeems/{id}/fail
```

## 待办事项

- [ ] 端到端验证完整流程
- [ ] 测试管理员权限守卫（非管理员访问 /admin 应重定向）
