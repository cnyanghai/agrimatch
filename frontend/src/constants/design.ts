/**
 * 设计系统常量
 * AgriMatch 统一设计规范
 */

// 按钮样式
export const BUTTON_STYLES = {
  primary: 'bg-brand-600 hover:bg-brand-700 text-white',
  secondary: 'bg-gray-100 hover:bg-gray-200 text-gray-700',
  outline: 'border border-gray-200 hover:bg-gray-50 text-gray-700',
  danger: 'bg-red-50 hover:bg-red-100 text-red-600',
  ghost: 'hover:bg-gray-100 text-gray-600',
  base: 'px-4 py-2 rounded-lg font-bold transition-all active:scale-95'
} as const

// 卡片样式
export const CARD_STYLES = {
  base: 'bg-white rounded-2xl border border-gray-100',
  interactive: 'hover:shadow-md hover:border-brand-100 hover:-translate-y-1 transition-all cursor-pointer',
  elevated: 'shadow-sm'
} as const

// 空状态样式
export const EMPTY_STATE = {
  iconContainer: 'w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center',
  icon: 'w-10 h-10 text-gray-300'
} as const

// 圆角规范
export const RADIUS = {
  sm: 'rounded-lg',    // 8px - 按钮、输入框、小标签
  md: 'rounded-xl',    // 12px - 卡片内元素
  lg: 'rounded-2xl',   // 16px - 卡片
  xl: 'rounded-3xl'    // 24px - 大卡片、弹窗
} as const

// 颜色语义
export const COLOR_SEMANTIC = {
  // 供应侧（若竹色 Ruozhu Green）
  supply: {
    bg: 'bg-brand-50',
    text: 'text-brand-600',
    border: 'border-brand-200',
    hover: 'hover:bg-brand-100'
  },
  // 采购侧（秋波蓝 Qiubo Blue）
  purchase: {
    bg: 'bg-autumn-50',
    text: 'text-autumn-600',
    border: 'border-autumn-200',
    hover: 'hover:bg-autumn-100'
  },
  // 警告
  warning: {
    bg: 'bg-amber-50',
    text: 'text-amber-600',
    border: 'border-amber-200',
    hover: 'hover:bg-amber-100'
  },
  // 错误
  error: {
    bg: 'bg-red-50',
    text: 'text-red-600',
    border: 'border-red-200',
    hover: 'hover:bg-red-100'
  },
  // 成功
  success: {
    bg: 'bg-green-50',
    text: 'text-green-600',
    border: 'border-green-200',
    hover: 'hover:bg-green-100'
  }
} as const

// 动画时间
export const ANIMATION_TIMING = {
  enter: 'duration-200',      // 入场动画
  exit: 'duration-150',       // 离场动画
  default: 'duration-200'     // 默认过渡
} as const

// 阴影规范
export const SHADOWS = {
  sm: 'shadow-sm',
  md: 'shadow-md',
  lg: 'shadow-lg',
  brand: 'shadow-lg shadow-brand-900/10'
} as const

// 文字规范
export const TEXT_STYLES = {
  heading1: 'text-3xl font-black text-gray-900',
  heading2: 'text-2xl font-bold text-gray-900',
  heading3: 'text-xl font-bold text-gray-900',
  heading4: 'text-lg font-bold text-gray-900',
  body: 'text-sm text-gray-700',
  caption: 'text-xs text-gray-500',
  label: 'text-[10px] font-bold text-gray-400 uppercase tracking-wider'
} as const
