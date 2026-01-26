# 聊天记录：统一合同文档样式与法律约束条款

- **日期**：2026-01-26
- **序号**：003
- **主要任务**：统一聊天议价和合同管理中的合同显示样式，添加法律约束条款

## 对话摘要

用户希望统一聊天议价和合同管理中的合同显示样式，以聊天议价中的专业纸张样式为基础，并添加法律约束条款。双方确认后生成的正式合同应包含完整的法律约束条款。

## 完成的工作

1. 分析了聊天议价和合同管理中的合同显示组件差异
2. 设计了法律约束条款结构（违约责任、不可抗力、争议解决、合同生效）
3. 创建了法律条款常量文件
4. 创建了统一合同文档展示组件
5. 修改合同详情页使用新组件
6. 修改聊天议价合同预览组件复用新组件
7. 通过 TypeScript 类型检查验证

## 修改的文件

- `frontend/src/constants/contractTerms.ts` - 新建，法律条款常量定义
- `frontend/src/components/contract/UnifiedContractDocument.vue` - 新建，统一合同文档组件
- `frontend/src/views/ContractDetailView.vue` - 修改，使用新组件（589行→333行）
- `frontend/src/components/negotiation/ContractPreview.vue` - 修改，复用新组件（475行→231行）

## 技术决策

### 1. 统一样式方案
- 以聊天议价的专业纸张样式为基础
- 创建可复用的 `UnifiedContractDocument` 组件
- 通过 props 控制显示内容（法律条款、确认区域、签署区域等）

### 2. 法律约束条款结构
采用固定模板方案，包含：
- 五、违约责任（买卖双方违约金比例 0.5%/日，拒绝履行 10%）
- 六、不可抗力（3日通知、15日证明、30日解除）
- 七、争议解决（适用中国法律、合同签订地法院管辖）
- 八、合同生效（电子签章效力、双方各执一份）

### 3. 正式合同生成流程
```
草稿合同（DRAFT水印）
    ↓ 双方点击确认条款
条款已确认（显示法律约束条款）
    ↓ 生成正式合同
签署中 → 已完成（电子签章效果）
```

### 4. 法律条款显示时机
- 合同管理：待签署及之后显示（status >= 1）
- 聊天议价：条款确认后显示（CONFIRMED/SIGNING/COMPLETED）

## 待办事项

- [ ] 后端生成 PDF 时包含法律约束条款
- [ ] 添加条款版本管理功能
- [ ] 考虑未来支持可配置的法律条款模板

## 关键代码片段

### 法律条款常量定义
```typescript
// frontend/src/constants/contractTerms.ts
export const BREACH_OF_CONTRACT_TERMS = `
5.1 卖方违约责任：
    (1) 卖方逾期交货的，每逾期一日，应向买方支付合同总金额 0.5% 的违约金；
    ...
`.trim()

export const CONTRACT_TERM_SECTIONS: ContractTermSection[] = [
  { number: '五', titleCn: '违约责任', titleEn: 'Breach of Contract', content: BREACH_OF_CONTRACT_TERMS, isLegalBinding: true },
  { number: '六', titleCn: '不可抗力', titleEn: 'Force Majeure', content: FORCE_MAJEURE_TERMS, isLegalBinding: true },
  { number: '七', titleCn: '争议解决', titleEn: 'Dispute Resolution', content: DISPUTE_RESOLUTION_TERMS, isLegalBinding: true },
  { number: '八', titleCn: '合同生效', titleEn: 'Contract Effectiveness', content: CONTRACT_EFFECTIVE_TERMS, isLegalBinding: true }
]
```

### 统一组件使用示例
```vue
<UnifiedContractDocument
  :data="documentData"
  :status="documentStatus"
  :buyer-signed="contract.buyerSigned"
  :seller-signed="contract.sellerSigned"
  :show-legal-terms="showLegalTerms"
  :show-confirm-area="false"
  :show-sign-area="true"
/>
```
