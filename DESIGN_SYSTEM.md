# AgriMatch 统一设计系统 (Unified Design System)

> 本文档定义了 AgriMatch 平台的统一设计语言，融合原项目的技术基础与 stitch 文件夹的现代化设计理念。

---

## 🎨 核心设计理念

### 设计原则
1. **Less is More** - 每个元素必须证明其存在的必要性
2. **Neo-Minimal** - 新极简主义：简洁而不简单
3. **Soft Glass** - 柔性玻璃：通过半透明和模糊创造层次感
4. **Card-First** - 卡片优先：信息以卡片形式呈现
5. **Trust First** - 信任优先：通过视觉设计传达专业性和可信度

### 60-30-10 色彩法则
- **60%** 中性色（背景、边框、辅助文字）
- **30%** 辅助色（卡片背景、次要元素）
- **10%** 强调色（主按钮、CTA、关键状态）

---

## 🌈 色彩系统

### 主色调（Primary）
基于原项目的 emerald 绿色，调整为更柔和、现代的色调：

```css
--primary-50: #ecfdf5
--primary-100: #d1fae5
--primary-200: #a7f3d0
--primary-300: #6ee7b7
--primary-400: #34d399
--primary-500: #10b981  /* 主色 */
--primary-600: #059669  /* 主按钮、CTA */
--primary-700: #047857
--primary-800: #065f46
--primary-900: #064e3b
```

### 背景色系统（Background）
融合 stitch 的柔和背景色：

```css
--bg-light: #f6f8f6           /* 浅灰绿色 - 白天模式 */
--bg-light-hover: #e8ebe8      /* 悬停/激活 */
--bg-dark: #102210             /* 深绿色 - 夜间模式 */
--bg-dark-hover: #1a351a      /* 深色悬停 */
--bg-white: #ffffff            /* 纯白卡片 */
--bg-gray: #f9fafb            /* 中性灰 */
```

### 中性色系统（Neutral）

```css
--neutral-50: #fafafa
--neutral-100: #f5f5f5
--neutral-200: #e5e5e5
--neutral-300: #d4d4d4
--neutral-400: #a3a3a3
--neutral-500: #737373
--neutral-600: #525252
--neutral-700: #404040
--neutral-800: #262626
--neutral-900: #171717
```

### 语义色（Semantic）

```css
--success: #10b981           /* 成功 - 使用 primary */
--warning: #f59e0b           /* 警告 - amber */
--error: #ef4444             /* 错误 - red */
--info: #6366f1              /* 信息 - indigo */
```

### 透明度（Opacity）

```css
--opacity-subtle: rgba(0, 0, 0, 0.03)
--opacity-light: rgba(0, 0, 0, 0.06)
--opacity-medium: rgba(0, 0, 0, 0.1)
--opacity-heavy: rgba(0, 0, 0, 0.15)
```

---

## 📐 圆角系统（Border Radius）

```css
--radius-sm: 0.5rem    /* 8px - 小元素 */
--radius-md: 0.75rem   /* 12px - 中等元素 */
--radius-lg: 1rem      /* 16px - 卡片、按钮 */
--radius-xl: 1.25rem   /* 20px - 大卡片 */
--radius-2xl: 1.5rem   /* 24px - 主要卡片、弹窗 */
--radius-full: 9999px   /* 完全圆角 */
```

**使用场景**：
- `radius-sm` - 标签、徽章
- `radius-md` - 输入框、小按钮
- `radius-lg` - 按钮、标签页
- `radius-xl` - 卡片内元素
- `radius-2xl` - 主要卡片、弹窗

---

## 📝 字体系统（Typography）

