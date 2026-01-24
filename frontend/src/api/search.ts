import { http, type Result, type PageResult } from './http'

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

export function searchUnified(params: {
  keyword?: string
  domain?: string
  entityType?: string
  tagFiltersJson?: string
  page?: number
  size?: number
}): Promise<Result<PageResult<UnifiedSearchResult>>> {
  return http.get('/api/search/unified', { params })
}

