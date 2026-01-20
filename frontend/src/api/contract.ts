import { http } from './http'
import type { Result } from './http'

// ==================== Enums (for backward compatibility) ====================

export type ContractStatus = 'draft' | 'pending' | 'signing' | 'signed' | 'executing' | 'completed' | 'cancelled' | 'disputed'
export type ContractType = 'purchase' | 'supply'

// ==================== Types ====================

export interface ProductParam {
  label: string
  value: string
}

export interface ContractResponse {
  id: number
  quoteMessageId?: number
  conversationId?: number
  contractNo: string
  
  // 买方基本信息
  buyerCompanyId?: number
  buyerCompanyName?: string
  // 买方详细信息
  buyerLicenseNo?: string      // 营业执照号
  buyerContacts?: string       // 联系人
  buyerPhone?: string          // 电话
  buyerAddress?: string        // 详细地址
  buyerBankInfo?: string       // 银行信息 JSON
  
  // 卖方基本信息
  sellerCompanyId?: number
  sellerCompanyName?: string
  // 卖方详细信息
  sellerLicenseNo?: string
  sellerContacts?: string
  sellerPhone?: string
  sellerAddress?: string
  sellerBankInfo?: string
  
  // 产品信息
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  paramsJson?: string
  totalAmount?: number
  // 产品参数（解析后的结构化数据）
  productParams?: ProductParam[]
  
  // 交付信息
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  deliveryMode?: string
  termsJson?: string
  // 格式化合同条款
  formattedTerms?: string
  
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

// ==================== 数据字典映射 ====================

/** 付款方式映射 */
export const paymentMethodMap: Record<string, string> = {
  '01': '款到发货',
  '02': '货到付款',
  '03': '账期30天',
  '04': '账期60天',
  '05': '分期付款',
  '06': '预付定金',
  '款到发货': '款到发货',
  '货到付款': '货到付款',
  '账期30天': '账期30天',
  '账期60天': '账期60天',
  '分期付款': '分期付款',
  '预付定金': '预付定金',
}

/** 获取付款方式文本 */
export function getPaymentMethodText(code?: string): string {
  if (!code) return '-'
  return paymentMethodMap[code] || code
}

/** 交付方式映射 */
export const deliveryModeMap: Record<string, string> = {
  '01': '送货上门',
  '02': '自提',
  '03': '物流配送',
  '04': '快递',
  '送货上门': '送货上门',
  '自提': '自提',
  '物流配送': '物流配送',
  '快递': '快递',
}

/** 获取交付方式文本 */
export function getDeliveryModeText(code?: string): string {
  if (!code) return '-'
  return deliveryModeMap[code] || code
}

/** 合同状态映射 */
export const contractStatusMap: Record<number, { label: string; color: string; bgColor: string }> = {
  0: { label: '草稿', color: 'text-gray-600', bgColor: 'bg-gray-100' },
  1: { label: '待签署', color: 'text-amber-600', bgColor: 'bg-amber-50' },
  2: { label: '已签署', color: 'text-brand-600', bgColor: 'bg-brand-50' },
  3: { label: '履约中', color: 'text-blue-600', bgColor: 'bg-blue-50' },
  4: { label: '已完成', color: 'text-brand-700', bgColor: 'bg-brand-100' },
  5: { label: '已取消', color: 'text-red-500', bgColor: 'bg-red-50' },
}

/** 解析银行信息 JSON */
export interface BankInfo {
  bankName?: string
  accountName?: string
  accountNo?: string
}

export function parseBankInfo(json?: string): BankInfo | null {
  if (!json) return null
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
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
  vehicleInfoJson?: string
  createTime: string
}

export interface MilestoneCreateRequest {
  milestoneType: string
  milestoneName: string
  description?: string
  expectedDate?: string
  sortOrder?: number
  vehicleInfoJson?: string
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

export async function cancelContract(id: number, reason?: string): Promise<Result<void>> {
  const res = await http.post(`/api/contracts/${id}/cancel`, { reason })
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
