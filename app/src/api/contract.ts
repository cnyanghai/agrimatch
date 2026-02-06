import { get, post, put, del } from '../utils/request'

// ==================== SMS for Contract Signing ====================

/**
 * 发送合同签署短信验证码
 * type=4 对应后端 SmsCodeService 中的合同签署场景
 */
export function sendSignSmsCode(phone: string) {
  return post<void>('/api/auth/sms/send', { phone, type: 4 })
}

// ==================== Types ====================

export interface ProductParam {
  label: string
  value: string
}

export interface BankInfo {
  bankName?: string
  accountName?: string
  accountNo?: string
}

export interface ContractResponse {
  id: number
  contractNo: string

  // 买方基本信息
  buyerCompanyId?: number
  buyerCompanyName?: string
  // 买方详细信息
  buyerLicenseNo?: string
  buyerContacts?: string
  buyerPhone?: string
  buyerAddress?: string
  buyerBankInfo?: string  // JSON string

  // 卖方基本信息
  sellerCompanyId?: number
  sellerCompanyName?: string
  // 卖方详细信息
  sellerLicenseNo?: string
  sellerContacts?: string
  sellerPhone?: string
  sellerAddress?: string
  sellerBankInfo?: string  // JSON string

  // 产品信息
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  totalAmount?: number
  paramsJson?: string
  productParams?: ProductParam[]

  // 交付信息
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  deliveryMode?: string
  invoiceType?: string
  packaging?: string
  remark?: string
  termsJson?: string

  // 状态 (0=草稿, 1=待签署, 2=已签署, 3=履约中, 4=已完成, 5=已取消)
  status: number

  // 签署信息
  buyerSignTime?: string
  sellerSignTime?: string
  buyerSigned?: boolean
  sellerSigned?: boolean
  buyerSealUrl?: string
  sellerSealUrl?: string
  pdfHash?: string
  pdfUrl?: string

  // 履约进度
  milestoneTotal?: number
  milestoneCompleted?: number

  createTime: string
  updateTime: string
}

export interface ContractQuery {
  status?: string
  keyword?: string
}

// ==================== 数据字典映射 ====================

export const contractStatusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: '#999' },
  1: { label: '待签署', color: '#e6a23c' },
  2: { label: '已签署', color: '#2D6A4F' },
  3: { label: '履约中', color: '#2563eb' },
  4: { label: '已完成', color: '#1a4532' },
  5: { label: '已取消', color: '#f56c6c' },
}

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

/** 获取付款方式文本 */
export function getPaymentMethodText(code?: string): string {
  if (!code) return '-'
  return paymentMethodMap[code] || code
}

/** 获取交付方式文本 */
export function getDeliveryModeText(code?: string): string {
  if (!code) return '-'
  return deliveryModeMap[code] || code
}

/** 解析银行信息 JSON */
export function parseBankInfo(json?: string): BankInfo | null {
  if (!json) return null
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

// ==================== 请求类型 ====================

export interface ContractCreateRequest {
  title?: string
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  deliveryMode?: string
  remark?: string
}

export interface ContractFromNegotiationRequest {
  conversationId: number
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  basisPrice?: number
  contractCode?: string
  priceType?: string
  deliveryDate?: string
  deliveryAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  invoiceType?: string
  packaging?: string
  remark?: string
  paramsJson?: string
}

export interface ContractSignRequest {
  signType: string  // 'seal' | 'handwrite' | 'typed' | 'seal_handwrite'
  sealId?: number
  signatureData?: string  // Base64 (handwrite/seal_handwrite)
  typedName?: string
  signerName?: string
  signerTitle?: string
  smsCode?: string
}

// ==================== Seal Types ====================

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

// ==================== Contract APIs ====================

export function listContracts(query?: ContractQuery) {
  return get<ContractResponse[]>('/api/contracts', query)
}

export function getContract(id: number) {
  return get<ContractResponse>(`/api/contracts/${id}`)
}

export function getNextContractNo() {
  return get<string>('/api/contracts/next-no')
}

export function updateContract(id: number, req: Partial<ContractCreateRequest>) {
  return put<void>(`/api/contracts/${id}`, req)
}

export function deleteContract(id: number) {
  return del<void>(`/api/contracts/${id}`)
}

export function sendForSigning(id: number) {
  return post<void>(`/api/contracts/${id}/send`)
}

export function signContract(id: number, req: ContractSignRequest) {
  return post<void>(`/api/contracts/${id}/sign`, req)
}

export function cancelContract(id: number, reason?: string) {
  return post<void>(`/api/contracts/${id}/cancel`, reason ? { reason } : undefined)
}

export function getContractPdfUrl(id: number) {
  return get<string>(`/api/contracts/${id}/pdf-url`)
}

/**
 * 获取合同PDF下载的完整URL路径
 * 后端接口: GET /api/contracts/{id}/pdf -> 返回 application/pdf
 */
export function getContractPdfDownloadPath(id: number): string {
  return `/api/contracts/${id}/pdf`
}

export function createContractFromNegotiation(req: ContractFromNegotiationRequest) {
  return post<number>('/api/contracts/from-negotiation', req)
}

// ==================== Seal APIs ====================

export function listSeals() {
  return get<SealResponse[]>('/api/seals')
}

export function createSeal(req: SealCreateRequest) {
  return post<number>('/api/seals', req)
}

export function getDefaultSeal() {
  return get<SealResponse | null>('/api/seals/default')
}

export function setDefaultSeal(id: number) {
  return put<void>(`/api/seals/${id}/default`)
}

export function deleteSeal(id: number) {
  return del<void>(`/api/seals/${id}`)
}
