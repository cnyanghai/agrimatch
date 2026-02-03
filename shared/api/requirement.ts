/**
 * Requirement module - Types and API path constants.
 */

// ==================== Types ====================

export interface RequirementCreateRequest {
  categoryName: string
  productId?: number
  contractNo?: string
  quantity?: number
  expectedPrice?: number
  packaging?: string
  invoiceType?: string
  paymentMethod?: string
  deliveryMethod?: string
  paramsJson?: string
  tagsJson?: string
  remark?: string
  expireMinutes?: number
  purchaseAddress?: string
}

export interface RequirementUpdateRequest {
  categoryName?: string
  quantity?: number
  expectedPrice?: number
  packaging?: string
  invoiceType?: string
  paymentMethod?: string
  deliveryMethod?: string
  paramsJson?: string
  remark?: string
  expireMinutes?: number
  purchaseAddress?: string
  status?: number
}

export interface RequirementResponse {
  id: number
  companyId: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  position?: string
  categoryName: string
  contractNo?: string
  quantity?: number
  remainingQuantity?: number
  expectedPrice?: number
  packaging?: string
  invoiceType?: string
  paymentMethod?: string
  deliveryMethod?: string
  paramsJson?: string
  remark?: string
  expireMinutes?: number
  expireTime?: string
  purchaseAddress?: string
  distanceKm?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface RequirementListParams {
  companyId?: number
  userId?: number
  categoryName?: string
  status?: number
  includeExpired?: boolean
  orderBy?: string
  order?: string
}

// ==================== API Path Constants ====================

export const REQUIREMENT_API = {
  /** POST - Create requirement */
  CREATE: '/api/requirements',
  /** GET - List requirements */
  LIST: '/api/requirements',
  /** GET - Get requirement by ID: /api/requirements/:id */
  GET_BY_ID: (id: number) => `/api/requirements/${id}`,
  /** PUT - Update requirement: /api/requirements/:id */
  UPDATE: (id: number) => `/api/requirements/${id}`,
  /** DELETE - Delete requirement: /api/requirements/:id */
  DELETE: (id: number) => `/api/requirements/${id}`,
  /** GET - Get next requirement number */
  NEXT_NO: '/api/requirements/next-no',
} as const