### 字体族
```css
font-family: 'Inter', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

### 字体层级

```css
/* 标题层级 */
--text-xs: 0.75rem          /* 12px - 辅助标签 */
--text-sm: 0.875rem         /* 14px - 次要文本 */
--text-base: 1rem           /* 16px - 正文 */
--text-lg: 1.125rem         /* 18px - 小标题 */
--text-xl: 1.25rem          /* 20px - 卡片标题 */
--text-2xl: 1.5rem          /* 24px - 页面标题 */
--text-3xl: 1.875rem        /* 30px - 主标题 */
--text-4xl: 2.25rem         /* 36px - Hero标题 */
```

### 字重（Font Weight）
```css
--font-normal: 400
--font-medium: 500
--font-semibold: 600
--font-bold: 700
--font-black: 900
```

### 行高（Line Height）
```css
--leading-tight: 1.25
--leading-snug: 1.375
--leading-normal: 1.5
--leading-relaxed: 1.625
```

---

## 🎭 阴影系统（Shadow）

```css
--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05), 0 1px 3px 0 rgba(0, 0, 0, 0.1);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.07), 0 4px 6px -2px rgba(0, 0, 0, 0.04);
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
--shadow-2xl: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
--shadow-inner: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06);
```

### 特殊阴影

```css
--shadow-primary: 0 4px 14px rgba(16, 185, 129, 0.25);  /* 主色阴影 */
--shadow-glow: 0 0 20px rgba(16, 185, 129, 0.15);      /* 发光效果 */
```

---

## 📦 间距系统（Spacing）

```css
--space-1: 0.25rem   /* 4px */
--space-2: 0.5rem    /* 8px */
--space-3: 0.75rem   /* 12px */
--space-4: 1rem      /* 16px */
--space-5: 1.25rem   /* 20px */
--space-6: 1.5rem    /* 24px */
--space-8: 2rem      /* 32px */
--space-10: 2.5rem   /* 40px */
--space-12: 3rem     /* 48px */
--space-16: 4rem     /* 64px */
```

### 布局间距（Container）

```css
--container-sm: 640px
--container-md: 768px
--container-lg: 1024px
--container-xl: 1280px
--container-2xl: 1440px
```

---

## 🔘 按钮系统

### 主按钮（Primary Button）
```html
<button class="px-6 py-3 bg-primary-600 text-white rounded-xl
  font-semibold shadow-lg shadow-primary/25
  hover:bg-primary-700 hover:scale-105
  active:scale-95 transition-all duration-200">
  立即登录
</button>
```

### 次按钮（Secondary Button）
```html
<button class="px-6 py-3 bg-white border border-neutral-200 text-neutral-700 rounded-xl
  font-semibold shadow-sm
  hover:bg-neutral-50 hover:border-neutral-300
  active:scale-95 transition-all duration-200">
  取消
</button>
```

### 文字按钮（Text Button）
```html
<button class="text-primary-600 font-semibold
  hover:text-primary-700 hover:underline
  active:text-primary-800 transition-all duration-200">
  了解更多
</button>
```

### 图标按钮（Icon Button）
```html
<button class="p-3 rounded-xl bg-neutral-100 hover:bg-neutral-200
  active:scale-95 transition-all duration-200">
  <svg>...</svg>
</button>
```

### 按钮尺寸
```css
--btn-sm: px-4 py-2 text-sm
--btn-md: px-6 py-3 text-base
--btn-lg: px-8 py-4 text-lg
```

---

## 📥 输入框系统

### 标准输入框
```html
<input class="w-full px-4 py-3 bg-white border border-neutral-200 rounded-xl
  text-neutral-900 placeholder:text-neutral-400
  focus:outline-none focus:ring-2 focus:ring-primary-500/50
  transition-all duration-200" />
```

### 带图标的输入框
```html
<div class="relative">
  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
    <svg>...</svg>
  </span>
  <input class="w-full pl-12 pr-4 py-3 bg-white border border-neutral-200 rounded-xl
    focus:outline-none focus:ring-2 focus:ring-primary-500/50" />
