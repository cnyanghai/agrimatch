import { get, post, put, del } from '../utils/request'

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
}

export function updateRequirement(id: number, req: RequirementUpdateRequest) {
  return put<void>(`/api/requirements/${id}`, req)
}

export function deleteRequirement(id: number) {
  return del<void>(`/api/requirements/${id}`)
}

/** 获取下一个采购编号 */
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
