import { http, type Result } from './http'

export interface SupplyCreateRequest {
  categoryName: string
  productId?: number
  supplyNo?: string
  origin?: string
  quantity?: number
  exFactoryPrice: number
  shipAddress?: string
  deliveryMode?: string
  packaging?: string
  storageMethod?: string
  priceRulesJson?: string
  paramsJson?: string
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
  categoryName: string
  supplyNo?: string
  origin?: string
  quantity?: number
  remainingQuantity?: number
  exFactoryPrice: number
  shipAddress?: string
  deliveryMode?: string
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

export async function createSupply(req: SupplyCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/supplies', req)
  return data
}

export async function listSupplies(params: {
  companyId?: number
  userId?: number
  categoryName?: string
  status?: number
  activeOnly?: boolean
  includeExpired?: boolean
  orderBy?: string
  order?: string
}) {
  const { data } = await http.get<Result<SupplyResponse[]>>('/api/supplies', { params })
  return data
}

export async function updateSupply(id: number, req: SupplyUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/supplies/${id}`, req)
  return data
}

export async function deleteSupply(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/supplies/${id}`)
  return data
}

export async function getNextSupplyNo() {
  const { data } = await http.get<Result<string>>('/api/supplies/next-no')
  return data
}


