/**
 * Contract module - Types and API path constants.
 */

// ==================== Types ====================

export type ContractStatus = 'draft' | 'pending' | 'signing' | 'signed' | 'executing' | 'completed' | 'cancelled' | 'disputed'
export type ContractType = 'purchase' | 'supply'

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
  buyerLicenseNo?: string
  buyerContacts?: string
  buyerPhone?: string
  buyerAddress?: string
  buyerBankInfo?: string

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
  formattedTerms?: string

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

export interface ContractFromQuoteRequest {
  quoteMessageId: number
  title?: string
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
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
  signType: string    // seal, handwrite, typed, seal_handwrite
  sealId?: number
  signatureData?: string  // Base64
  typedName?: string
  signerName?: string
  signerTitle?: string
  smsCode?: string        // 短信验证码（盖章时必填）
}

export interface ContractQuery {
  status?: string
  keyword?: string
  orderBy?: string
  order?: string
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

// ==================== Milestone Types ====================

export interface MilestoneResponse {
  id: number
  contractId: number
  milestoneType: string
  responsibleParty?: string   // buyer/seller
  milestoneName: string
  description?: string
  expectedDate?: string
  actualDate?: string
  operatorUserId?: number
  operatorName?: string
  evidenceUrl?: string
  evidenceJson?: string
  remark?: string
  rejectReason?: string
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
  responsibleParty?: string   // buyer/seller（CUSTOM类型手动指定）
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

// ==================== Bank Info ====================

export interface BankInfo {
  bankName?: string
  accountName?: string
  accountNo?: string
}

// ==================== Contract Stats ====================

export interface ContractStats {
  signedContractCount: number
  partnerCount: number
}

export interface PartnerCompany {
  companyId: number
  companyName: string
  contractCount: number
  totalAmount: number
}

// ==================== API Path Constants ====================

export const CONTRACT_API = {
  /** POST - Create contract from quote */
  FROM_QUOTE: '/api/contracts/from-quote',
  /** POST - Create contract from negotiation */
  FROM_NEGOTIATION: '/api/contracts/from-negotiation',
  /** POST - Create contract (legacy) */
  CREATE: '/api/contracts',
  /** GET - List contracts */
  LIST: '/api/contracts',
  /** GET - Get next contract number */
  NEXT_NO: '/api/contracts/next-no',
  /** GET - Get contract by ID: /api/contracts/:id */
  GET_BY_ID: (id: number) => `/api/contracts/${id}`,
  /** PUT - Update contract: /api/contracts/:id */
  UPDATE: (id: number) => `/api/contracts/${id}`,
  /** DELETE - Delete contract: /api/contracts/:id */
  DELETE: (id: number) => `/api/contracts/${id}`,
  /** GET - Download contract PDF: /api/contracts/:id/pdf */
  PDF: (id: number) => `/api/contracts/${id}/pdf`,
  /** POST - Send contract for signing: /api/contracts/:id/send */
  SEND: (id: number) => `/api/contracts/${id}/send`,
  /** POST - Sign contract: /api/contracts/:id/sign */
  SIGN: (id: number) => `/api/contracts/${id}/sign`,
  /** POST - Cancel contract: /api/contracts/:id/cancel */
  CANCEL: (id: number) => `/api/contracts/${id}/cancel`,
  /** GET - Company contract stats: /api/contracts/stats/company/:companyId */
  STATS: (companyId: number) => `/api/contracts/stats/company/${companyId}`,
  /** GET - Company partners: /api/contracts/partners/company/:companyId */
  PARTNERS: (companyId: number) => `/api/contracts/partners/company/${companyId}`,
} as const

export const SEAL_API = {
  /** GET - List seals */
  LIST: '/api/seals',
  /** POST - Create seal */
  CREATE: '/api/seals',
  /** GET - Get default seal */
  DEFAULT: '/api/seals/default',
  /** PUT - Set default seal: /api/seals/:id/default */
  SET_DEFAULT: (id: number) => `/api/seals/${id}/default`,
  /** DELETE - Delete seal: /api/seals/:id */
  DELETE: (id: number) => `/api/seals/${id}`,
} as const

export const MILESTONE_API = {
  /** GET - List milestones: /api/contracts/:contractId/milestones */
  LIST: (contractId: number) => `/api/contracts/${contractId}/milestones`,
  /** POST - Create milestone: /api/contracts/:contractId/milestones */
  CREATE: (contractId: number) => `/api/contracts/${contractId}/milestones`,
  /** POST - Submit milestone: /api/contracts/:contractId/milestones/:milestoneId/submit */
  SUBMIT: (contractId: number, milestoneId: number) => `/api/contracts/${contractId}/milestones/${milestoneId}/submit`,
  /** POST - Confirm milestone: /api/contracts/:contractId/milestones/:milestoneId/confirm */
  CONFIRM: (contractId: number, milestoneId: number) => `/api/contracts/${contractId}/milestones/${milestoneId}/confirm`,
  /** POST - Reject milestone: /api/contracts/:contractId/milestones/:milestoneId/reject */
  REJECT: (contractId: number, milestoneId: number) => `/api/contracts/${contractId}/milestones/${milestoneId}/reject`,
  /** DELETE - Delete milestone: /api/contracts/:contractId/milestones/:milestoneId */
  DELETE: (contractId: number, milestoneId: number) => `/api/contracts/${contractId}/milestones/${milestoneId}`,
  /** POST - Generate standard milestones: /api/contracts/:contractId/milestones/actions/generate-standard */
  GENERATE_STANDARD: (contractId: number) => `/api/contracts/${contractId}/milestones/actions/generate-standard`,
} as const
