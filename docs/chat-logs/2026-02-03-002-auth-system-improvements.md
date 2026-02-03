# 聊天记录：认证系统全面改进

- **日期**：2026-02-03
- **序号**：002
- **主要任务**：分析并改进注册登录系统的安全性和用户体验

## 对话摘要

延续上一次会话（P2 功能实施后），用户要求对注册登录页面进行表现层和逻辑层的全面分析。分析完成后，用户明确了一个关键业务规则（不区分采购商/供应商），并要求按分析结果进行全面改进。通过 3 个并行 Agent 同时改进后端、前端和 App 端，所有类型检查通过。

## 完成的工作

### 1. 认证系统分析
- 对前端 AuthDialog.vue、store/auth.ts、后端 AuthController、AuthServiceImpl、SmsCodeService、CaptchaService 等进行了全面审查
- 发现并报告了多项安全隐患和 UX 改进点

### 2. 业务规则确认
- 用户明确：**不区分采购商/供应商**，任何用户都可以发布采购和供应信息
- 所谓的"采购商"/"供应商"标签是根据用户是否发布过相应信息来自动判定的

### 3. 后端安全修复 (#26)
- **SMS 验证码绕过**：将硬编码的 `000000` 绕过改为环境变量控制（`agrimatch.sms.dev-bypass`），默认开发模式启用
- **验证码生成**：`CaptchaService` 从 `Random` 改为 `SecureRandom`
- **自动注册**：`loginByPhone` 方法在手机号不存在时自动创建用户（isBuyer=0, isSeller=0）
- **登录锁定**：添加暴力破解防护，5 次失败后锁定 15 分钟
- **密码重置**：新增 `/api/auth/reset-password` 端点，基于 SMS 验证码重置密码
- 新增 `ResetPasswordRequest` DTO

### 4. 前端认证改进 (#27)
- **简化注册表单**：移除 nickName、userType、companyName、companyType 字段
- **确认密码**：注册时新增密码确认输入框
- **密码强度指示**：基于复杂度评分（长度+大小写+数字+特殊字符），0-3 级别
- **忘记密码**：完整的 SMS 验证码重置密码流程（发送验证码→验证→设新密码）
- **移除角色硬编码**：注册不再设置 buyer/seller 角色

### 5. App 登录页改进 (#28)
- 副标题改为"验证手机号即可登录，新用户自动注册"
- 手机号格式实时验证（非 11 位显示错误提示）
- 发送验证码按钮在手机号无效时禁用
- 改进返回导航逻辑（检查页面栈深度）
- 添加"其他方式"区域，提示密码登录请访问网页版

## 修改的文件

### 新建文件
- `backend/.../auth/dto/ResetPasswordRequest.java` - 密码重置请求 DTO

### 后端修改
- `backend/.../auth/service/SmsCodeService.java` - 环境变量控制 SMS 绕过
- `backend/.../auth/service/CaptchaService.java` - SecureRandom 替换 Random
- `backend/.../auth/service/impl/AuthServiceImpl.java` - 自动注册、登录锁定、密码重置实现
- `backend/.../auth/service/AuthService.java` - 接口添加 resetPassword 方法
- `backend/.../auth/controller/AuthController.java` - 添加 reset-password 端点
- `backend/.../resources/application.yml` - 添加 dev-bypass 配置

### 前端修改
- `frontend/src/store/auth.ts` - 简化注册、添加密码重置 actions
- `frontend/src/components/AuthDialog.vue` - 确认密码、密码强度、忘记密码流程

### App 修改
- `app/src/pages/auth/login.vue` - UX 改进（验证、提示、导航）

## 技术决策

1. SMS 绕过使用环境变量而非完全移除，保留开发便利性
2. 登录锁定使用内存存储（ConcurrentHashMap），适合单实例部署
3. 密码强度使用客户端计算（复杂度评分），不依赖后端
4. App 端使用 SMS 自动注册模式（无独立注册流程），简化移动端体验
5. 密码重置使用 SMS type=3 区分于登录验证码（type=2）

## 待办事项

- [ ] 真机调试测试所有功能
- [ ] iOS/Android 适配测试
- [ ] 性能优化（分包加载、图片懒加载）
- [ ] 深色模式支持（可选）
- [ ] 推送通知集成（uni-push 2.0）
- [ ] 应用商店上架准备
- [ ] 登录锁定机制如需分布式支持，迁移到 Redis
