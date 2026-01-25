/**
 * Quote Parser Utility
 * Handles parsing of various quote payload formats (V1, V2, Basis, Legacy)
 */

import type {
  QuotePayload,
  QuotePayloadV1,
  QuotePayloadV2,
  BasisQuotePayloadV1,
  QuoteFieldsV1,
  QuoteFieldsV2,
  BasisQuoteFieldsV1,
  LegacyQuotePayload,
  QuoteDisplayField,
  QuoteFieldDiff,
  TradeType
} from '../../types/chat/quote'
import { getFieldLabel, calculatePriceDiff } from '../../types/chat/quote'

// ==================== Parsing Functions ====================

/**
 * 解析报价 payload JSON
 * 自动检测版本并返回规范化的 payload
 */
export function parseQuotePayload(payloadJson?: string | null): QuotePayload | null {
  if (!payloadJson) return null

  try {
    const obj = JSON.parse(payloadJson)
    return normalizeQuotePayload(obj)
  } catch (e) {
    console.error('[quoteParser] Failed to parse quote payload:', e)
    return null
  }
}

/**
 * 规范化报价 payload
 * 将各种格式转换为标准格式
 */
export function normalizeQuotePayload(obj: any): QuotePayload | null {
  if (!obj) return null

  // V2 格式
  if (obj.version === 2 && obj.kind === 'QUOTE_V2') {
    return obj as QuotePayloadV2
  }

  // V1 基差报价格式
  if (obj.kind === 'BASIS_QUOTE_V1') {
    return obj as BasisQuotePayloadV1
  }

  // V1 现货报价格式
  if (obj.kind === 'QUOTE_V1') {
    return obj as QuotePayloadV1
  }

  // 有 fields 但无 kind 的半规范格式
  if (obj.fields && typeof obj.fields === 'object') {
    // 检查是否为基差报价（通过 fields 中的字段判断）
    if (obj.fields.basisPrice !== undefined || obj.fields.contractCode) {
      return {
        version: 1,
        kind: 'BASIS_QUOTE_V1',
        createdAt: obj.createdAt || new Date().toISOString(),
        fields: obj.fields as BasisQuoteFieldsV1
      }
    }
    return {
      version: 1,
      kind: 'QUOTE_V1',
      createdAt: obj.createdAt || new Date().toISOString(),
      fields: obj.fields as QuoteFieldsV1
    }
  }

  // 遗留格式：直接字段
  if (obj.price !== undefined || obj.quantity !== undefined || obj.basisPrice !== undefined) {
    const legacy = obj as LegacyQuotePayload

    // 判断是基差还是现货
    if (legacy.basisPrice !== undefined || legacy.contractCode) {
      return {
        version: 1,
        kind: 'BASIS_QUOTE_V1',
        createdAt: new Date().toISOString(),
        fields: {
          contractCode: legacy.contractCode || '',
          contractName: legacy.contractName,
          basisPrice: legacy.basisPrice || 0,
          futuresPrice: legacy.futuresPrice || 0,
          referencePrice: legacy.referencePrice || 0,
          quantity: legacy.quantity || '',
          remark: legacy.remark
        }
      }
    }

    return {
      version: 1,
      kind: 'QUOTE_V1',
      createdAt: new Date().toISOString(),
      fields: {
        price: legacy.price,
        quantity: legacy.quantity,
        deliveryPlace: legacy.deliveryPlace,
        arrivalDate: legacy.arrivalDate,
        paymentMethod: legacy.paymentMethod,
        remark: legacy.remark,
        invoiceType: legacy.invoiceType,
        packaging: legacy.packaging,
        deliveryMethod: legacy.deliveryMethod,
        dynamicParams: legacy.dynamicParams
      }
    }
  }

  return null
}

/**
 * 获取报价字段（统一接口）
 */
export function getQuoteFields(payload: QuotePayload | null): QuoteFieldsV1 | BasisQuoteFieldsV1 | null {
  if (!payload) return null
  return payload.fields
}

/**
 * 判断是否为基差报价
 */
export function isBasisQuote(payload: QuotePayload | null): boolean {
  if (!payload) return false
  return payload.kind === 'BASIS_QUOTE_V1' ||
    (payload.kind === 'QUOTE_V2' && (payload as QuotePayloadV2).tradeType === 'BASIS')
}

/**
 * 获取交易类型
 */
export function getTradeType(payload: QuotePayload | null): TradeType {
  if (!payload) return 'SPOT'
  if (payload.kind === 'BASIS_QUOTE_V1') return 'BASIS'
  if (payload.kind === 'QUOTE_V2') return (payload as QuotePayloadV2).tradeType
  return 'SPOT'
}

// ==================== Display Functions ====================

/**
 * 获取报价显示字段列表
 */
