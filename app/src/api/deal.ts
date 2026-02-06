import { get, post } from '../utils/request'

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
  // Joined fields
  buyerCompanyName?: string
  sellerCompanyName?: string
  categoryName?: string
}

/** Create a deal (confirm transaction) */
export function createDeal(req: DealCreateRequest) {
  return post<number>('/api/deals', req)
}

/** List deals by requirement */
export function listDealsByRequirement(requirementId: number) {
  return get<DealResponse[]>('/api/deals', { requirementId })
}

/** Get deal detail */
export function getDeal(id: number) {
  return get<DealResponse>(`/api/deals/${id}`)
}
