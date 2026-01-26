# 聊天记录：合同管理模块页面迭代

- **日期**：2026-01-27
- **序号**：001
- **主要任务**：基于模板设计重新设计合同管理模块页面

## 对话摘要

本次对话延续了之前的工作，完成了合同管理模块页面的重新设计。参考 `stitch_/合同管理中心/` 目录下的模板设计，将原有的卡片式布局改为更专业的表格式布局，并增加了右侧统计边栏。

## 完成的工作

1. **分析模板设计**
   - 阅读 `stitch_/合同管理中心/code.html` 模板文件
   - 提取关键设计元素：Tab导航、筛选栏、表格布局、分页、右侧边栏

2. **重新设计合同列表页面**
   - 采用左右分栏布局
   - 左侧：Tab标签 + 筛选栏 + 数据表格 + 分页
   - 右侧：合同统计 + 待办提醒 + 快捷工具

3. **功能增强**
   - Tab 标签导航（所有合同、待我签署、履行中、已归档）
   - 状态下拉筛选和搜索框
   - 表格展示合同数据（7列）
   - 完整的分页组件
   - 统计数据展示（待结算总额、本月新增、完成率）
   - 待办提醒列表

## 修改的文件

- `frontend/src/views/ContractListView.vue` - 完全重写，采用新的设计风格

## 技术决策

1. **布局选择**：采用 Flex 左右分栏，左侧主内容区 flex-1，右侧边栏固定宽度 w-80

2. **数据展示**：使用原生 HTML table 替代卡片，提高信息密度

3. **分页实现**：前端分页，支持省略号显示和页码导航

4. **颜色系统**：使用项目品牌色（brand-*、autumn-*）替代模板的蓝色

5. **统计计算**：
   - 待结算总额：待签署 + 履行中合同的金额总和
   - 本月新增：当月创建的合同数量
   - 完成率：已完成 / 总数

## 待办事项

- [ ] 快捷工具按钮功能实现（批量导入、合同模板）
- [ ] 响应式布局优化（移动端隐藏右侧边栏）
- [ ] 后端分页 API 对接（当前为前端分页）

## 关键代码片段

### Tab 导航配置
```typescript
const tabs = [
  { key: 'all', label: '所有合同', statusFilter: null },
  { key: 'pending', label: '待我签署', statusFilter: 1 },
  { key: 'executing', label: '履行中', statusFilter: 3 },
  { key: 'archived', label: '已归档', statusFilter: 4 }
]
```

### 分页逻辑
```typescript
const pageNumbers = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i)
    }
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }
  return pages
})
```

### 统计数据计算
```typescript
const stats = computed(() => {
  const all = contracts.value
  const pendingTotal = all
    .filter(c => c.status === 1 || c.status === 3)
    .reduce((sum, c) => sum + (c.totalAmount || 0), 0)
  // ...
})
```
