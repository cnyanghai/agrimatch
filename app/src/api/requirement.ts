import { get, post, put, del } from '../utils/request'

export interface BasisQuoteRequest {
  contractCode: string
  basisPrice: number
  availableQty: number
  remark?: string
}

export interface BasisQuoteResponse {
  id: number
  contractCode: string
  contractName: string
  basisPrice: number
  availableQty: number
  soldQty: number
  remainingQty: number
  lastPrice: number | null
  referencePrice: number | null
  remark?: string
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
  schemaCode?: string
  contractNo?: string
  quantity?: number
  remainingQuantity?: number
  expectedPrice?: number
  /** 报价类型：0=期望价，1=基差报价 */
  priceType: number
  /** 基差报价明细（priceType=1 时有值） */
  basisQuotes?: BasisQuoteResponse[]
  packaging?: string
  paymentMethod?: string
  deliveryMethod?: string
  purchaseAddress?: string
  remark?: string
  paramsJson?: string
  imagesJson?: string
  status?: number
  expireTime?: string
  distanceKm?: number
  createTime?: string
}

export interface RequirementListParams {
  companyId?: number
  userId?: number
  categoryName?: string
  schemaCode?: string
  status?: number
  includeExpired?: boolean
  orderBy?: string
  order?: string
}

export function listRequirements(params?: RequirementListParams) {
  return get<RequirementResponse[]>('/api/requirements', params)
}

export function getRequirement(id: number) {
  return get<RequirementResponse>(`/api/requirements/${id}`)
}

/**
 * 基差报价请求（单条）
 */
export interface BasisQuoteRequest {
  contractCode: string    // 期货合约代码 (M2509)
  basisPrice: number      // 基差（正=升水，负=贴水）
  availableQty: number    // 可售量（吨）
  remark?: string
}

/**
 * 基差报价响应（单条）
 */
export interface BasisQuoteResponse {
  id: number
  contractCode: string
  contractName: string
  basisPrice: number
  availableQty: number
  soldQty: number
  remainingQty: number
  lastPrice: number | null  // 期货最新价格
  referencePrice: number | null  // 参考现价 = lastPrice + basisPrice
  remark?: string
}

export interface RequirementCreateRequest {
  categoryName: string
  productId?: number
  contractNo?: string
  quantity?: number
  /** 报价类型：0=期望价（默认），1=基差报价 */
  priceType?: number
  /** 期望价（期望价模式必填，基差模式可为0） */
  expectedPrice?: number
  /** 基差报价明细（priceType=1 时必填） */
  basisQuotes?: BasisQuoteRequest[]
  packaging?: string
  invoiceType?: string
  paymentMethod?: string
  deliveryMethod?: string
  purchaseAddress?: string
  paramsJson?: string
  tagsJson?: string
  remark?: string
  expireMinutes?: number
  imagesJson?: string
}

export function createRequirement(req: RequirementCreateRequest) {
  return post<number>('/api/requirements', req)
}

export interface RequirementUpdateRequest {
  categoryName?: string
  quantity?: number
  remainingQuantity?: number
  expectedPrice?: number
  packaging?: string
  invoiceType?: string
  paymentMethod?: string
  deliveryMethod?: string
  purchaseAddress?: string
  paramsJson?: string
  remark?: string
  expireMinutes?: number
  status?: number
  /** 报价类型：0=期望价，1=基差报价 */
  priceType?: number
  /** 期望价（期望价模式必填，基差模式可为0） */
  exFactoryPrice?: number
  /** 基差报价明细（priceType=1 时有值） */
  basisQuotes?: BasisQuoteRequest[]
}

export function updateRequirement(id: number, req: RequirementUpdateRequest) {
  return put<void>(`/api/requirements/${id}`, req)
}

/**
 * 删除采购需求
 */
export function deleteRequirement(id: number) {
  return del<void>(`/api/requirements/${id}`)
}

/**
 * 获取下一个采购编号
 */
export function getNextRequirementNo() {
  return get<string>('/api/requirements/next-no')
}

// ========== 采购模板 API ==========

export interface RequirementTemplateCreateRequest {
  templateName: string
  templateJson: string
}

export interface RequirementTemplateResponse {
  id: number
  templateName: string
  templateJson: string
  createTime?: string
  updateTime?: string
}

export function listMyRequirementTemplates() {
  return get<RequirementTemplateResponse[]>('/api/requirement-templates')
}

export function createRequirementTemplate(req: RequirementTemplateCreateRequest) {
  return post<number>('/api/requirement-templates', req)
}

export function deleteRequirementTemplate(id: number) {
  return del<void>(`/api/requirement-templates/${id}`)
}
