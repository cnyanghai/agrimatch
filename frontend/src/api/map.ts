import { http, type Result } from './http'

export interface MapCompanyMarkerResponse {
  companyId: number
  companyName: string
  address?: string
  lat?: number
  lng?: number
  supplyCount: number
  requirementCount: number
  supplyCategories: string[]
  requirementCategories: string[]
}

export async function listMapCompanies(keyword?: string) {
  const { data } = await http.get<Result<MapCompanyMarkerResponse[]>>('/api/map/companies', {
    params: { keyword: keyword || undefined }
  })
  return data
}





