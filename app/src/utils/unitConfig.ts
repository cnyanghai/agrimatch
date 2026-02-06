/**
 * 业态动态单位配置（移植自 Web 端 schemaUnits.ts 的简化版）
 *
 * 不同业态使用不同的计量单位和价格单位。
 * - feed:      原料饲料 -> 吨 / 元/吨
 * - breed:     生物种苗 -> 只 / 元/只
 * - process:   农牧加工 -> 吨 / 元/吨
 * - equipment: 装备物流 -> 台/套 / 元
 * 向后兼容旧代码：poultry -> breed, meat -> process, other -> equipment
 */

export interface UnitConfig {
  quantityUnit: string
  priceUnit: string
}

/** 基础业态单位映射 */
const schemaUnitMap: Record<string, UnitConfig> = {
  feed: { quantityUnit: '吨', priceUnit: '元/吨' },
  breed: { quantityUnit: '只', priceUnit: '元/只' },
  process: { quantityUnit: '吨', priceUnit: '元/吨' },
  equipment: { quantityUnit: '台/套', priceUnit: '元' },
  // 向后兼容旧业态代码
  poultry: { quantityUnit: '只', priceUnit: '元/只' },
  meat: { quantityUnit: '吨', priceUnit: '元/吨' },
  other: { quantityUnit: '台/套', priceUnit: '元' },
}

/** 生物种苗细分品类单位（品类名 -> 单位） */
const breedSubUnits: Record<string, UnitConfig> = {
  '种蛋': { quantityUnit: '枚', priceUnit: '元/枚' },
  '鸡苗': { quantityUnit: '只', priceUnit: '元/只' },
  '鸭苗': { quantityUnit: '只', priceUnit: '元/只' },
  '鹅苗': { quantityUnit: '只', priceUnit: '元/只' },
  '商品蛋': { quantityUnit: '斤', priceUnit: '元/斤' },
  '种猪': { quantityUnit: '头', priceUnit: '元/头' },
  '种牛': { quantityUnit: '头', priceUnit: '元/头' },
  '种羊': { quantityUnit: '只', priceUnit: '元/只' },
  '鱼苗': { quantityUnit: '万尾', priceUnit: '元/万尾' },
  '虾苗': { quantityUnit: '万尾', priceUnit: '元/万尾' },
  '蟹苗': { quantityUnit: '斤', priceUnit: '元/斤' },
}

/** 农牧加工细分品类单位 */
const processSubUnits: Record<string, UnitConfig> = {
  '冷冻鱼类': { quantityUnit: '公斤', priceUnit: '元/公斤' },
  '冷冻虾类': { quantityUnit: '公斤', priceUnit: '元/公斤' },
  '水产制品': { quantityUnit: '公斤', priceUnit: '元/公斤' },
}

/** 装备物流细分品类单位 */
const equipmentSubUnits: Record<string, UnitConfig> = {
  '冷链物流': { quantityUnit: '次', priceUnit: '元/次' },
  '活禽运输': { quantityUnit: '次', priceUnit: '元/次' },
  '大宗散料运输': { quantityUnit: '吨', priceUnit: '元/吨' },
  '蛋托/蛋箱': { quantityUnit: '个', priceUnit: '元/个' },
  '编织袋/吨包': { quantityUnit: '个', priceUnit: '元/个' },
}

/** 默认单位配置（feed 饲料原料） */
const DEFAULT_UNIT: UnitConfig = { quantityUnit: '吨', priceUnit: '元/吨' }

/**
 * 获取单位标签
 *
 * @param schemaCode  - 业态代码，如 'feed'、'breed' 等
 * @param field       - 'quantity' 返回数量单位，'price' 返回价格单位
 * @param categoryName - 可选，品类名称用于细分单位
 * @returns 对应的单位文字
 */
export function getUnitLabel(
  schemaCode?: string | null,
  field: 'quantity' | 'price' = 'quantity',
  categoryName?: string | null,
): string {
  // 1. 如果有品类名称，尝试细分单位
  if (categoryName && schemaCode) {
    let subConfig: UnitConfig | undefined
    if (schemaCode === 'breed' || schemaCode === 'poultry') {
      subConfig = breedSubUnits[categoryName]
    } else if (schemaCode === 'process' || schemaCode === 'meat') {
      subConfig = processSubUnits[categoryName]
    } else if (schemaCode === 'equipment' || schemaCode === 'other') {
      subConfig = equipmentSubUnits[categoryName]
    }
    if (subConfig) {
      return field === 'quantity' ? subConfig.quantityUnit : subConfig.priceUnit
    }
  }

  // 2. 根据 schemaCode 获取基础单位
  const config = schemaCode ? schemaUnitMap[schemaCode] : undefined
  const unit = config || DEFAULT_UNIT

  return field === 'quantity' ? unit.quantityUnit : unit.priceUnit
}

/**
 * 获取完整单位配置对象
 */
export function getUnitConfig(
  schemaCode?: string | null,
  categoryName?: string | null,
): UnitConfig {
  return {
    quantityUnit: getUnitLabel(schemaCode, 'quantity', categoryName),
    priceUnit: getUnitLabel(schemaCode, 'price', categoryName),
  }
}
