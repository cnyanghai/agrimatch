/**
 * 业态单位配置
 * 不同业态使用不同的计量单位和价格单位
 *
 * 业态体系：
 * - feed: 原料饲料 - 大宗原料及核心添加剂
 * - breed: 生物种苗 - 种禽、种蛋、鱼苗、畜种资源
 * - process: 农牧加工 - 成品、半成品
 * - equipment: 装备物流 - 自动化养殖系统、农机、物流
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
    schemaName: '原料饲料',
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
  breed: {
    schemaCode: 'breed',
    schemaName: '生物种苗',
    quantityUnit: '只',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 100,
    priceUnit: '元/只',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 0.1,
    packagingOptions: ['专用运输箱', '纸箱', '蛋托', '其他'],
    deliveryOptions: ['到场', '自提', '专车配送']
  },
  process: {
    schemaCode: 'process',
    schemaName: '农牧加工',
    quantityUnit: '吨',
    quantityLabel: '可供数量（吨）',
    quantityPlaceholder: '请输入数量',
    quantityStep: 0.1,
    priceUnit: '元/吨',
    priceLabel: '价格（元/吨）',
    pricePlaceholder: '请输入价格',
    priceStep: 100,
    packagingOptions: ['散装', '纸箱', '真空包装', '冷冻包装'],
    deliveryOptions: ['冷链配送', '自提', '物流配送']
  },
  equipment: {
    schemaCode: 'equipment',
    schemaName: '装备物流',
    quantityUnit: '台/套',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 1,
    priceUnit: '元',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 100,
    packagingOptions: ['裸装', '木架', '纸箱', '其他'],
    deliveryOptions: ['送货上门', '自提', '物流']
  },
  // 向后兼容：保留旧的业态代码映射
  poultry: {
    schemaCode: 'breed',
    schemaName: '生物种苗',
    quantityUnit: '只',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 100,
    priceUnit: '元/只',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 0.1,
    packagingOptions: ['专用运输箱', '纸箱', '蛋托', '其他'],
    deliveryOptions: ['到场', '自提', '专车配送']
  },
  meat: {
    schemaCode: 'process',
    schemaName: '农牧加工',
    quantityUnit: '吨',
    quantityLabel: '可供数量（吨）',
    quantityPlaceholder: '请输入数量',
    quantityStep: 0.1,
    priceUnit: '元/吨',
    priceLabel: '价格（元/吨）',
    pricePlaceholder: '请输入价格',
    priceStep: 100,
    packagingOptions: ['散装', '纸箱', '真空包装', '冷冻包装'],
    deliveryOptions: ['冷链配送', '自提', '物流配送']
  },
  other: {
    schemaCode: 'equipment',
    schemaName: '装备物流',
    quantityUnit: '台/套',
    quantityLabel: '可供数量',
    quantityPlaceholder: '请输入数量',
    quantityStep: 1,
    priceUnit: '元',
    priceLabel: '单价（元）',
    pricePlaceholder: '请输入单价',
    priceStep: 100,
    packagingOptions: ['裸装', '木架', '纸箱', '其他'],
    deliveryOptions: ['送货上门', '自提', '物流']
  }
}

/**
 * 生物种苗细分单位配置
 * 根据具体品类使用不同单位
 */
export const breedSubUnits: Record<string, { quantityUnit: string; quantityLabel: string; priceUnit: string; priceLabel: string }> = {
  // 禽类种苗
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
  },
  // 畜类种苗
  '种猪': {
    quantityUnit: '头',
    quantityLabel: '可供数量（头）',
    priceUnit: '元/头',
    priceLabel: '单价（元/头）'
  },
  '种牛': {
    quantityUnit: '头',
    quantityLabel: '可供数量（头）',
    priceUnit: '元/头',
    priceLabel: '单价（元/头）'
  },
  '种羊': {
    quantityUnit: '只',
    quantityLabel: '可供数量（只）',
    priceUnit: '元/只',
    priceLabel: '单价（元/只）'
  },
  // 水产种苗
  '鱼苗': {
    quantityUnit: '万尾',
    quantityLabel: '可供数量（万尾）',
    priceUnit: '元/万尾',
    priceLabel: '单价（元/万尾）'
  },
  '虾苗': {
    quantityUnit: '万尾',
    quantityLabel: '可供数量（万尾）',
    priceUnit: '元/万尾',
    priceLabel: '单价（元/万尾）'
  },
  '蟹苗': {
    quantityUnit: '斤',
    quantityLabel: '可供数量（斤）',
    priceUnit: '元/斤',
    priceLabel: '单价（元/斤）'
  }
}

