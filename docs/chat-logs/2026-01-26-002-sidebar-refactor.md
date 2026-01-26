# 聊天记录：供求大厅侧边栏改造

- **日期**：2026-01-26
- **序号**：002
- **主要任务**：将供求大厅的业态和品类筛选改为左侧边栏布局

## 对话摘要

用户提出将供应大厅和需求大厅搜索框下方的业态筛选和品种筛选合并到左侧边栏，同时将搜索按钮文字统一为"搜索"。

## 完成的工作

### 1. 新增 CategorySidebar 组件
- 创建可复用的品类侧边栏组件
- 支持业态选择（四宫格图标按钮）
- 支持品类树形结构展示（三级嵌套）
- 支持主题色切换（brand 供应 / autumn 需求）
- 响应式设计，桌面端固定侧边栏，移动端抽屉式

### 2. SupplyHallView.vue 改造
- 集成 CategorySidebar 组件
- 简化搜索区域布局
- 添加移动端侧边栏切换按钮
- 搜索按钮文字改为"搜索"
- 显示当前筛选状态标签
- 添加侧边栏过渡动画

### 3. PurchaseHallView.vue 改造
- 同样集成 CategorySidebar 组件
- 使用 autumn 主题色
- 搜索按钮文字改为"搜索"
- 添加侧边栏过渡动画

## 修改的文件

### 新增
- `frontend/src/components/CategorySidebar.vue` - 品类侧边栏组件

### 修改
- `frontend/src/views/SupplyHallView.vue` - 供应大厅视图
- `frontend/src/views/PurchaseHallView.vue` - 需求大厅视图

## 技术决策

### 1. 布局结构
```
+------------------------------------------+
|           搜索区（精简版）                  |
+----------+-------------------------------+
| 侧边栏   |           列表区               |
| (256px)  |         (flex-1)              |
|          |                               |
+----------+-------------------------------+
```

### 2. 侧边栏组件 API
```vue
<CategorySidebar
  :schema-tree="schemaTree"
  v-model:selected-schema-code="selectedSchemaCode"
  v-model:selected-category="selectedCategory"
  theme="brand | autumn"
  @schema-change="onSchemaChange"
  @category-change="onCategoryChange"
/>
```

### 3. 移动端适配
- 桌面端（lg 以上）：固定左侧边栏，宽度 256px
- 移动端：点击菜单按钮展开抽屉式侧边栏
- 使用 Vue Teleport 将抽屉渲染到 body
- 选择品类后自动关闭抽屉

### 4. 过渡动画
- 遮罩层：淡入淡出 0.2s
- 侧边栏：从左滑入 0.3s

## 关键代码片段

### CategorySidebar 组件结构
```vue
<template>
  <div class="h-full flex flex-col bg-white">
    <!-- 业态选择区（四宫格） -->
    <div class="p-4 border-b">
      <div class="grid grid-cols-2 gap-2">
        <button v-for="schema in schemaTree" ...>
          <component :is="schemaIcons[schema.schemaCode]" />
          <span>{{ schema.schemaName }}</span>
        </button>
      </div>
    </div>

    <!-- 品类树区域 -->
    <div class="flex-1 overflow-y-auto">
      <button @click="selectCategory(null)">全部品类</button>
      <template v-for="node in currentSchema.categories">
        <!-- 一级分类 -->
        <button @click="toggleNode(node.id) || selectCategory(node.name)">
          <ChevronDown v-if="isExpanded(node.id)" />
          {{ node.name }}
        </button>
        <!-- 二级/三级分类嵌套 -->
      </template>
    </div>
  </div>
</template>
```

### 移动端抽屉实现
```vue
<Teleport to="body">
  <Transition name="fade">
    <div v-if="mobileSidebarOpen" class="fixed inset-0 bg-black/50" />
  </Transition>
  <Transition name="slide-left">
    <div v-if="mobileSidebarOpen" class="fixed left-0 top-0 bottom-0 w-72 bg-white">
      <CategorySidebar ... />
    </div>
  </Transition>
</Teleport>
```

## 界面效果

### 改造前
- 搜索框 + 按钮（一行）
- 业态筛选标签（一行）
- 品类筛选标签（一行）
- 列表区（全宽）

### 改造后
- 搜索框 + 菜单按钮（移动端）+ 搜索按钮（一行）
- 当前筛选状态（可选显示）
- 左侧边栏（业态图标 + 品类树）| 右侧列表区

## 待办事项

- [x] 创建 CategorySidebar 组件
- [x] 改造 SupplyHallView
- [x] 改造 PurchaseHallView
- [x] 搜索按钮统一为"搜索"
- [x] 移动端抽屉式侧边栏
- [ ] 侧边栏记住折叠状态（可选优化）
- [ ] 侧边栏品类数量统计（需后端支持）
