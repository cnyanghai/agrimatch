import { http, type Result } from './http'

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

export interface SupplyCreateRequest {
  categoryName: string
  productId?: number
  supplyNo?: string
  origin?: string
  quantity?: number
  /**
   * 报价类型：0=现货一口价（默认），1=基差报价
   */
  priceType?: number
  /**
   * 出厂价（现货模式必填，基差模式可为0）
   */
  exFactoryPrice?: number
  /**
   * 基差报价明细（priceType=1 时必填）
   */
  basisQuotes?: BasisQuoteRequest[]
  shipAddress?: string
  deliveryMode?: string
  packaging?: string
  storageMethod?: string
  priceRulesJson?: string
  paramsJson?: string
  tagsJson?: string
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
  schemaCode?: string  // 业态代码：feed, poultry, meat, other
  supplyNo?: string
  origin?: string
  quantity?: number
  remainingQuantity?: number
  /**
   * 报价类型：0=现货一口价，1=基差报价
   */
  priceType: number
  exFactoryPrice: number
  /**
   * 基差报价明细（priceType=1 时有值）
   */
  basisQuotes?: BasisQuoteResponse[]
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

export async function createSupplyTemplate(req: SupplyTemplateCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/supply-templates', req)
  return data
}

export async function getMySupplyTemplates() {
  const { data } = await http.get<Result<SupplyTemplateResponse[]>>('/api/supply-templates')
  return data
}

export async function deleteSupplyTemplate(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/supply-templates/${id}`)
  return data
}