export function getQuoteDisplayFields(payloadJson?: string | null): QuoteDisplayField[] {
  const payload = parseQuotePayload(payloadJson)
  if (!payload) return []

  // 基差报价
  if (isBasisQuote(payload)) {
    const fields = payload.fields as BasisQuoteFieldsV1
    const display: QuoteDisplayField[] = []

    if (fields.contractName || fields.contractCode) {
      display.push({
        key: 'contractName',
        label: '期货合约',
        value: fields.contractName || fields.contractCode
      })
    }

    if (fields.basisPrice !== undefined) {
      const prefix = fields.basisPrice > 0 ? '+' : ''
      display.push({
        key: 'basisPrice',
        label: '基差',
        value: `${prefix}${fields.basisPrice} 元/吨`
      })
    }

    if (fields.futuresPrice) {
      display.push({
        key: 'futuresPrice',
        label: '盘面价',
        value: `¥${fields.futuresPrice}`
      })
    }

    if (fields.referencePrice) {
      display.push({
        key: 'referencePrice',
        label: '核算价',
        value: `¥${fields.referencePrice.toFixed(2)}`
      })
    }

    if (fields.quantity) {
      display.push({
        key: 'quantity',
        label: '数量',
        value: fields.quantity
      })
    }

    if (fields.remark) {
      display.push({
        key: 'remark',
        label: '备注',
        value: fields.remark
      })
    }

    return display
  }

  // 现货报价
  const fields = payload.fields as QuoteFieldsV1
  const display: QuoteDisplayField[] = []

  // 核心字段
  if (fields.price) {
    display.push({
      key: 'price',
      label: '单价',
      value: `¥${fields.price}/吨`
    })
  }

  if (fields.quantity) {
    display.push({
      key: 'quantity',
      label: '数量',
      value: fields.quantity
    })
  }

  // 交付条款
  if (fields.deliveryMethod) {
    display.push({
      key: 'deliveryMethod',
      label: '交货方式',
      value: fields.deliveryMethod
    })
  }

  if (fields.deliveryPlace) {
    display.push({
      key: 'deliveryPlace',
      label: '交付地',
      value: fields.deliveryPlace
    })
  }

  if (fields.arrivalDate) {
    display.push({
      key: 'arrivalDate',
      label: '到货期',
      value: fields.arrivalDate
    })
  }

  // 商务条款
  if (fields.paymentMethod) {
    display.push({
      key: 'paymentMethod',
      label: '结算方式',
      value: fields.paymentMethod
    })
  }

  if (fields.invoiceType) {
    display.push({
      key: 'invoiceType',
      label: '发票类型',
      value: fields.invoiceType
    })
  }

  if (fields.packaging) {
    display.push({
      key: 'packaging',
      label: '包装方式',
      value: fields.packaging
    })
  }

  // 动态参数
  if (fields.dynamicParams) {
    Object.entries(fields.dynamicParams).forEach(([k, v]) => {
      if (v) {
        display.push({
          key: `param_${k}`,
          label: k,
          value: v
        })
      }
    })
  }

  // 备注
  if (fields.remark) {
    display.push({
      key: 'remark',
      label: '备注',
      value: fields.remark
    })
  }

  return display
}

/**
 * 生成报价摘要
 */
export function buildQuoteSummary(payload: QuotePayload | null): string {
  if (!payload) return '[报价]'

  if (isBasisQuote(payload)) {
    const fields = payload.fields as BasisQuoteFieldsV1
    const prefix = (fields.basisPrice || 0) > 0 ? '+' : ''
    const contractDisplay = fields.contractName || fields.contractCode || ''
    return `${contractDisplay} | 基差 ${prefix}${fields.basisPrice} | 核算价 ¥${(fields.referencePrice || 0).toFixed(2)}`
  }

  const fields = payload.fields as QuoteFieldsV1
  const parts: string[] = []
  if (fields.price) parts.push(`¥${fields.price}`)
  if (fields.quantity) parts.push(fields.quantity)
  return parts.length ? parts.join(' · ') : '报价卡'
}

// ==================== Diff Functions ====================

/**
 * 计算两个报价之间的差异
 */
