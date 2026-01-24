import { http, type Result } from './http'

export interface Tag {
  id: number
  tagName: string
  tagKey: string
  domain: string
  tagType: number // 0-文本, 1-数值, 2-选项, 3-范围
  unit?: string
  options?: string // JSON string
  recommendCategories?: string // JSON string
  isHot: number
  status: number
  sort: number
}

export interface TagValue {
  tagId: number
  tagName: string
  tagKey: string
  tagType: number
  value: string | number
  unit?: string
}

export function listTags(params: { keyword?: string; domain?: string; isHot?: number }): Promise<Result<Tag[]>> {
  return http.get('/api/tags/list', { params })
}

export function recommendTags(categoryId: number | string): Promise<Result<Tag[]>> {
  return http.get(`/api/tags/recommend/${categoryId}`)
}

export function createTag(tag: Partial<Tag>): Promise<Result<number>> {
  return http.post('/api/tags', tag)
}

