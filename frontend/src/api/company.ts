import { http, type Result } from './http'

export type CompanyType = 'feed_factory' | 'trader' | 'grain_depot' | 'processor' | 'logistics' | 'other'

export interface CompanyCreateRequest {
  companyName: string
  companyType?: CompanyType
  licenseNo?: string
  licenseImgUrl?: string
  contacts?: string
  phone?: string
  wechat?: string
  province?: string
  city?: string
  district?: string
  address?: string
  lat?: number
  lng?: number
  locationsJson?: string
  bankInfoJson?: string
}

export interface CompanyUpdateRequest extends Partial<CompanyCreateRequest> {
  licenseImgUrl?: string
}

export interface CompanyResponse {
  id: number
  ownerUserId?: number
  companyName: string
  companyType?: CompanyType
  licenseNo?: string
  licenseImgUrl?: string
  contacts?: string
  phone?: string
  wechat?: string
  province?: string
  city?: string
  district?: string
  address?: string
  lat?: number
  lng?: number
  locationsJson?: string
  bankInfoJson?: string
  createTime?: string
  updateTime?: string
}

export interface CompanyBriefResponse {
  id: number
  companyName: string
  companyType?: CompanyType
  address?: string
}

export async function getMyCompany() {
  const { data } = await http.get<Result<CompanyResponse | null>>('/api/companies/my')
  return data
}

export async function createCompany(req: CompanyCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/companies', req)
  return data
}

export async function updateCompany(id: number | string, req: CompanyUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/companies/${id}`, req)
  return data
}

export async function uploadCompanyLicense(id: number, file: File) {
  const fd = new FormData()
  fd.append('file', file)
  const { data } = await http.post<Result<string>>(`/api/companies/${id}/license`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return data
}

export async function geocodeCompany(id: number) {
  const { data } = await http.post<Result<CompanyResponse>>(`/api/companies/${id}/geocode`)
  return data
}

export async function searchCompanies(keyword: string, limit = 20) {
  const { data } = await http.get<Result<CompanyBriefResponse[]>>('/api/companies/search', {
    params: { keyword, limit }
  })
  return data
}


