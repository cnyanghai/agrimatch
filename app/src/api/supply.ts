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

export interface SupplyCreateRequest {
  categoryName: string
  origin?: string
  quantity?: number
  exFactoryPrice?: number
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  remark?: string
}

export function createSupply(req: SupplyCreateRequest) {
  return post<number>('/api/supplies', req)
}

export interface SupplyUpdateRequest {
  quantity?: number
  exFactoryPrice?: number
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  remark?: string
  expireMinutes?: number
}

export function updateSupply(id: number, req: SupplyUpdateRequest) {
  return put<void>(`/api/supplies/${id}`, req)
}

export function deleteSupply(id: number) {
  return del<void>(`/api/supplies/${id}`)
}
