# 聊天记录：业态单位系统与多业态适配

- **日期**：2026-01-26
- **序号**：001
- **主要任务**：完成产品发布系统的业态适配，实现动态单位显示

## 对话摘要

本次对话延续之前的产品业态(Schema)系统改造工作，完成了三个主要迭代：
1. 需求发布表单同步改造
2. 供求大厅左侧边栏改造
3. 聊天协商卡片适配

核心目标是让不同业态（饲料原料/禽蛋种苗/畜禽肉类/其他）使用各自适合的计量单位和价格单位。

## 完成的工作

### 1. 需求发布表单同步改造 (MyPurchaseManageView.vue)
- 集成 `SchemaAwareCategoryPicker` 组件用于业态与品类选择
- 集成 `CategoryParamsForm` 组件用于结构化参数输入
- 实现动态单位标签（吨/枚/只等）
- 实现动态包装和交付选项（根据业态变化）
- 模板保存/应用支持 schemaCode

### 2. 供求大厅左侧边栏改造
- **SupplyHallView.vue**: 移除硬编码的 domains 和 categoryOptions，改用 API 加载业态树
- **PurchaseHallView.vue**: 同样的业态筛选升级
- 两个视图现在根据选中的业态显示对应的快捷品类筛选
- 供应/需求卡片显示动态单位

### 3. 聊天协商卡片适配
- 更新 `QuoteCard.vue` 接受 `schemaCode` 和 `categoryName` 属性
- 报价卡片现在显示动态价格单位（元/吨、元/枚等）
- 建立属性传递链：`ChatLayout` → `ChatMain` → `MessageBubble` → `QuoteCard`
- 使用 `getSchemaUnitConfig` 和 `getCategoryUnitConfig` 解析单位

## 修改的文件

### 前端视图
- `frontend/src/views/MyPurchaseManageView.vue` - 需求发布表单业态适配
- `frontend/src/views/SupplyHallView.vue` - 供应大厅业态筛选
- `frontend/src/views/PurchaseHallView.vue` - 需求大厅业态筛选

### 聊天组件
- `frontend/src/components/chat/message/QuoteCard.vue` - 报价卡片动态单位
- `frontend/src/components/chat/message/MessageBubble.vue` - 传递 schemaCode 属性
- `frontend/src/components/chat/layout/ChatMain.vue` - 传递 schemaCode 属性
- `frontend/src/components/chat/layout/ChatLayout.vue` - 传递 schemaCode 属性

### API 接口
- `frontend/src/api/supply.ts` - 添加 schemaCode、tagsJson 字段
- `frontend/src/api/requirement.ts` - 添加 tagsJson 字段

### 工具函数
- `frontend/src/utils/schemaUnits.ts` - 修复类型错误

## 技术决策

### 1. 单位配置架构
```typescript
// 业态级别配置
getSchemaUnitConfig(schemaCode: string): SchemaUnitConfig

// 品类级别配置（更精确）
getCategoryUnitConfig(schemaCode: string, categoryName: string): UnitConfig
```

### 2. 业态与品类关系
- feed (饲料原料): 吨、元/吨
- poultry (禽蛋种苗): 枚/只、元/枚 或 元/只
- meat (畜禽肉类): 吨、元/吨
- other (其他品类): 单位、元

### 3. 品类推断策略
当 schemaCode 不可用时，通过 `findSchemaCodeByCategory(categoryName)` 从业态树中反向查找。

## 待办事项

- [ ] 后端 Supply/Requirement 实体添加 schemaCode 字段
- [ ] 后端 API 返回 schemaCode 以减少前端推断
- [ ] 聊天视图中传递 schemaCode 到 ChatLayout（需要从 subjectSnapshot 解析）
- [ ] 历史数据迁移（为现有供应/需求添加 schemaCode）

## 关键代码片段

### 动态单位配置使用示例
```vue
<script setup>
import { getSchemaUnitConfig, getCategoryUnitConfig } from '../utils/schemaUnits'

const currentUnitConfig = computed(() => {
  return getCategoryUnitConfig(selectedSchemaCode.value, publishForm.categoryName || '')
})
</script>

<template>
  <label>{{ currentUnitConfig.quantityLabel }}</label>
  <div>{{ quantity }} {{ currentUnitConfig.quantityUnit }}</div>
  <div>¥{{ price }}/{{ currentUnitConfig.priceUnit }}</div>
</template>
```

### 品类反向查找
```typescript
function findSchemaCodeByCategory(categoryName: string): string {
  for (const schema of schemaTree.value) {
    const categories = flattenCategories(schema.categories)
    if (categories.includes(categoryName)) {
      return schema.schemaCode
    }
  }
  return 'feed' // 默认饲料原料
}
```
