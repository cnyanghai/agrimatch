/**
 * 运行时颜色常量 — 与 brain/design/tokens.json 保持一致
 *
 * 用于 <script> 中需要传递颜色字符串的场景（如 WgIcon 的 color 属性）
 * <style> 中请使用 SCSS 变量（如 $brand-600），禁止使用本文件的常量
 *
 * ⚠️ 修改颜色时必须同步修改 tokens.json 并重新生成
 */

// 品牌色 — 沃野绿
export const BRAND_600 = '#2D6A4F'
export const BRAND_700 = '#1a4532'
export const BRAND_50 = '#f0f7f4'

// 暖中性色
export const WARM_100 = '#F5F0E8'
export const WARM_200 = '#E8E0D4'
export const WARM_300 = '#D6CCC0'
export const WARM_400 = '#A8A29E'
export const WARM_500 = '#78716C'
export const WARM_600 = '#57534E'
export const WARM_900 = '#1C1917'

// 强调色 — 赭石橙
export const ACCENT_400 = '#E76F51'

// 采购色 — 麦穗金
export const AUTUMN_500 = '#c28a55'

// 行动色 — CTA 蓝
export const ACTION_600 = '#2563eb'

// 采购辅助色阶
export const AUTUMN_400 = '#D4A373'

// 语义色
export const COLOR_SUCCESS = '#22c55e'
export const SUCCESS_600 = '#16a34a'
export const COLOR_WARNING = '#f59e0b'
export const WARNING_700 = '#B45309'
export const COLOR_ERROR = '#ef4444'
export const ERROR_600 = '#dc2626'
export const ERROR_700 = '#b91c1c'

// 纯色
export const WHITE = '#ffffff'
export const BLACK = '#000000'
export const ERROR_500 = '#dc2626'
export const SUCCESS_500 = '#16a34a'
