# AgriMatch 快速参考

> 常用命令和配置的速查手册

---

## 🛠️ 构建和测试命令

### 前端（Vue 3 + TypeScript + Vite）

```bash
# 开发服务器
cd frontend && npm run dev

# 类型检查
cd frontend && npx vue-tsc -b

# 构建
cd frontend && npm run build
```

### 后端（Java 17 + Spring Boot 3.2.5 + Maven）

```bash
# 构建
cd backend && mvn clean install

# 运行
cd backend/agrimatch-service && mvn spring-boot:run

# 测试
cd backend && mvn test

# 单个测试
cd backend && mvn test -Dtest=ClassName#methodName
```

---

## 📁 关键文件位置

### 配置文件
| 文件 | 路径 | 说明 |
|------|------|------|
| 前端配置 | `frontend/vite.config.ts` | Vite 构建配置 |
| Tailwind 配置 | `frontend/tailwind.config.cjs` | Tailwind CSS 配置 |
| 后端配置 | `backend/pom.xml` | Maven 依赖管理 |
| 服务配置 | `backend/agrimatch-service/pom.xml` | 服务模块配置 |

### 规范文档
| 文件 | 说明 | 目标受众 |
|------|------|---------|
| `.cursorrules` | 核心开发规范 | 开发者 + AI |
| `PROJECT_AI_GUIDE.md` | AI 专用开发指南 | AI Agent |
| `QUICK_REFERENCE.md` | 快速参考（本文件） | 所有 |

---

## 🎨 Tailwind CSS 常用模式

### 按钮
```html
<!-- 主按钮 -->
<button class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-xl font-bold transition-all active:scale-95">
  确认提交
</button>

<!-- 次按钮 -->
<button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-xl font-bold transition-all active:scale-95">
  取消
</button>

<!-- 描边按钮 -->
<button class="border border-gray-200 hover:bg-gray-50 text-gray-700 px-4 py-2 rounded-xl font-bold transition-all active:scale-95">
  详情
</button>
```

### 卡片
```html
<!-- 基础卡片 -->
<div class="bg-white p-6 rounded-2xl border border-gray-100">
  内容
</div>

<!-- 可点击卡片 -->
<div class="bg-white p-6 rounded-2xl border border-gray-100 cursor-pointer hover:shadow-md hover:border-emerald-100 transition-all">
  内容
</div>
```

### 输入框
```html
<input class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm" />
```

### 弹窗
```html
<!-- 遮罩 -->
<div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50">
  <!-- 容器 -->
  <div class="bg-white rounded-[32px] shadow-2xl overflow-hidden max-w-lg mx-auto mt-20">
    <!-- 头部 -->
    <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
      <h2 class="text-lg font-bold text-gray-900">标题</h2>
      <button>×</button>
    </div>
    <!-- 内容 -->
    <div class="p-6">内容</div>
    <!-- 底部 -->
    <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-3">
      <button>取消</button>
      <button>确认</button>
    </div>
  </div>
</div>
```

---

## 📐 项目结构

### 前端目录
```
frontend/src/
├── api/          # API 接口定义（按业务模块拆分）
├── components/   # 通用组件（跨页面复用）
├── composables/  # 组合式函数（逻辑复用）
├── views/        # 页面组件（路由对应）
├── store/        # 状态管理（Pinia）
├── utils/        # 工具函数（纯函数）
└── types/        # TypeScript 类型定义
```

### 后端目录
```
com.agrimatch/
├── controller/   # REST 控制器（薄层，只做参数校验和路由）
├── service/      # 业务逻辑层（核心业务）
├── mapper/       # 数据访问层（MyBatis）
├── domain/       # 实体类（对应数据库表）
├── dto/          # 数据传输对象（请求/响应）
└── common/       # 公共工具和常量
```

---

## 🎯 命名约定

### 前端（Vue 3 + TypeScript）
| 类型 | 约定 | 示例 |
|------|------|------|
| 组件 | PascalCase | `ContractDetailView.vue` |
| 函数 | 动词开头的 camelCase | `loadVehicles`, `handleSubmit` |
| 变量 | camelCase | `loading`, `dialogVisible` |
| 布尔值 | is/has/can/should 前缀 | `isLoading`, `hasError`, `canSubmit` |

### 后端（Java）
| 类型 | 约定 | 示例 |
|------|------|------|
| 类 | PascalCase | `ProductService`, `VehicleServiceImpl` |
| 方法 | camelCase，动词开头 | `tree()`, `search()`, `createCustom()` |
| 变量 | camelCase | `productMapper`, `userId` |
| 常量 | UPPER_SNAKE_CASE | `PARAM_ERROR`, `SERVER_ERROR` |

---

## 📊 UI/UX 快速检查

### 设计原则
- **Less is More**：每个元素必须证明其必要性
- **3-click 规则**：用户应在 ≤ 3 次点击内完成目标
- **60-30-10 色彩法则**：60% 中性，30% 辅助，10% 强调

### 动效规范
- 入场：200-300ms
- 退场：150-200ms
- Hover：150ms
- 缓动：`ease-out`
- 按钮：`active:scale-95`

### 圆角体系
- 外层卡片：`rounded-2xl`
- 按钮/输入：`rounded-xl`
- 弹窗：`rounded-[32px]`
- 头像/徽章：`rounded-full`

---

## 🔒 数据隐私和安全

### 敏感数据脱敏
- 手机号：中间 4 位用 `*` 替代
- 身份证：中间 8 位用 `*` 替代

### 金额格式化
- 保留 2 位小数
- 千分位分隔符
- ¥ 前缀

### 输入验证
- 永远不要信任客户端输入
- 在后端验证所有输入
- 使用友好的错误消息

---

## 🚫 常见陷阱

### 代码质量
- ❌ 使用 `console.log`（仅用于错误）
- ❌ 跳过错误处理
- ❌ 深度嵌套 > 3 层
- ❌ 魔法数字
- ❌ 内联样式
- ❌ 忽略 TypeScript 错误

### 命名
- ❌ 缩写（除非行业通用）：`usr` → `user`
- ❌ 无意义名称：`Component1` → `PricingTable`
- ❌ 非 camelCase 变量名

### 错误处理
```typescript
// ✅ 正确：详细错误信息 + 用户友好提示
try {
  await submitContract(data)
  ElMessage.success('合同提交成功')
} catch (e: any) {
  console.error('Contract submission failed:', e)
  ElMessage.error(e.response?.data?.message || '提交失败，请稍后重试')
}

// ❌ 错误：吞掉错误或只显示技术信息
try { await submitContract(data) } catch (e) { }
```

---

## 📦 常用依赖

### 前端
- Vue 3：`vue`
- 路由：`vue-router`
- 状态管理：`pinia`
- UI 组件：`element-plus`
- 图标：`lucide-vue-next`
- 工具：`@vueuse/core`

### 后端
- Spring Boot：`3.2.5`
- Java：`17`
- 构建：`Maven`
- ORM：`MyBatis`
- 数据库：`MySQL`

---

## 🔍 调试技巧

### 前端
- 使用 `console.error` 记录错误
- 使用 Vue DevTools 检查组件状态
- 使用 Network 标签检查 API 请求

### 后端
- 检查日志文件
- 使用调试器设置断点
- 检查数据库连接和查询

---

## 📞 获取帮助

### 文档
- 核心开发规范：`.cursorrules`
- AI 指南：`PROJECT_AI_GUIDE.md`
- 快速参考：`QUICK_REFERENCE.md`（本文件）

### 工具
- 前端类型检查：`cd frontend && npx vue-tsc -b`
- 后端测试：`cd backend && mvn test`
