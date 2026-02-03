/**
 * Business constants for the WoGu (沃谷) platform.
 * These constants are shared between frontend and backend consumers.
 */

// ==================== Contract Status ====================

/** Contract status codes (numeric, used in DB) */
export const CONTRACT_STATUS = {
  DRAFT: 0,
  PENDING_SIGN: 1,
  SIGNED: 2,
  EXECUTING: 3,
  COMPLETED: 4,
  CANCELLED: 5,
} as const

/** Contract status labels (Chinese) */
export const CONTRACT_STATUS_LABEL: Record<number, string> = {
  [CONTRACT_STATUS.DRAFT]: '草稿',
  [CONTRACT_STATUS.PENDING_SIGN]: '待签署',
  [CONTRACT_STATUS.SIGNED]: '已签署',
  [CONTRACT_STATUS.EXECUTING]: '履约中',
  [CONTRACT_STATUS.COMPLETED]: '已完成',
  [CONTRACT_STATUS.CANCELLED]: '已取消',
}

// ==================== Payment Methods ====================

/** Payment method code -> label mapping */
export const PAYMENT_METHOD_MAP: Record<string, string> = {
  '01': '款到发货',
  '02': '货到付款',
  '03': '账期30天',
  '04': '账期60天',
  '05': '分期付款',
  '06': '预付定金',
  '款到发货': '款到发货',
  '货到付款': '货到付款',
  '账期30天': '账期30天',
  '账期60天': '账期60天',
  '分期付款': '分期付款',
  '预付定金': '预付定金',
}

/** Get payment method display text */
export function getPaymentMethodText(code?: string): string {
  if (!code) return '-'
  return PAYMENT_METHOD_MAP[code] || code
}

// ==================== Delivery Modes ====================

/** Delivery mode code -> label mapping */
export const DELIVERY_MODE_MAP: Record<string, string> = {
  '01': '送货上门',
  '02': '自提',
  '03': '物流配送',
  '04': '快递',
  '送货上门': '送货上门',
  '自提': '自提',
  '物流配送': '物流配送',
  '快递': '快递',
}

/** Get delivery mode display text */
export function getDeliveryModeText(code?: string): string {
  if (!code) return '-'
  return DELIVERY_MODE_MAP[code] || code
}

// ==================== Supply/Requirement Status ====================

export const LISTING_STATUS = {
  /** Active / on-shelf */
  ACTIVE: 1,
  /** Inactive / off-shelf */
  INACTIVE: 0,
  /** Deleted */
  DELETED: -1,
} as const

// ==================== Price Types ====================

export const PRICE_TYPE = {
  /** 现货一口价 */
  SPOT: 0,
  /** 基差报价 */
  BASIS: 1,
} as const

// ==================== SMS Code Types ====================

export const SMS_TYPE = {
  REGISTER: 1,
  LOGIN: 2,
  IDENTITY_VERIFY: 3,
  CONTRACT_SIGN: 4,
} as const

// ==================== User Gender ====================

export const GENDER = {
  MALE: 1,
  FEMALE: 2,
} as const

export const GENDER_LABEL: Record<number, string> = {
  [GENDER.MALE]: '男',
  [GENDER.FEMALE]: '女',
}

// ==================== Company Types ====================

export const COMPANY_TYPE_LABEL: Record<string, string> = {
  feed_factory: '饲料厂',
  trader: '贸易商',
  grain_depot: '粮库',
  processor: '加工企业',
  logistics: '物流企业',
  other: '其他',
}

// ==================== Schema (Business Domain) Codes ====================

export const SCHEMA_CODES = {
  FEED: 'feed',
  BREED: 'breed',
  PROCESS: 'process',
  EQUIPMENT: 'equipment',
  // Legacy aliases
  POULTRY: 'poultry',
  MEAT: 'meat',
  OTHER: 'other',
} as const

export const SCHEMA_LABEL: Record<string, string> = {
  feed: '原料饲料',
  breed: '生物种苗',
  process: '农牧加工',
  equipment: '装备物流',
  poultry: '生物种苗',
  meat: '农牧加工',
  other: '装备物流',
}

/**
 * Normalize legacy schema codes to current codes.
 */
export function normalizeSchemaCode(schemaCode: string): string {
  const mapping: Record<string, string> = {
    poultry: 'breed',
    meat: 'process',
    other: 'equipment',
  }
  return mapping[schemaCode] || schemaCode
}

// ==================== Post Types ====================

export const POST_TYPES = {
  GENERAL: 'general',
  POLL: 'poll',
  PAID: 'paid',
} as const

// ==================== Chat Message Types ====================

export const CHAT_MSG_TYPES = {
  TEXT: 'TEXT',
  QUOTE: 'QUOTE',
  SYSTEM: 'SYSTEM',
  ATTACHMENT: 'ATTACHMENT',
} as const

export const CHAT_QUOTE_STATUS = {
  OFFERED: 'OFFERED',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  EXPIRED: 'EXPIRED',
} as const

// ==================== JD Redeem Status ====================

export const JD_REDEEM_STATUS = {
  PENDING: 0,
  FULFILLED: 1,
  FAILED: 2,
} as const

export const JD_REDEEM_STATUS_LABEL: Record<number, string> = {
  [JD_REDEEM_STATUS.PENDING]: '待发卡',
  [JD_REDEEM_STATUS.FULFILLED]: '已发卡',
  [JD_REDEEM_STATUS.FAILED]: '已失败',
}

// ==================== Milestone Types ====================

export const MILESTONE_STATUS = {
  PENDING: 'pending',
  SUBMITTED: 'submitted',
  CONFIRMED: 'confirmed',
  REJECTED: 'rejected',
} as const

// ==================== Sign Types ====================

export const SIGN_TYPES = {
  SEAL: 'seal',
  HANDWRITE: 'handwrite',
  TYPED: 'typed',
  SEAL_HANDWRITE: 'seal_handwrite',
} as const
