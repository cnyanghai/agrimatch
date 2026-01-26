/**
 * Negotiation Components
 * 议价与合同相关组件
 */

export { default as ProductRequirementForm } from './ProductRequirementForm.vue'
export { default as ChatPanel } from './ChatPanel.vue'
export { default as ContractPreview } from './ContractPreview.vue'
export { default as ConversationSidebar } from './ConversationSidebar.vue'
export { default as QuoteInputPanel } from './QuoteInputPanel.vue'

// 类型导出
export type { RequirementData } from './ProductRequirementForm.vue'
export type { ContractData, ContractParty, ContractProduct, QualityStandard, ContractStatus } from './ContractPreview.vue'
export type { QuotePayload } from './QuoteInputPanel.vue'