/**
 * 农牧加工细分单位配置
 */
export const processSubUnits: Record<string, { quantityUnit: string; quantityLabel: string; priceUnit: string; priceLabel: string }> = {
  // 水产加工类通常按公斤
  '冷冻鱼类': {
    quantityUnit: '公斤',
    quantityLabel: '可供数量（公斤）',
    priceUnit: '元/公斤',
    priceLabel: '单价（元/公斤）'
  },
  '冷冻虾类': {
    quantityUnit: '公斤',
    quantityLabel: '可供数量（公斤）',
    priceUnit: '元/公斤',
    priceLabel: '单价（元/公斤）'
  },
  '水产制品': {
    quantityUnit: '公斤',
    quantityLabel: '可供数量（公斤）',
    priceUnit: '元/公斤',
    priceLabel: '单价（元/公斤）'
  }
}

/**
 * 装备物流细分单位配置
 */
export const equipmentSubUnits: Record<string, { quantityUnit: string; quantityLabel: string; priceUnit: string; priceLabel: string }> = {
  // 物流服务类按次计价
  '冷链物流': {
    quantityUnit: '次',
    quantityLabel: '服务次数',
    priceUnit: '元/次',
    priceLabel: '单价（元/次）'
  },
  '活禽运输': {
    quantityUnit: '次',
    quantityLabel: '服务次数',
    priceUnit: '元/次',
    priceLabel: '单价（元/次）'
  },
  '大宗散料运输': {
    quantityUnit: '吨',
    quantityLabel: '运输量（吨）',
    priceUnit: '元/吨',
    priceLabel: '运费（元/吨）'
  },
  // 包装耗材
  '蛋托/蛋箱': {
    quantityUnit: '个',
    quantityLabel: '数量（个）',
    priceUnit: '元/个',
    priceLabel: '单价（元/个）'
  },
  '编织袋/吨包': {
    quantityUnit: '个',
    quantityLabel: '数量（个）',
    priceUnit: '元/个',
    priceLabel: '单价（元/个）'
  }
}

// 向后兼容：保留旧的导出名
export const poultrySubUnits = breedSubUnits

/**
 * 获取业态的单位配置
 */
export function getSchemaUnitConfig(schemaCode: string): SchemaUnitConfig {
  return schemaUnits[schemaCode] ?? schemaUnits['equipment'] as SchemaUnitConfig
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

  // 生物种苗业态下，根据具体品类使用特定单位
  if ((schemaCode === 'breed' || schemaCode === 'poultry') && categoryName) {
    const subUnit = breedSubUnits[categoryName]
    if (subUnit) {
      return subUnit
    }
  }

  // 农牧加工业态下，根据具体品类使用特定单位
  if ((schemaCode === 'process' || schemaCode === 'meat') && categoryName) {
    const subUnit = processSubUnits[categoryName]
    if (subUnit) {
      return subUnit
    }
  }

  // 装备物流业态下，根据具体品类使用特定单位
  if ((schemaCode === 'equipment' || schemaCode === 'other') && categoryName) {
    const subUnit = equipmentSubUnits[categoryName]
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

/**
 * 规范化业态代码（将旧代码转换为新代码）
 */
export function normalizeSchemaCode(schemaCode: string): string {
  const mapping: Record<string, string> = {
    'poultry': 'breed',
    'meat': 'process',
    'other': 'equipment'
  }
  return mapping[schemaCode] || schemaCode
}
