import { http, type Result } from './http'

export type ContractStatus = 'draft' | 'pending' | 'signed' | 'executing' | 'completed' | 'cancelled'
export type ContractType = 'purchase' | 'supply'

export interface ContractResponse {
  id: number
  companyId: number
  userId: number
  contractNo: string
  contractType: ContractType
  title: string
  partyA: string
  partyB: string
  productName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  totalAmount?: number
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
  status: ContractStatus
  signTime?: string
  pdfHash?: string
  createTime?: string
  updateTime?: string
}

export interface ContractCreateRequest {
  contractNo: string
  contractType: ContractType
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
  status: ContractStatus
}

export interface ContractUpdateRequest {
  title?: string
  contractType?: ContractType
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
  status?: ContractStatus
}

export async function getNextContractNo() {
  const { data } = await http.get<Result<string>>('/api/contracts/next-no')
  return data
}

export async function listContracts(params: {
  companyId?: number
  userId?: number
  keyword?: string
  status?: ContractStatus | 'all'
  orderBy?: 'create_time' | 'update_time'
  order?: 'asc' | 'desc'
}) {
  const { data } = await http.get<Result<ContractResponse[]>>('/api/contracts', { params })
  return data
}

export async function createContract(req: ContractCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/contracts', req)
  return data
}

export async function updateContract(id: number, req: ContractUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/contracts/${id}`, req)
  return data
}

export async function deleteContract(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/contracts/${id}`)
  return data
}

export async function downloadContractPdf(id: number) {
  const res = await http.get(`/api/contracts/${id}/pdf`, {
    responseType: 'blob',
    // 需要读存证 hash
    validateStatus: (s) => (s >= 200 && s < 300) || s === 401
  })
  return {
    blob: res.data as Blob,
    hash: (res.headers?.['x-contract-hash'] as string | undefined) ?? undefined
  }
}


