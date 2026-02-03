/**
 * Deal module - Types and API path constants.
 */

// ==================== Types ====================

export interface DealCreateRequest {
  requirementId: number
  supplyId: number
  quantity: number
  finalExFactoryPrice?: number
  deliveryMode?: string
}

export interface DealResponse {
  id: number
  requirementId: number
  supplyId: number
  buyerCompanyId: number
  sellerCompanyId: number
  buyerUserId: number
  sellerUserId: number
  quantity: number
  finalExFactoryPrice: number
  deliveryMode?: string
  distanceKm?: number
  freightRatePerTonKm?: number
  deliveredPrice?: number
  status?: number
  createTime?: string
}

// ==================== API Path Constants ====================

export const DEAL_API = {
  /** POST - Create deal */
  CREATE: '/api/deals',
  /** GET - List deals (by requirementId) */
  LIST: '/api/deals',
  /** GET - Get deal by ID: /api/deals/:id */
  GET_BY_ID: (id: number) => `/api/deals/${id}`,
} as const
