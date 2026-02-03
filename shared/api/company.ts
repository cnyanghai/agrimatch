/**
 * Company module - Types and API path constants.
 */

// ==================== Types ====================

export type CompanyType = 'feed_factory' | 'trader' | 'grain_depot' | 'processor' | 'logistics' | 'other'

export interface CompanyCreateRequest {
  companyName: string
  companyType?: CompanyType
  licenseNo?: string
  licenseImgUrl?: string
  legalPerson?: string
  businessScope?: string
  registeredCapital?: string
  establishDate?: string
  scale?: string
  companyIntro?: string
  announcementsJson?: string
  recruitmentJson?: string
  certificatesJson?: string
  wechat?: string
  province?: string
  city?: string
  district?: string
  address?: string
  lat?: number
  lng?: number
  locationsJson?: string
  bankInfoJson?: string
  contacts?: string
  phone?: string
}

export interface CompanyUpdateRequest extends Partial<CompanyCreateRequest> {
  licenseImgUrl?: string
  certificatesJson?: string
}

export interface CompanyResponse {
  id: number
  ownerUserId?: number
  companyName: string
  companyType?: CompanyType
  licenseNo?: string
  licenseImgUrl?: string
  legalPerson?: string
  businessScope?: string
  registeredCapital?: string
  establishDate?: string
  scale?: string
  companyIntro?: string
  announcementsJson?: string
  recruitmentJson?: string
  certificatesJson?: string
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
  contacts?: string
  phone?: string
}

export interface CompanyBriefResponse {
  id: number
  companyName: string
  companyType?: CompanyType
  address?: string
}

export interface CompanyCardResponse {
  id: number
  companyName: string
  province?: string
  city?: string
  count: number
  categoryNames?: string[]
  logo?: string
}

export interface CompanyProfileResponse {
  company: CompanyResponse
  supplies: any[]
  requirements: any[]
}

// ==================== API Path Constants ====================

export const COMPANY_API = {
  /** GET - Get my company */
  MY: '/api/companies/my',
  /** GET - Get company by user ID: /api/companies/by-user/:userId */
  BY_USER: (userId: number) => `/api/companies/by-user/${userId}`,
  /** POST - Create company */
  CREATE: '/api/companies',
  /** PUT - Update company: /api/companies/:id */
  UPDATE: (id: number | string) => `/api/companies/${id}`,
  /** POST - Upload company license: /api/companies/:id/license */
  UPLOAD_LICENSE: (id: number) => `/api/companies/${id}/license`,
  /** POST - Geocode company: /api/companies/:id/geocode */
  GEOCODE: (id: number) => `/api/companies/${id}/geocode`,
  /** GET - Search companies */
  SEARCH: '/api/companies/search',
  /** GET - List top suppliers */
  SUPPLIERS: '/api/companies/suppliers',
  /** GET - List top buyers */
  BUYERS: '/api/companies/buyers',
  /** GET - List top companies */
  TOP: '/api/companies/top',
  /** GET - Get company directory */
  DIRECTORY: '/api/companies/directory',
  /** GET - Get company profile: /api/companies/:id/profile */
  PROFILE: (id: number | string) => `/api/companies/${id}/profile`,
} as const
