import { http } from './http'
import type { Result } from './http'

// ==================== Enums (for backward compatibility) ====================

export type ContractStatus = 'draft' | 'pending' | 'signing' | 'signed' | 'executing' | 'completed' | 'cancelled' | 'disputed'
export type ContractType = 'purchase' | 'supply'

// ==================== Types ====================

export interface ContractResponse {
  id: number
  quoteMessageId?: number
  conversationId?: number
  contractNo: string
  // 买卖双方
  buyerCompanyId?: number
  sellerCompanyId?: number
  buyerCompanyName?: string
  sellerCompanyName?: string
  // 产品信息
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  paramsJson?: string
  totalAmount?: number
  // 交付信息
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  deliveryMode?: string
  termsJson?: string
  // 状态 (0=草稿, 1=待签署, 2=已签署, 3=履约中, 4=已完成, 5=已取消)
  status: number
  // 签署信息
  buyerSignTime?: string
  sellerSignTime?: string
  buyerSigned?: boolean
  sellerSigned?: boolean
  pdfHash?: string
  pdfUrl?: string
  createTime: string
  updateTime: string
}

export interface ContractFromQuoteRequest {
  quoteMessageId: number
  title?: string
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
}

export interface ContractUpdateRequest {
  title?: string
  contractType?: string
  partyA?: string
  partyB?: string
  productName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
  status?: string
}

export interface ContractSignRequest {
  signType: string  // seal, handwrite, typed, seal_handwrite
  sealId?: number
  signatureData?: string  // Base64
  typedName?: string
  signerName?: string
  signerTitle?: string
}

export interface ContractQuery {
  status?: string
  keyword?: string
  orderBy?: string
  order?: string
}

export interface SealResponse {
  id: number
  companyId: number
  sealName: string
  sealType: string
  sealUrl: string
  isGenerated: boolean
  isDefault: boolean
  createTime: string
}

export interface SealCreateRequest {
  sealName: string
  sealType: string
  sealUrl?: string
  generate: boolean
}

export interface MilestoneResponse {
  id: number
  contractId: number
  milestoneType: string
  milestoneName: string
  description?: string
  expectedDate?: string
  actualDate?: string
  operatorUserId?: number
  operatorName?: string
  evidenceUrl?: string
  evidenceJson?: string
  remark?: string
  status: string
  confirmUserId?: number
  confirmUserName?: string
  confirmTime?: string
  sortOrder: number
  createTime: string
}

export interface MilestoneCreateRequest {
  milestoneType: string
  milestoneName: string
  description?: string
  expectedDate?: string
  sortOrder?: number
}

export interface MilestoneSubmitRequest {
  actualDate?: string
  evidenceUrl?: string
  evidenceUrls?: string[]
  remark?: string
}

// ==================== Types for backward compatibility ====================

export interface ContractCreateRequest {
  contractNo: string
  contractType: string
  title: string
  partyA: string
  partyB: string
  productName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
  status?: string
}

// ==================== Contract APIs ====================

export async function createContractFromQuote(req: ContractFromQuoteRequest): Promise<Result<number>> {
  const res = await http.post('/api/contracts/from-quote', req)
  return res.data
}

// Backward compatibility
export async function createContract(req: ContractCreateRequest): Promise<Result<number>> {
  const res = await http.post('/api/contracts', req)
  return res.data
}

export async function getNextContractNo(): Promise<Result<string>> {
  const res = await http.get('/api/contracts/next-no')
  return res.data
}

export async function downloadContractPdf(id: number): Promise<{ blob: Blob; hash: string }> {
  const res = await http.get(`/api/contracts/${id}/pdf`, { responseType: 'blob' })
  const hash = res.headers['x-contract-hash'] || ''
  return { blob: res.data, hash }
}

export async function getContract(id: number): Promise<Result<ContractResponse>> {
  const res = await http.get(`/api/contracts/${id}`)
  return res.data
}

export async function listContracts(query?: ContractQuery): Promise<Result<ContractResponse[]>> {
  const res = await http.get('/api/contracts', { params: query })
  return res.data
}

export async function updateContract(id: number, req: ContractUpdateRequest): Promise<Result<void>> {
  const res = await http.put(`/api/contracts/${id}`, req)
  return res.data
}

export async function deleteContract(id: number): Promise<Result<void>> {
  const res = await http.delete(`/api/contracts/${id}`)
  return res.data
}

export async function sendContractForSigning(id: number): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${id}/send`)
  return res.data
}

export async function signContract(id: number, req: ContractSignRequest): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${id}/sign`, req)
  return res.data
}

export function getContractPdfUrl(id: number): string {
  return `/api/contracts/${id}/pdf`
}

// ==================== Seal APIs ====================

export async function listSeals(): Promise<Result<SealResponse[]>> {
  const res = await http.get('/api/seals')
  return res.data
}

export async function createSeal(req: SealCreateRequest): Promise<Result<number>> {
  const res = await http.post('/api/seals', req)
  return res.data
}

export async function getDefaultSeal(): Promise<Result<SealResponse | null>> {
  const res = await http.get('/api/seals/default')
  return res.data
}

export async function setDefaultSeal(id: number): Promise<Result<void>> {
  const res = await http.put(`/api/seals/${id}/default`)
  return res.data
}

export async function deleteSeal(id: number): Promise<Result<void>> {
  const res = await http.delete(`/api/seals/${id}`)
  return res.data
}

// ==================== Milestone APIs ====================

export async function listMilestones(contractId: number): Promise<Result<MilestoneResponse[]>> {
  const res = await http.get(`/api/contracts/${contractId}/milestones`)
  return res.data
}

export async function createMilestone(contractId: number, req: MilestoneCreateRequest): Promise<Result<number>> {
  const res = await http.post(`/api/contracts/${contractId}/milestones`, req)
  return res.data
}

export async function submitMilestone(contractId: number, milestoneId: number, req: MilestoneSubmitRequest): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${contractId}/milestones/${milestoneId}/submit`, req)
  return res.data
}

export async function confirmMilestone(contractId: number, milestoneId: number): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${contractId}/milestones/${milestoneId}/confirm`)
  return res.data
}

export async function rejectMilestone(contractId: number, milestoneId: number, reason?: string): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${contractId}/milestones/${milestoneId}/reject`, null, { params: { reason } })
  return res.data
}

export async function deleteMilestone(contractId: number, milestoneId: number): Promise<Result<void>> {
  const res = await http.delete(`/api/contracts/${contractId}/milestones/${milestoneId}`)
  return res.data
}
