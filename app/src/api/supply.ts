import { get, post, put, del } from '../utils/request'

/** 基差报价响应（单条） */
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

export interface SupplyResponse {
  id: number
  companyId: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  position?: string
  categoryName: string
  schemaCode?: string
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
  remark?: string
  paramsJson?: string
  priceRulesJson?: string
  imagesJson?: string
  status?: number
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
  schemaCode?: string
  status?: number
  activeOnly?: boolean
  includeExpired?: boolean
  orderBy?: string
  order?: string
}

export function listSupplies(params?: SupplyListParams) {
  return get<SupplyResponse[]>('/api/supplies', params)
}

export function getSupply(id: number) {
  return get<SupplyResponse>(`/api/supplies/${id}`)
}

/** 基差报价请求（单条） */
export interface BasisQuoteRequest {
  contractCode: string    // 期货合约代码 (M2509)
  basisPrice: number      // 基差（正=升水，负=贴水）
  availableQty: number    // 可售量（吨）
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
  imagesJson?: string
}

export function createSupply(req: SupplyCreateRequest) {
  return post<number>('/api/supplies', req)
}

export interface SupplyUpdateRequest {
  categoryName?: string
  origin?: string
  quantity?: number
  remainingQuantity?: number
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

export function updateSupply(id: number, req: SupplyUpdateRequest) {
  return put<void>(`/api/supplies/${id}`, req)
}

export function deleteSupply(id: number) {
  return del<void>(`/api/supplies/${id}`)
}

/** 获取下一个供应编号 */
export function getNextSupplyNo() {
  return get<string>('/api/supplies/next-no')
}

// ========== 供应模板 API ==========

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

export function getMySupplyTemplates() {
  return get<SupplyTemplateResponse[]>('/api/supply-templates')
}

export function createSupplyTemplate(req: SupplyTemplateCreateRequest) {
  return post<number>('/api/supply-templates', req)
}

export function deleteSupplyTemplate(id: number) {
  return del<void>(`/api/supply-templates/${id}`)
}
