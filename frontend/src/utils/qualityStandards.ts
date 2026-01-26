/**
 * 质量标准配置
 * 根据产品类别提供不同的质量标准模板
 */

export interface QualityStandard {
  label: string
  value: string
}

// 默认质量标准（谷物类）
const defaultStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 14.0%' },
  { label: '杂质 (Impurity)', value: '≤ 1.0%' },
  { label: '霉变粒 (Moldy)', value: '≤ 2.0%' }
]

// 玉米质量标准
const cornStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 14.0%' },
  { label: '杂质 (Impurity)', value: '≤ 1.0%' },
  { label: '霉变粒 (Moldy)', value: '≤ 2.0%' },
  { label: '不完善粒', value: '≤ 8.0%' },
  { label: '容重', value: '≥ 685g/L' }
]

// 小麦质量标准
const wheatStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 13.0%' },
  { label: '杂质 (Impurity)', value: '≤ 1.0%' },
  { label: '不完善粒', value: '≤ 6.0%' },
  { label: '容重', value: '≥ 750g/L' },
  { label: '面筋含量', value: '≥ 28%' }
]

// 大豆质量标准
const soybeanStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 13.0%' },
  { label: '杂质 (Impurity)', value: '≤ 1.0%' },
  { label: '损伤粒', value: '≤ 3.0%' },
  { label: '蛋白质含量', value: '≥ 38%' },
  { label: '油脂含量', value: '≥ 18%' }
]

// 豆粕质量标准
const soybeanMealStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 13.0%' },
  { label: '粗蛋白', value: '≥ 43%' },
  { label: '粗纤维', value: '≤ 7.0%' },
  { label: '粗灰分', value: '≤ 7.0%' },
  { label: '脲酶活性', value: '≤ 0.3 U/g' }
]

// 菜粕质量标准
const rapeseedMealStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 12.0%' },
  { label: '粗蛋白', value: '≥ 36%' },
  { label: '粗纤维', value: '≤ 14.0%' },
  { label: '粗灰分', value: '≤ 8.0%' },
  { label: '硫甙', value: '≤ 40 μmol/g' }
]

// 鱼粉质量标准
const fishMealStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 10.0%' },
  { label: '粗蛋白', value: '≥ 65%' },
  { label: '粗脂肪', value: '≤ 12.0%' },
  { label: '粗灰分', value: '≤ 17.0%' },
  { label: '盐分', value: '≤ 3.0%' },
  { label: '挥发性盐基氮', value: '≤ 120 mg/100g' }
]

// 棉粕质量标准
const cottonseedMealStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 12.0%' },
  { label: '粗蛋白', value: '≥ 40%' },
  { label: '粗纤维', value: '≤ 15.0%' },
  { label: '游离棉酚', value: '≤ 400 mg/kg' }
]

// 预混料/添加剂标准
const premixStandards: QualityStandard[] = [
  { label: '水分 (Moisture)', value: '≤ 10.0%' },
  { label: '混合均匀度', value: 'CV ≤ 10%' },
  { label: '有效成分含量', value: '符合标签标示' },
  { label: '重金属', value: '符合GB 13078' }
]

// 类别关键词映射
const categoryKeywords: Record<string, QualityStandard[]> = {
  // 玉米类
  '玉米': cornStandards,
  'corn': cornStandards,
  '干玉米': cornStandards,
  '湿玉米': cornStandards,

  // 小麦类
  '小麦': wheatStandards,
  'wheat': wheatStandards,
  '麦子': wheatStandards,

  // 大豆类
  '大豆': soybeanStandards,
  '黄豆': soybeanStandards,
  'soybean': soybeanStandards,

  // 豆粕类
  '豆粕': soybeanMealStandards,
  '大豆粕': soybeanMealStandards,
  'soybean meal': soybeanMealStandards,

  // 菜粕类
  '菜粕': rapeseedMealStandards,
  '菜籽粕': rapeseedMealStandards,
  'rapeseed meal': rapeseedMealStandards,

  // 鱼粉类
  '鱼粉': fishMealStandards,
  'fish meal': fishMealStandards,
  '进口鱼粉': fishMealStandards,
  '国产鱼粉': fishMealStandards,

  // 棉粕类
  '棉粕': cottonseedMealStandards,
  '棉籽粕': cottonseedMealStandards,

  // 预混料/添加剂
  '预混料': premixStandards,
  '添加剂': premixStandards,
  '维生素': premixStandards,
  '氨基酸': premixStandards
}

/**
 * 根据产品名称或类别名称获取质量标准
 */
export function getQualityStandards(productName?: string, categoryName?: string): QualityStandard[] {
  const searchTerms = [productName, categoryName].filter(Boolean).join(' ').toLowerCase()

  if (!searchTerms) return defaultStandards

  // 遍历关键词映射，查找匹配的标准
  for (const [keyword, standards] of Object.entries(categoryKeywords)) {
    if (searchTerms.includes(keyword.toLowerCase())) {
      return standards
    }
  }

  return defaultStandards
}

/**
 * 获取所有可用的质量标准模板
 */
export function getAllQualityTemplates(): Record<string, QualityStandard[]> {
  return {
    '默认（谷物）': defaultStandards,
    '玉米': cornStandards,
    '小麦': wheatStandards,
    '大豆': soybeanStandards,
    '豆粕': soybeanMealStandards,
    '菜粕': rapeseedMealStandards,
    '鱼粉': fishMealStandards,
    '棉粕': cottonseedMealStandards,
    '预混料/添加剂': premixStandards
  }
}
