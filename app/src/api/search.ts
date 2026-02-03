import { get } from '../utils/request'

export interface UnifiedSearchResult {
  entityType: 'supply' | 'requirement' | 'post'
  entityId: number
  title: string
  content: string
  domain: string
  tagsJson: string
  userName: string
  companyName: string
  imageUrl?: string
  createTime: string
  extra?: any
}

export interface SearchPageResult {
  records: UnifiedSearchResult[]
  total: number
}

export interface SearchParams {
  keyword?: string
  entityType?: string
  domain?: string
  page?: number
  size?: number
}

export function searchUnified(params: SearchParams) {
  return get<SearchPageResult>('/api/search/unified', params)
}
