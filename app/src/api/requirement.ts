import { get, post } from '../utils/request'

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
  paymentMethod?: string
  deliveryMethod?: string
  purchaseAddress?: string
  remark?: string
  status?: number
  expireTime?: string
  distanceKm?: number
  createTime?: string
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

export function listRequirements(params?: RequirementListParams) {
  return get<RequirementResponse[]>('/api/requirements', params)
}

export function getRequirement(id: number) {
  return get<RequirementResponse>(`/api/requirements/${id}`)
}

export interface RequirementCreateRequest {
  categoryName: string
  quantity?: number
  expectedPrice?: number
  packaging?: string
  paymentMethod?: string
  deliveryMethod?: string
  purchaseAddress?: string
  remark?: string
}

export function createRequirement(req: RequirementCreateRequest) {
  return post<number>('/api/requirements', req)
}
