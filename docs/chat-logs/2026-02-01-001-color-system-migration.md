# 聊天记录：颜色体系迁移

- **日期**：2026-02-01
- **序号**：001
- **主要任务**：将前端所有原生 Tailwind 颜色迁移到项目定义的语义化 token

## 对话摘要

按照 8 个阶段的迁移计划，将前端代码中所有原生 Tailwind 颜色类名替换为项目自定义的语义化 token，涉及 102 个文件、约 2600 处修改。

## 完成的工作

1. **Phase 0**: 补全 `warning-*`/`error-*`/`success-*` 缺失色阶（100-400, 800-900），删除冗余 `primary-*` 调色板，更新 BaseButton.vue 中 `primary-` → `brand-`，更新 design-system.css CSS 变量命名
2. **Phase 1**: `blue-*` → `action-*`（23 文件），`indigo-*` → `action-*`（5 文件）
3. **Phase 2**: `amber-*` → `warning-*`（56 文件，175 处）
4. **Phase 3**: `red-*` → `error-*` 选择性替换（30 文件，81 处），保留印章/必填星号/爱心图标的 `red-*`
5. **Phase 4**: `orange-*` → `accent-*`（全部替换）
6. **Phase 5**: 未定义色迁移 — `purple-*` → `action-*`，`teal-*` → `brand-*`，`emerald-*` → `brand-*`，`pink-*` → `accent-*`，`rose-*` → `accent-*`，`sky-*` → `action-*`；更新头像颜色数组
7. **Phase 6**: `gray-*` → `neutral-*`（约 2195 处）
8. **Phase 7**: BusinessMapView.vue 中硬编码 HEX 替换为品牌 token 对应的 hex 值

## 修改的文件

- `frontend/tailwind.config.cjs` — 补全色阶，删除 primary
- `frontend/src/styles/design-system.css` — CSS 变量 `--primary-*` → `--brand-*`
- `frontend/src/components/ui/BaseButton.vue` — `primary-` → `brand-`
- `frontend/src/types/chat/conversation.ts` — 头像颜色数组更新，gray → neutral
- `frontend/src/views/BusinessMapView.vue` — 硬编码 HEX 迁移
- 以及其余 ~97 个 `.vue`/`.ts` 文件的颜色类名迁移

## 技术决策

1. **Phase 3 选择性保留 `red-*`**: 印章视觉效果、表单必填星号 `*`、爱心/收藏图标、地图定位图标、财务方向指示、促销标签保留原生 `red-*`
2. **Phase 7 保持 inline style**: BusinessMapView 的地图弹窗是 AMap InfoWindow HTML 字符串，无法使用 Tailwind class，改用 token 对应的 hex 值
3. **stone-* 保留**: 仅 2 处使用，作为页面背景色保留

## 待办事项

- [ ] 修复 `vue-tsc` 预存在的类型错误（与颜色迁移无关）
- [ ] 浏览器手动验证关键页面（Dashboard、合同列表、聊天、公司主页）
- [ ] 考虑为 `stone-*` 添加 token（目前仅 2 处使用）

## 关键代码片段

### tailwind.config.cjs 色阶补全示例

```js
warning: {
  50: '#fffbeb',
  100: '#fef3c7',
  200: '#fde68a',
  300: '#fcd34d',
  400: '#fbbf24',
  500: '#f59e0b',
  600: '#d97706',
  700: '#b45309',
  800: '#92400e',
  900: '#78350f',
},
```

### 头像颜色数组（conversation.ts）

```ts
const colors = [
  'bg-brand-500',
  'bg-autumn-500',
  'bg-action-500',
  'bg-action-700',
  'bg-accent-400',
  'bg-accent-600',
  'bg-brand-700',
  'bg-warning-500'
]
```
