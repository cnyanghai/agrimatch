/**
 * Supply module - Types and API path constants.
 */

// ==================== Types ====================

/**
 * Basis quote request (single entry).
 */
export interface BasisQuoteRequest {
  contractCode: string    // 期货合约代码 (M2509)
  basisPrice: number      // 基差（正=升水，负=贴水）
  availableQty: number    // 可售量（吨）
  remark?: string
}

/**
 * Basis quote response (single entry).
 */
export interface BasisQuoteResponse {
  id: number
  contractCode: string
  contractName: string
  basisPrice: number
  availableQty: number
  soldQty: number
  remainingQty: number
  lastPrice: number | null       // 期货最新价格
  referencePrice: number | null  // 参考现价 = lastPrice + basisPrice
  remark?: string
}

export interface SupplyCreateRequest {
  categoryName: string
  productId?: number
  supplyNo?: string
  origin?: string
  quantity?: number
  /** 报价类型：0=现货一口价（默认），1=基差报价 */
  priceType?: number
  /** 出厂价（现货模式必填，基差模式可为0） */
  exFactoryPrice?: number
  /** 基差报价明细（priceType=1 时必填） */
  basisQuotes?: BasisQuoteRequest[]
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  invoiceType?: string
  packaging?: string
  storageMethod?: string
  priceRulesJson?: string
  paramsJson?: string
  tagsJson?: string
  remark?: string
  expireMinutes?: number
}

export interface SupplyUpdateRequest {
  categoryName?: string
  origin?: string
  quantity?: number
  exFactoryPrice?: number
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  invoiceType?: string
  packaging?: string
  storageMethod?: string
  priceRulesJson?: string
  paramsJson?: string
  remark?: string
  expireMinutes?: number
  status?: number
}

export interface SupplyResponse {
  id: number
  companyId: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  position?: string
  categoryName: string
  schemaCode?: string   // 业态代码：feed, poultry, meat, other
  supplyNo?: string
  origin?: string
  quantity?: number
  remainingQuantity?: number
  /** 报价类型：0=现货一口价，1=基差报价 */
  priceType: number
  exFactoryPrice: number
  /** 基差报价明细（priceType=1 时有值） */
  basisQuotes?: BasisQuoteResponse[]
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  invoiceType?: string
  packaging?: string
  storageMethod?: string
  priceRulesJson?: string
  paramsJson?: string
  remark?: string
  status?: number
  expireMinutes?: number
  expireTime?: string
  distanceKm?: number
  deliveredPrice?: number
  createTime?: string
  updateTime?: string
}

export interface SupplyListParams {
  companyId?: number
  userId?: number
  categoryName?: string
  status?: number
  activeOnly?: boolean
  includeExpired?: boolean
  orderBy?: string
  order?: string
}

// ==================== Supply Template Types ====================

export interface SupplyTemplateCreateRequest {
  templateName: string
  templateJson: string
}

export interface SupplyTemplateResponse {
  id: number
  templateName: string
  templateJson: string
  createTime?: string
}

// ==================== API Path Constants ====================

export const SUPPLY_API = {
  /** POST - Create supply */
  CREATE: '/api/supplies',
  /** GET - List supplies */
  LIST: '/api/supplies',
  /** PUT - Update supply: /api/supplies/:id */
  UPDATE: (id: number) => `/api/supplies/${id}`,
  /** DELETE - Delete supply: /api/supplies/:id */
  DELETE: (id: number) => `/api/supplies/${id}`,
  /** GET - Get next supply number */
  NEXT_NO: '/api/supplies/next-no',
} as const

export const SUPPLY_TEMPLATE_API = {
  /** POST - Create supply template */
  CREATE: '/api/supply-templates',
  /** GET - List my supply templates */
  LIST: '/api/supply-templates',
  /** DELETE - Delete supply template: /api/supply-templates/:id */
  DELETE: (id: number) => `/api/supply-templates/${id}`,
} as const
