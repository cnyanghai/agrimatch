import { get, post } from '../utils/request'

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
  priceType: number
  exFactoryPrice: number
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  remark?: string
  status?: number
  expireTime?: string
  distanceKm?: number
  createTime?: string
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
