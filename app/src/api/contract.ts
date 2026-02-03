import { get } from '../utils/request'

export interface ContractResponse {
  id: number
  contractNo: string
  buyerCompanyId?: number
  buyerCompanyName?: string
  buyerContacts?: string
  buyerPhone?: string
  sellerCompanyId?: number
  sellerCompanyName?: string
  sellerContacts?: string
  sellerPhone?: string
  productName?: string
  categoryName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  totalAmount?: number
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  deliveryMode?: string
  remark?: string
  status: number
  buyerSigned?: boolean
  sellerSigned?: boolean
  milestoneTotal?: number
  milestoneCompleted?: number
  createTime: string
  updateTime: string
}

export interface ContractQuery {
  status?: string
  keyword?: string
}

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
}

export function listContracts(query?: ContractQuery) {
  return get<ContractResponse[]>('/api/contracts', query)
}

export function getContract(id: number) {
  return get<ContractResponse>(`/api/contracts/${id}`)
}
