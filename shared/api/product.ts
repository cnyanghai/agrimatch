/**
 * Product module - Types and API path constants.
 */

// ==================== Types ====================

export interface ProductNode {
  id: number
  parentId: number
  name: string
  children?: ProductNode[]
}

export interface ProductCreateRequest {
  parentId?: number
  name: string
}

export interface ProductParamResponse {
  id: number
  productId: number
  paramName: string
  paramType: number     // 0输入 1下拉
  paramGroup?: string   // 参数分组：quality/biology/logistics/trade
  unit?: string
  placeholder?: string
  required: boolean
  sort?: number
  options?: string[]
}

// ==================== Tag Types ====================

export interface Tag {
  id: number
  tagName: string
  tagKey: string
  domain: string
  tagType: number   // 0-文本, 1-数值, 2-选项, 3-范围
  unit?: string
  options?: string  // JSON string
  recommendCategories?: string  // JSON string
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

// ==================== Search Types ====================

export type SearchEntityType = 'supply' | 'requirement' | 'post'

export interface UnifiedSearchResult {
  entityType: SearchEntityType
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

export interface UnifiedSearchParams {
  keyword?: string
  domain?: string
  entityType?: string
  tagFiltersJson?: string
  page?: number
  size?: number
}

// ==================== API Path Constants ====================

export const PRODUCT_API = {
  /** GET - Get product tree */
  TREE: '/api/products/tree',
  /** GET - Search products */
  SEARCH: '/api/products/search',
  /** POST - Create custom product */
  CREATE_CUSTOM: '/api/products/custom',
  /** GET - Get product params: /api/products/:productId/params */
  PARAMS: (productId: number) => `/api/products/${productId}/params`,
  /** POST - Add product param option: /api/products/params/:paramId/options */
  ADD_PARAM_OPTION: (paramId: number) => `/api/products/params/${paramId}/options`,
} as const

export const TAG_API = {
  /** GET - List tags */
  LIST: '/api/tags/list',
  /** GET - Recommend tags: /api/tags/recommend/:categoryId */
  RECOMMEND: (categoryId: number | string) => `/api/tags/recommend/${categoryId}`,
  /** POST - Create tag */
  CREATE: '/api/tags',
} as const

export const SEARCH_API = {
  /** GET - Unified search */
  UNIFIED: '/api/search/unified',
} as const
