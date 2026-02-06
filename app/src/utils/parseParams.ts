/**
 * paramsJson 通用解析工具
 *
 * 后端 paramsJson 是一个 JSON 字符串，实际格式可能为：
 * 1. 扁平对象：{"水分": "14%", "蛋白": "43%"}
 * 2. 包装数组：{params: [{name: "水分", value: "14", unit: "%"}, ...]}
 * 3. 纯数组：  [{name: "水分", value: "14", unit: "%"}, ...]
 *
 * 本工具统一输出为 ParsedParam[] 格式，供所有页面引用。
 */

export interface ParsedParam {
  /** 参数名称 */
  key: string
  /** 参数值 */
  value: string
  /** 单位（可选） */
  unit?: string
}

/**
 * 解析 paramsJson 字符串为标准化参数数组
 *
 * @param paramsJson - 原始 JSON 字符串
 * @returns ParsedParam[] 标准化参数数组，解析失败返回空数组
 */
export function parseParams(paramsJson?: string | null): ParsedParam[] {
  if (!paramsJson) return []

  try {
    const parsed = JSON.parse(paramsJson)
    if (parsed === null || parsed === undefined) return []

    // 格式 2: { params: [{name, value, unit}] }
    if (typeof parsed === 'object' && !Array.isArray(parsed) && Array.isArray(parsed.params)) {
      return normalizeArray(parsed.params)
    }

    // 格式 3: [{name, value, unit}]
    if (Array.isArray(parsed)) {
      return normalizeArray(parsed)
    }

    // 格式 1: {key: value} 扁平对象
    if (typeof parsed === 'object' && !Array.isArray(parsed)) {
      return normalizeFlatObject(parsed)
    }

    return []
  } catch {
    return []
  }
}

/**
 * 解析 paramsJson 并生成标签字符串数组，格式为 "参数名:值"
 *
 * @param paramsJson - 原始 JSON 字符串
 * @param maxCount - 最多返回几个标签，默认 5
 * @returns string[] 标签数组
 */
export function parseParamTags(paramsJson?: string | null, maxCount = 5): string[] {
  const params = parseParams(paramsJson)
  return params.slice(0, maxCount).map(p => {
    if (p.unit) {
      return `${p.key}:${p.value}${p.unit}`
    }
    return `${p.key}:${p.value}`
  })
}

/**
 * 将数组格式的参数标准化为 ParsedParam[]
 * 支持 {name, value, unit} 和 {paramName, value, unit} 两种键名
 */
function normalizeArray(arr: any[]): ParsedParam[] {
  if (!Array.isArray(arr)) return []
  const result: ParsedParam[] = []
  for (const item of arr) {
    if (!item || typeof item !== 'object') continue
    const key = item.name || item.paramName || item.key || ''
    const value = item.value ?? ''
    if (!key || value === '' || value === undefined || value === null) continue
    result.push({
      key: String(key),
      value: String(value),
      unit: item.unit ? String(item.unit) : undefined,
    })
  }
  return result
}

/**
 * 将扁平对象格式的参数标准化为 ParsedParam[]
 * 如 {"水分": "14%", "蛋白": "43%"}
 */
function normalizeFlatObject(obj: Record<string, any>): ParsedParam[] {
  const result: ParsedParam[] = []
  for (const [key, val] of Object.entries(obj)) {
    if (val === undefined || val === null || val === '') continue
    result.push({
      key,
      value: String(val),
    })
  }
  return result
}
