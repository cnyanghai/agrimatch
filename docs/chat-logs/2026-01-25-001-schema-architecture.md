# 聊天记录：产品业态架构设计

- **日期**：2026-01-25 (估计)
- **序号**：001
- **主要任务**：设计并实现产品业态(Schema)架构

## 对话摘要

本次对话是产品业态系统的初始设计和实现阶段，建立了四大业态分类体系，并创建了相关的数据库表、后端服务和前端组件。

## 完成的工作

### 1. 数据库设计
- 创建 `nht_product_schema` 表存储业态信息
- 设计业态与品类的关联关系
- 编写数据迁移SQL

### 2. 后端 ProductSchema 模块
- 创建 domain: `NhtProductSchema`
- 创建 DTO: `ProductSchemaVO`, `CategoryNode`
- 创建 mapper: `ProductSchemaMapper`
- 创建 service: `ProductSchemaService`, `ProductSchemaServiceImpl`
- 创建 controller: `ProductSchemaController`

### 3. 前端 API 与工具
- 创建 `api/productSchema.ts` - 业态API接口
- 创建 `utils/schemaUnits.ts` - 业态单位配置工具

### 4. 前端组件
- 创建 `SchemaAwareCategoryPicker.vue` - 业态感知的品类选择器
- 创建 `CategoryParamsForm.vue` - 结构化参数表单

### 5. 供应发布表单改造
- 更新 `MySupplyManageView.vue` 使用新的业态系统

## 修改的文件

### 数据库
- `backend/agrimatch-service/src/main/resources/db/schema_migration.sql` - 业态表结构

### 后端
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/domain/NhtProductSchema.java`
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/dto/ProductSchemaVO.java`
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/mapper/ProductSchemaMapper.java`
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/service/ProductSchemaService.java`
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/service/impl/ProductSchemaServiceImpl.java`
- `backend/agrimatch-service/src/main/java/com/agrimatch/product_schema/controller/ProductSchemaController.java`

### 前端
- `frontend/src/api/productSchema.ts`
- `frontend/src/utils/schemaUnits.ts`
- `frontend/src/components/SchemaAwareCategoryPicker.vue`
- `frontend/src/components/CategoryParamsForm.vue`
- `frontend/src/views/MySupplyManageView.vue`

## 技术决策

### 1. 四大业态分类
| schemaCode | schemaName | 描述 |
|------------|------------|------|
| feed | 饲料原料 | 玉米、豆粕等大宗原料 |
| poultry | 禽蛋种苗 | 种蛋、鸡苗、鸭苗等 |
| meat | 畜禽肉类 | 鸡肉、猪肉等 |
| other | 其他品类 | 其他农产品 |

### 2. API 设计
- `GET /api/product-schemas` - 获取所有业态列表
- `GET /api/product-schemas/tree` - 获取业态及其品类树
- `GET /api/product-schemas/{schemaCode}` - 获取单个业态详情

### 3. 单位配置结构
```typescript
interface SchemaUnitConfig {
  schemaCode: string
  schemaName: string
  quantityUnit: string      // 数量单位
  quantityLabel: string     // 数量标签
  priceUnit: string         // 价格单位
  priceLabel: string        // 价格标签
  packagingOptions: string[] // 包装选项
  deliveryOptions: string[]  // 交付选项
}
```

## 待办事项

- [x] 需求发布表单同步改造
- [x] 供求大厅左侧边栏改造
- [x] 聊天协商卡片适配
- [ ] 后端实体添加 schemaCode 字段
- [ ] 历史数据迁移

## 遇到的问题与解决

### 1. Mapper 冲突错误
**问题**: `Mapped Statements collection already contains key selectAllWithSchema`
**原因**: 方法同时在接口注解和XML mapper中定义
**解决**: 从XML中移除重复定义

### 2. PathVariable 错误
**问题**: `Name for argument of type [java.lang.String] not specified`
**解决**: 显式指定参数名 `@PathVariable("schemaCode") String schemaCode`