</div>
```

### 输入框状态
- **默认** - 边框 `neutral-200`
- **聚焦** - 边框 `primary-500`，外环 `primary-500/50`
- **错误** - 边框 `error`，红色提示
- **禁用** - 背景 `neutral-100`，文字 `neutral-400`

---

## 🃏 卡片系统

### 基础卡片
```html
<div class="bg-white dark:bg-neutral-900 rounded-2xl
  border border-neutral-200 dark:border-neutral-800
  shadow-md p-6">
  卡片内容
</div>
```

### 可交互卡片
```html
<div class="bg-white dark:bg-neutral-900 rounded-2xl
  border border-neutral-200 dark:border-neutral-800
  shadow-md hover:shadow-lg hover:border-primary-300
  p-6 cursor-pointer transition-all duration-300">
  卡片内容
</div>
```

### 信息卡片
```html
<div class="bg-white dark:bg-neutral-900 rounded-2xl
  border border-neutral-200 dark:border-neutral-800
  shadow-sm p-6">
  <div class="flex items-center gap-3 mb-4">
    <div class="w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center">
      <svg class="text-primary-600">...</svg>
    </div>
    <div>
      <h3 class="text-xl font-bold">卡片标题</h3>
      <p class="text-sm text-neutral-500">卡片描述</p>
    </div>
  </div>
</div>
```

---

## 🏷️ 标签与徽章

### 标签（Tag）
```html
<span class="px-3 py-1 bg-primary-100 text-primary-700
  rounded-full text-xs font-semibold uppercase tracking-wider">
  标签
</span>
```

### 状态标签
- **成功** - `bg-success/10 text-success`
- **警告** - `bg-warning/10 text-warning`
- **错误** - `bg-error/10 text-error`
- **信息** - `bg-info/10 text-info`

### 徽章（Badge）
```html
<span class="absolute -top-2 -right-2 px-2 py-0.5 bg-error text-white
  rounded-full text-xs font-bold">
  3
</span>
```

---

## 🖼️ 图片与图标

### 头像（Avatar）
```html
<div class="w-10 h-10 rounded-full border-2 border-primary-200 overflow-hidden">
  <img src="..." class="w-full h-full object-cover" />
</div>
```

### 图标系统
**推荐图标库**：`lucide-vue-next`（与原项目保持一致）

```vue
<script setup>
import { Search, Bell, Settings, User } from 'lucide-vue-next'
</script>

<template>
  <Search class="w-5 h-5 text-neutral-600" />
  <Bell class="w-5 h-5 text-neutral-600" />
  <Settings class="w-5 h-5 text-neutral-600" />
  <User class="w-5 h-5 text-neutral-600" />
</template>
```

---

## 📊 数据展示

### 表格
```html
<table class="w-full">
  <thead class="bg-neutral-50">
    <tr>
      <th class="px-6 py-3 text-left text-sm font-semibold text-neutral-700">列1</th>
      <th class="px-6 py-3 text-left text-sm font-semibold text-neutral-700">列2</th>
    </tr>
  </thead>
  <tbody class="divide-y divide-neutral-200">
    <tr class="hover:bg-neutral-50">
      <td class="px-6 py-4 text-sm text-neutral-900">数据1</td>
      <td class="px-6 py-4 text-sm text-neutral-900">数据2</td>
    </tr>
  </tbody>
</table>
```

### 统计卡片
```html
<div class="bg-white dark:bg-neutral-900 rounded-2xl
  border border-neutral-200 dark:border-neutral-800
  shadow-sm p-6">
  <div class="flex items-center gap-3 mb-2">
    <div class="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center">
      <svg class="text-primary-600 w-5 h-5">...</svg>
    </div>
    <p class="text-sm text-neutral-500 font-medium uppercase tracking-wider">指标名称</p>
  </div>
  <p class="text-3xl font-black text-neutral-900">12,450</p>
