import { http, type Result } from './http'

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

export async function createDeal(req: DealCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/deals', req)
  return data
}

export async function listDealsByRequirement(requirementId: number) {
  const { data } = await http.get<Result<DealResponse[]>>('/api/deals', { params: { requirementId } })
  return data
}

export async function getDeal(id: number) {
  const { data } = await http.get<Result<DealResponse>>(`/api/deals/${id}`)
  return data
}


