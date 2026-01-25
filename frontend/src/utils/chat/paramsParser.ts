/**
 * Product Params Parser Utility
 * Handles parsing of various paramsJson formats
 */

/** 产品参数类型 */
export type ProductParams = Record<string, string>

/**
 * 旧格式类型：{ params: { "1": { name: "xxx", value: "yyy" }, ... } }
 */
interface LegacyParamsFormat {
  params: Record<string, { name: string; value: string | number }>
}

/**
 * 新格式类型：{ "参数名": "参数值", ... }
 */
type NewParamsFormat = Record<string, string | number>

/**
 * 解析产品参数 JSON
 * 支持多种格式：
 * 1. 新格式：{ "蛋白质": "43%", "水分": "12%" }
 * 2. 旧格式：{ params: { "1": { name: "蛋白质", value: "43%" }, "2": { name: "水分", value: "12%" } } }
 * 3. 混合格式：{ params: { "蛋白质": "43%" } }
 */
export function parseProductParams(paramsJson?: string | null): ProductParams {
  if (!paramsJson) return {}

  try {
    const parsed = JSON.parse(paramsJson)
    return normalizeParams(parsed)
  } catch (e) {
    console.error('[paramsParser] Failed to parse paramsJson:', e)
    return {}
  }
}

/**
 * 规范化参数对象
 */
export function normalizeParams(obj: any): ProductParams {
  if (!obj || typeof obj !== 'object') return {}

  const result: ProductParams = {}

  // 检查是否为旧格式 { params: {...} }
  const source = obj.params || obj

  Object.entries(source).forEach(([key, value]) => {
    // 跳过纯数字键名（旧格式中的索引）
    if (/^\d+$/.test(key)) {
      // 旧格式：{ "1": { name: "xxx", value: "yyy" } }
      if (isLegacyParamEntry(value)) {
        const entry = value as { name: string; value: string | number }
        if (entry.name) {
          result[entry.name] = String(entry.value ?? '')
        }
      }
      return
    }

    // 新格式或混合格式
    if (typeof value === 'object' && value !== null && 'name' in value) {
      // 混合格式：{ "1": { name: "xxx", value: "yyy" } }
      const entry = value as { name: string; value: string | number }
      if (entry.name) {
        result[entry.name] = String(entry.value ?? '')
      }
    } else if (typeof value === 'string' || typeof value === 'number') {
      // 新格式：{ "参数名": "参数值" }
      result[key] = String(value)
    }
  })

  return result
}

/**
 * 检查是否为旧格式参数条目
 */
function isLegacyParamEntry(value: unknown): boolean {
  return (
    typeof value === 'object' &&
    value !== null &&
    'name' in value &&
    'value' in value
  )
}

/**
 * 将参数对象转换为显示列表
 */
export interface ParamDisplayItem {
  name: string
  value: string
}

export function paramsToDisplayList(params: ProductParams): ParamDisplayItem[] {
  return Object.entries(params)
    .filter(([_, v]) => v && v.trim())
    .map(([name, value]) => ({ name, value }))
}

/**
 * 合并两个参数对象
 * 新值覆盖旧值
 */
export function mergeParams(base: ProductParams, override: ProductParams): ProductParams {
  return { ...base, ...override }
}

/**
 * 比较两个参数对象的差异
 */
export interface ParamDiff {
  name: string
  oldValue?: string
  newValue?: string
  type: 'added' | 'removed' | 'changed'
}

export function compareParams(oldParams: ProductParams, newParams: ProductParams): ParamDiff[] {
  const diffs: ParamDiff[] = []
  const allKeys = new Set([...Object.keys(oldParams), ...Object.keys(newParams)])

  allKeys.forEach(name => {
    const oldVal = oldParams[name]
    const newVal = newParams[name]

    if (oldVal !== newVal) {
      if (!oldVal && newVal) {
        diffs.push({ name, newValue: newVal, type: 'added' })
      } else if (oldVal && !newVal) {
        diffs.push({ name, oldValue: oldVal, type: 'removed' })
      } else {
        diffs.push({ name, oldValue: oldVal, newValue: newVal, type: 'changed' })
      }
    }
  })

  return diffs
}

/**
 * 从标的快照中提取参数
 */
export function extractParamsFromSnapshot(snapshotJson?: string | null): ProductParams {
  if (!snapshotJson) return {}

  try {
    const snapshot = JSON.parse(snapshotJson)
    if (snapshot.paramsJson) {
      return parseProductParams(snapshot.paramsJson)
    }
    return {}
  } catch (e) {
    console.error('[paramsParser] Failed to extract params from snapshot:', e)
    return {}
  }
}

/**
 * 将参数对象序列化为新格式 JSON
 */
export function serializeParams(params: ProductParams): string {
  return JSON.stringify(params)
}

/**
 * 检查参数是否为空
 */
export function isParamsEmpty(params: ProductParams): boolean {
  return Object.keys(params).length === 0
}

/**
 * 过滤空值参数
 */
export function filterEmptyParams(params: ProductParams): ProductParams {
  const result: ProductParams = {}
  Object.entries(params).forEach(([k, v]) => {
    if (v && v.trim()) {
      result[k] = v.trim()
    }
  })
  return result
}
