/**
 * 业态单位配置
 * 不同业态使用不同的计量单位和价格单位
 */

export interface SchemaUnitConfig {
  schemaCode: string
  schemaName: string
  // 数量单位
  quantityUnit: string
  quantityLabel: string
  quantityPlaceholder: string
  quantityStep: number
  // 价格单位
  priceUnit: string
  priceLabel: string
  pricePlaceholder: string
  priceStep: number
  // 包装方式选项
  packagingOptions: string[]
  // 交付方式选项
  deliveryOptions: string[]
}

/**
 * 业态单位配置表
 */
export const schemaUnits: Record<string, SchemaUnitConfig> = {
  feed: {
    schemaCode: 'feed',
    schemaName: '饲料原料',
    quantityUnit: '吨',
    quantityLabel: '可供数量（吨）',
    quantityPlaceholder: '请输入数量',
    quantityStep: 1,
    priceUnit: '元/吨',
    priceLabel: '出厂价（元/吨）',
    pricePlaceholder: '请输入价格',
    priceStep: 10,
    packagingOptions: ['散装', '袋装', '吨包'],
    deliveryOptions: ['到厂', '自提', '物流配送']
  },
  poultry: {
    schemaCode: 'poultry',
    schemaName: '禽蛋种苗',
    quantityUnit: '枚/只',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 100,
    priceUnit: '元',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 0.1,
    packagingOptions: ['蛋托', '纸箱', '专用运输箱', '其他'],
    deliveryOptions: ['到场', '自提', '专车配送']
  },
  meat: {
    schemaCode: 'meat',
    schemaName: '畜禽肉类',
    quantityUnit: '吨',
    quantityLabel: '可供数量（吨）',
    quantityPlaceholder: '请输入数量',
    quantityStep: 0.1,
    priceUnit: '元/吨',
    priceLabel: '价格（元/吨）',
    pricePlaceholder: '请输入价格',
    priceStep: 100,
    packagingOptions: ['散装', '纸箱', '泡沫箱', '真空包装'],
    deliveryOptions: ['冷链配送', '自提', '物流配送']
  },
  other: {
    schemaCode: 'other',
    schemaName: '其他品类',
    quantityUnit: '单位',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 1,
    priceUnit: '元',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 1,
    packagingOptions: ['散装', '袋装', '箱装', '其他'],
    deliveryOptions: ['配送', '自提', '物流']
  }
}

/**
 * 禽蛋种苗细分单位配置
 * 根据具体品类使用不同单位
 */
export const poultrySubUnits: Record<string, { quantityUnit: string; quantityLabel: string; priceUnit: string; priceLabel: string }> = {
  '种蛋': {
    quantityUnit: '枚',
    quantityLabel: '可供数量（枚）',
    priceUnit: '元/枚',
    priceLabel: '单价（元/枚）'
  },
  '鸡苗': {
    quantityUnit: '只',
    quantityLabel: '可供数量（只）',
    priceUnit: '元/只',
    priceLabel: '单价（元/只）'
  },
  '鸭苗': {
    quantityUnit: '只',
    quantityLabel: '可供数量（只）',
    priceUnit: '元/只',
    priceLabel: '单价（元/只）'
  },
  '鹅苗': {
    quantityUnit: '只',
    quantityLabel: '可供数量（只）',
    priceUnit: '元/只',
    priceLabel: '单价（元/只）'
  },
  '商品蛋': {
    quantityUnit: '斤',
    quantityLabel: '可供数量（斤）',
    priceUnit: '元/斤',
    priceLabel: '单价（元/斤）'
  }
}

/**
 * 获取业态的单位配置
 */
export function getSchemaUnitConfig(schemaCode: string): SchemaUnitConfig {
  return schemaUnits[schemaCode] ?? schemaUnits['other'] as SchemaUnitConfig
}

/**
 * 获取品类的具体单位配置（考虑子品类特殊单位）
 */
export function getCategoryUnitConfig(schemaCode: string, categoryName: string): {
  quantityUnit: string
  quantityLabel: string
  priceUnit: string
  priceLabel: string
} {
  const baseConfig = getSchemaUnitConfig(schemaCode)

  // 禽蛋种苗业态下，根据具体品类使用特定单位
  if (schemaCode === 'poultry' && categoryName) {
    const subUnit = poultrySubUnits[categoryName]
    if (subUnit) {
      return subUnit
    }
  }

  return {
    quantityUnit: baseConfig.quantityUnit,
    quantityLabel: baseConfig.quantityLabel,
    priceUnit: baseConfig.priceUnit,
    priceLabel: baseConfig.priceLabel
  }
}

/**
 * 格式化价格显示
 */
export function formatPriceWithUnit(price: number | undefined | null, schemaCode: string, categoryName?: string): string {
  if (price === undefined || price === null) return '面议'
  const config = categoryName
    ? getCategoryUnitConfig(schemaCode, categoryName)
    : getSchemaUnitConfig(schemaCode)
  return `¥${price}/${config.priceUnit.replace('元/', '')}`
}

/**
 * 格式化数量显示
 */
export function formatQuantityWithUnit(quantity: number | undefined | null, schemaCode: string, categoryName?: string): string {
  if (quantity === undefined || quantity === null) return '-'
  const config = categoryName
    ? getCategoryUnitConfig(schemaCode, categoryName)
    : getSchemaUnitConfig(schemaCode)
  return `${quantity} ${config.quantityUnit}`
}
