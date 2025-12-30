import { http, type Result } from './http'

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
  remark?: string
  expireMinutes?: number
  purchaseAddress?: string
}

export interface RequirementUpdateRequest {
  categoryName?: string
  quantity?: number
  packaging?: string
  paymentMethod?: string
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

export async function createRequirement(req: RequirementCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/requirements', req)
  return data
}

export async function listRequirements(params: {
  companyId?: number
  userId?: number
  categoryName?: string
  status?: number
  orderBy?: string
  order?: string
}) {
  const { data } = await http.get<Result<RequirementResponse[]>>('/api/requirements', { params })
  return data
}

export async function getRequirement(id: number) {
  const { data } = await http.get<Result<RequirementResponse>>(`/api/requirements/${id}`)
  return data
}

export async function updateRequirement(id: number, req: RequirementUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/requirements/${id}`, req)
  return data
}

export async function deleteRequirement(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/requirements/${id}`)
  return data
}

export async function getNextRequirementNo() {
  const { data } = await http.get<Result<string>>('/api/requirements/next-no')
  return data
}