export function calculateQuoteDiff(
  currentPayload: QuotePayload | null,
  previousPayload: QuotePayload | null
): QuoteFieldDiff[] {
  if (!currentPayload) return []
  if (!previousPayload) {
    // 新报价，所有字段都是"新增"
    return getQuoteDisplayFields(JSON.stringify(currentPayload)).map(f => ({
      key: f.key,
      label: f.label,
      newValue: f.value,
      type: 'added' as const
    }))
  }

  const currentFields = currentPayload.fields as Record<string, any>
  const previousFields = previousPayload.fields as Record<string, any>
  const diffs: QuoteFieldDiff[] = []

  // 比较基础字段
  const allKeys = new Set([
    ...Object.keys(currentFields),
    ...Object.keys(previousFields)
  ])

  // 排除动态参数，单独处理
  allKeys.delete('dynamicParams')
  allKeys.delete('productParams')

  allKeys.forEach(key => {
    const currentVal = currentFields[key]
    const previousVal = previousFields[key]
    const currentStr = formatFieldValue(key, currentVal)
    const previousStr = formatFieldValue(key, previousVal)

    if (currentStr !== previousStr) {
      if (!previousStr && currentStr) {
        diffs.push({
          key,
          label: getFieldLabel(key),
          newValue: currentStr,
          type: 'added'
        })
      } else if (previousStr && !currentStr) {
        diffs.push({
          key,
          label: getFieldLabel(key),
          oldValue: previousStr,
          type: 'removed'
        })
      } else {
        diffs.push({
          key,
          label: getFieldLabel(key),
          oldValue: previousStr,
          newValue: currentStr,
          type: 'changed'
        })
      }
    }
  })

  // 比较动态参数
  const currentDynamic = currentFields.dynamicParams || currentFields.productParams || {}
  const previousDynamic = previousFields.dynamicParams || previousFields.productParams || {}
  const allDynamicKeys = new Set([
    ...Object.keys(currentDynamic),
    ...Object.keys(previousDynamic)
  ])

  allDynamicKeys.forEach(key => {
    const currentVal = currentDynamic[key] || ''
    const previousVal = previousDynamic[key] || ''

    if (currentVal !== previousVal) {
      if (!previousVal && currentVal) {
        diffs.push({
          key: `param_${key}`,
          label: key,
          newValue: currentVal,
          type: 'added'
        })
      } else if (previousVal && !currentVal) {
        diffs.push({
          key: `param_${key}`,
          label: key,
          oldValue: previousVal,
          type: 'removed'
        })
      } else {
        diffs.push({
          key: `param_${key}`,
          label: key,
          oldValue: previousVal,
          newValue: currentVal,
          type: 'changed'
        })
      }
    }
  })

  return diffs
}

/**
 * 格式化字段值为字符串
 */
function formatFieldValue(key: string, value: any): string {
  if (value === undefined || value === null || value === '') return ''

  if (key === 'price') {
    return `¥${value}/吨`
  }
  if (key === 'basisPrice') {
    const num = typeof value === 'number' ? value : parseFloat(value)
    const prefix = num > 0 ? '+' : ''
    return `${prefix}${num} 元/吨`
  }
  if (key === 'futuresPrice' || key === 'referencePrice') {
    return `¥${value}`
  }

  return String(value)
}

/**
 * 获取价格变更描述
 */
export function getPriceChangeSummary(
  currentPayload: QuotePayload | null,
  previousPayload: QuotePayload | null
): string | null {
  if (!currentPayload || !previousPayload) return null

  const isBasis = isBasisQuote(currentPayload)

  if (isBasis) {
    const currentFields = currentPayload.fields as BasisQuoteFieldsV1
    const previousFields = previousPayload.fields as BasisQuoteFieldsV1
    const diff = (currentFields.basisPrice || 0) - (previousFields.basisPrice || 0)
    if (diff === 0) return null
    const prefix = diff > 0 ? '+' : ''
    return `基差 ${previousFields.basisPrice}→${currentFields.basisPrice} (${prefix}${diff})`
  }

  const currentFields = currentPayload.fields as QuoteFieldsV1
  const previousFields = previousPayload.fields as QuoteFieldsV1

  if (!currentFields.price || !previousFields.price) return null

  const { diff, percentage, direction } = calculatePriceDiff(previousFields.price, currentFields.price)
  if (direction === 'unchanged') return null

  const prefix = diff > 0 ? '+' : ''
  return `单价 ¥${previousFields.price}→¥${currentFields.price} (${prefix}${diff.toFixed(0)})`
}

// ==================== Validation Functions ====================

/**
 * 验证报价是否可发送
 */
export function canSendQuote(payload: QuotePayload | null): boolean {
  if (!payload) return false

  if (isBasisQuote(payload)) {
    const fields = payload.fields as BasisQuoteFieldsV1
    return !!(fields.contractCode && fields.basisPrice !== undefined && fields.quantity)
  }

  const fields = payload.fields as QuoteFieldsV1
  return !!(fields.price || fields.quantity)
}

/**
 * 检查报价是否已过期
 */
export function isQuoteExpired(payload: QuotePayload | null): boolean {
  if (!payload) return false

  // V2 支持 expiresAt
  if (payload.kind === 'QUOTE_V2') {
    const v2 = payload as QuotePayloadV2
    if (v2.expiresAt) {
      return new Date(v2.expiresAt) < new Date()
    }
  }

  return false
}

/**
 * 获取报价剩余有效时间（毫秒）
 */
export function getQuoteRemainingTime(payload: QuotePayload | null): number | null {
  if (!payload) return null

  if (payload.kind === 'QUOTE_V2') {
    const v2 = payload as QuotePayloadV2
    if (v2.expiresAt) {
      const remaining = new Date(v2.expiresAt).getTime() - Date.now()
      return remaining > 0 ? remaining : 0
    }
  }

  return null
}
