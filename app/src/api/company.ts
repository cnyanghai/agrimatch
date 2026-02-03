import { get } from '../utils/request'

export interface CompanyResponse {
  id: number
  ownerUserId?: number
  companyName: string
  companyType?: string
  licenseNo?: string
  licenseImgUrl?: string
  legalPerson?: string
  businessScope?: string
  registeredCapital?: string
  establishDate?: string
  scale?: string
  companyIntro?: string
  wechat?: string
  province?: string
  city?: string
  district?: string
  address?: string
  contacts?: string
  phone?: string
  createTime?: string
}

export interface CompanyProfileResponse {
  company: CompanyResponse
  supplies: any[]
  requirements: any[]
}

export const companyTypeMap: Record<string, string> = {
  feed_factory: '饲料厂',
  trader: '贸易商',
  grain_depot: '粮库',
  processor: '加工厂',
  logistics: '物流',
  other: '其他',
}

export function getCompanyProfile(id: number) {
  return get<CompanyProfileResponse>(`/api/companies/${id}/profile`)
}

export function getMyCompany() {
  return get<CompanyResponse | null>('/api/companies/my')
}

export function getCompanyByUserId(userId: number) {
  return get<CompanyResponse | null>(`/api/companies/by-user/${userId}`)
}

/* ===== 企业名录 ===== */

export interface CompanyCardResponse {
  id: number
  companyName: string
  province?: string
  city?: string
  logo?: string
  count?: number
  categoryNamesStr?: string
  categoryNames?: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 企业名录分页 */
export function getCompanyDirectory(type: string, letter?: string, page = 1, size = 20) {
  return get<PageResult<CompanyCardResponse>>('/api/companies/directory', {
    type,
    letter: letter || undefined,
    page,
    size,
  })
}

/** 获取头部企业 */
export function getTopCompanies(type: string, limit?: number) {
  return get<CompanyCardResponse[]>('/api/companies/top', { type, limit })
}

/** 搜索企业 */
export function searchCompanies(keyword: string, limit?: number) {
  return get<CompanyCardResponse[]>('/api/companies/search', { keyword, limit })
}