</div>
```

---

## 🌙 暗色模式

### 暗色模式适配
所有组件必须支持暗色模式，通过 `dark:` 前缀适配：

```html
<div class="bg-white dark:bg-neutral-900
  text-neutral-900 dark:text-white">
  内容
</div>
```

### 暗色模式色彩映射
- `bg-white` → `dark:bg-neutral-900`
- `bg-neutral-100` → `dark:bg-neutral-800`
- `text-neutral-900` → `dark:text-white`
- `text-neutral-500` → `dark:text-neutral-400`
- `border-neutral-200` → `dark:border-neutral-800`

---

## 🎬 动画与过渡

### 标准过渡时间
```css
--duration-fast: 150ms
--duration-normal: 200ms
--duration-slow: 300ms
```

### 标准缓动函数
```css
--ease-out: cubic-bezier(0.25, 0.46, 0.45, 0.94)
--ease-in-out: cubic-bezier(0.4, 0, 0.2, 1)
```

### 常用动画效果

```css
/* 悬停缩放 */
.hover\:scale-105:hover {
  transform: scale(1.05);
}

/* 点击缩放 */
.active\:scale-95:active {
  transform: scale(0.95);
}

/* 淡入淡出 */
.fade-enter-active, .fade-leave-active {
  transition: opacity var(--duration-normal) var(--ease-out);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 滑入滑出 */
.slide-enter-active, .slide-leave-active {
  transition: transform var(--duration-slow) var(--ease-out);
}
.slide-enter-from {
  transform: translateX(-100%);
}
.slide-leave-to {
  transform: translateX(100%);
}
```

---

## 📐 布局模式

### 页面布局
```html
<div class="min-h-screen flex flex-col">
  <header class="sticky top-0 z-50">
    <!-- 导航栏 -->
  </header>
  <main class="flex-1 max-w-[1440px] mx-auto px-6 py-8">
    <!-- 主内容 -->
  </main>
  <footer>
    <!-- 页脚 -->
  </footer>
</div>
```

### 两栏布局
```html
<div class="grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-6">
  <aside>
    <!-- 侧边栏 -->
  </aside>
  <main>
    <!-- 主内容 -->
  </main>
</div>
```

### 三栏布局
```html
<div class="grid grid-cols-1 lg:grid-cols-[280px_1fr_320px] gap-6">
  <aside>
    <!-- 左侧栏 -->
  </aside>
  <main>
    <!-- 主内容 -->
  </main>
  <aside>
    <!-- 右侧栏 -->
  </aside>
</div>
```

---

## 📱 响应式断点

```css
--breakpoint-sm: 640px
--breakpoint-md: 768px
--breakpoint-lg: 1024px
--breakpoint-xl: 1280px
--breakpoint-2xl: 1440px
```

### 响应式模式
```html
<!-- 移动优先 -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  <!-- 内容 -->
</div>

<!-- 隐藏/显示 -->
<div class="hidden md:block">
  <!-- 桌面显示 -->
</div>
<div class="block md:hidden">
  <!-- 移动显示 -->
</div>
```

---

## ✅ 组件设计检查清单

每个新组件必须通过以下检查：

### 视觉设计
- [ ] 使用设计系统中的标准色彩
- [ ] 使用设计系统中的标准圆角
- [ ] 使用设计系统中的标准阴影
- [ ] 使用设计系统中的标准间距
- [ ] 使用设计系统中的标准字体层级

### 交互设计
- [ ] 有悬停状态
- [ ] 有激活状态
- [ ] 有焦点状态
- [ ] 有禁用状态
- [ ] 动画时长在 150-300ms 之间

### 响应式设计
- [ ] 支持移动端
- [ ] 支持平板端
- [ ] 支持桌面端
- [ ] 使用响应式断点

### 可访问性
- [ ] 有适当的 ARIA 标签
- [ ] 支持键盘导航
- [ ] 有适当的焦点指示
- [ ] 颜色对比度符合 WCAG AA 标准

### 暗色模式
- [ ] 支持亮色模式
- [ ] 支持暗色模式
- [ ] 所有状态都有暗色适配

---

## 🔧 Tailwind 配置建议

更新 `frontend/tailwind.config.cjs`：

```javascript
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          900: '#064e3b',
        },
        bg: {
          light: '#f6f8f6',
          lightHover: '#e8ebe8',
          dark: '#102210',
          darkHover: '#1a351a',
          white: '#ffffff',
          gray: '#f9fafb',
        },
        neutral: {
          50: '#fafafa',
          100: '#f5f5f5',
          200: '#e5e5e5',
          300: '#d4d4d4',
          400: '#a3a3a3',
          500: '#737373',
          600: '#525252',
          700: '#404040',
          800: '#262626',
          900: '#171717',
        },
      },
      borderRadius: {
        sm: '0.5rem',
        md: '0.75rem',
        lg: '1rem',
        xl: '1.25rem',
        '2xl': '1.5rem',
        full: '9999px',
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05), 0 1px 3px 0 rgba(0, 0, 0, 0.1)',
        md: '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
        lg: '0 10px 15px -3px rgba(0, 0, 0, 0.07), 0 4px 6px -2px rgba(0, 0, 0, 0.04)',
        xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05)',
        primary: '0 4px 14px rgba(16, 185, 129, 0.25)',
        glow: '0 0 20px rgba(16, 185, 129, 0.15)',
      },
      fontSize: {
        '3xl': '1.875rem',
        '4xl': '2.25rem',
      },
      spacing: {
        '16': '4rem',
        '18': '4.5rem',
        '20': '5rem',
        '24': '6rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: []
}
```

---

## 📋 实施计划

### 阶段 1：基础设施（1-2周）
1. ✅ 更新 `tailwind.config.cjs`，融合设计系统
2. ✅ 创建 `src/styles/design-system.css`，定义 CSS 变量
3. ✅ 创建基础组件（Button, Input, Card, Tag, Badge）
4. ✅ 创建布局组件（Header, Footer, Sidebar）

### 阶段 2：页面迁移（2-4周）
1. ✅ 选择 2-3 个代表性页面进行重构（如登录页、首页）
2. ✅ 验证设计系统在实际页面中的效果
3. ✅ 根据反馈微调设计系统
4. ✅ 逐步迁移其他页面

### 阶段 3：持续优化（持续）
1. ✅ 收集用户反馈
2. ✅ 迭代设计系统
3. ✅ 更新设计文档
4. ✅ 培训团队成员

---

## 🎯 关键决策点

### 为什么选择融合方案？
1. **保持技术连续性** - 保留原项目的 TypeScript + Vue 3 + Tailwind 技术栈
2. **降低迁移成本** - 不需要重写所有代码
3. **渐进式改进** - 可以逐步迁移，不影响业务

### 为什么保留 lucide 图标？
1. **一致性** - 原项目已大量使用
2. **类型安全** - TypeScript 支持
3. **可维护性** - 单一图标库，便于管理

### 为什么调整色彩？
1. **更柔和** - stitch 的背景色 `#f6f8f6` 比原项目的纯白更舒适
2. **更现代** - 使用更丰富的灰度层次
3. **更专业** - 通过细微的绿色倾向强化品牌认知

---

## 📚 参考资料

- **原项目规范**：`.cursorrules`（353行）
- **原项目配置**：`frontend/tailwind.config.cjs`
- **设计参考**：`stitch/` 文件夹（28个页面设计）
- **Vue 3 组件库**：Element Plus（已集成）
- **图标库**：lucide-vue-next（已集成）

---

## 🔄 版本历史

- **v1.0** (2025-01-18) - 初始版本，基于 stitch 设计与原项目融合

---

**文档维护者**：AgriMatch 设计团队
**最后更新**：2025-01-18
