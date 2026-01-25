/**
 * Quote Payload Types
 * Unified and versioned quote type definitions
 */

/** 报价版本 */
export type QuoteVersion = 1 | 2

/** 报价类型标识 */
export type QuoteKind = 'QUOTE_V1' | 'QUOTE_V2' | 'BASIS_QUOTE_V1'

/** 交易类型 */
export type TradeType = 'SPOT' | 'BASIS'

// ==================== V1 Types (Legacy) ====================

/** V1 报价字段 */
export interface QuoteFieldsV1 {
  price?: string
  quantity?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  remark?: string
  invoiceType?: string
  packaging?: string
  deliveryMethod?: string
  /** 动态产品参数 */
  dynamicParams?: Record<string, string>
}

/** V1 报价 payload */
export interface QuotePayloadV1 {
  version: 1
  kind: 'QUOTE_V1'
  createdAt: string
  fields: QuoteFieldsV1
}

/** V1 基差报价字段 */
export interface BasisQuoteFieldsV1 {
  contractCode: string
  contractName?: string
  basisPrice: number
  futuresPrice: number
  referencePrice: number
  quantity: string
  remark?: string
}

/** V1 基差报价 payload */
export interface BasisQuotePayloadV1 {
  version: 1
  kind: 'BASIS_QUOTE_V1'
  createdAt: string
  fields: BasisQuoteFieldsV1
}

// ==================== V2 Types (New) ====================

/** V2 统一报价字段 */
export interface QuoteFieldsV2 {
  // 核心字段
  price?: string
  quantity?: string
  unit?: string

  // 基差交易字段
  basisPrice?: number
  contractCode?: string
  contractName?: string
  futuresPrice?: number
  referencePrice?: number

  // 交付条款
  deliveryMethod?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  invoiceType?: string
  packaging?: string

  // 产品参数
  productParams?: Record<string, string>

  remark?: string
}

/** V2 报价 payload */
export interface QuotePayloadV2 {
  version: 2
  kind: 'QUOTE_V2'
  createdAt: string
  expiresAt?: string
  parentQuoteId?: number
  tradeType: TradeType
  fields: QuoteFieldsV2
  /** 相对于上一个报价变更的字段列表 */
  changedFields?: string[]
}

// ==================== Union Types ====================

/** 所有报价 payload 类型 */
export type QuotePayload = QuotePayloadV1 | BasisQuotePayloadV1 | QuotePayloadV2

/** 所有报价字段类型 */
export type QuoteFields = QuoteFieldsV1 | BasisQuoteFieldsV1 | QuoteFieldsV2

// ==================== Legacy Format Types ====================

/** 遗留格式：直接字段（无 version/kind 包装） */
export interface LegacyQuotePayload {
  price?: string
  quantity?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  remark?: string
  invoiceType?: string
  packaging?: string
  deliveryMethod?: string
  dynamicParams?: Record<string, string>
  // 基差字段
  basisPrice?: number
  contractCode?: string
  contractName?: string
  futuresPrice?: number
  referencePrice?: number
}

// ==================== Display Types ====================

/** 报价显示字段 */
export interface QuoteDisplayField {
  label: string
  value: string
  key: string
  changed?: boolean
  oldValue?: string
}

/** 报价字段标签映射 */
export const QUOTE_FIELD_LABELS: Record<string, string> = {
  price: '单价',
  quantity: '数量',
  unit: '单位',
  deliveryPlace: '交付地',
  arrivalDate: '到货期',
  paymentMethod: '结算方式',
  invoiceType: '发票类型',
  packaging: '包装方式',
  deliveryMethod: '交货方式',
  remark: '备注',
  basisPrice: '基差',
  contractCode: '合约代码',
  contractName: '期货合约',
  futuresPrice: '盘面价',
  referencePrice: '核算价'
}

/** 获取字段标签 */
export function getFieldLabel(key: string): string {
  return QUOTE_FIELD_LABELS[key] || key
}

// ==================== Quote Diff Types ====================

/** 报价字段变更 */
export interface QuoteFieldDiff {
  key: string
  label: string
  oldValue?: string
  newValue?: string
  type: 'added' | 'removed' | 'changed'
}

/** 计算数值变更 */
export function calculatePriceDiff(oldPrice?: string | number, newPrice?: string | number): {
  diff: number
  percentage: number
  direction: 'up' | 'down' | 'unchanged'
} {
  const oldNum = typeof oldPrice === 'string' ? parseFloat(oldPrice) : (oldPrice || 0)
  const newNum = typeof newPrice === 'string' ? parseFloat(newPrice) : (newPrice || 0)

  if (isNaN(oldNum) || isNaN(newNum) || oldNum === 0) {
    return { diff: 0, percentage: 0, direction: 'unchanged' }
  }

  const diff = newNum - oldNum
  const percentage = (diff / oldNum) * 100

  return {
    diff,
    percentage,
    direction: diff > 0 ? 'up' : diff < 0 ? 'down' : 'unchanged'
  }
}
