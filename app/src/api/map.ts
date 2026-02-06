import { get } from '../utils/request'

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

/** List companies for map display */
export function listMapCompanies(params?: MapCompanyQuery) {
  return get<MapCompanyMarkerResponse[]>('/api/map/companies', params)
}
