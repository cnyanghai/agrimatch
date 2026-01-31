import { http, type Result } from './http'

export interface MapCompanyMarkerResponse {
  companyId: number
  companyName: string
  address?: string
  lat?: number
  lng?: number
  ownerUserId?: number
  companyType?: string
  supplyCount: number
  requirementCount: number
  supplyCategories: string[]
  requirementCategories: string[]
}

export interface MapCompanyQuery {
  keyword?: string
  province?: string
  city?: string
  companyType?: string
}

export async function listMapCompanies(params?: MapCompanyQuery) {
  const { data } = await http.get<Result<MapCompanyMarkerResponse[]>>('/api/map/companies', {
    params: {
      keyword: params?.keyword || undefined,
      province: params?.province || undefined,
      city: params?.city || undefined,
      companyType: params?.companyType || undefined,
    }
  })
  return data
}
