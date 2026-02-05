import { get } from '../utils/request'

export interface CategoryNode {
  id: number
  parentId: number
  name: string
  hasParams?: boolean
  allowCustomName?: boolean
  children?: CategoryNode[]
}

export interface ProductSchemaVO {
  id: number
  schemaCode: string
  schemaName: string
  description?: string
  icon?: string
  sort?: number
  categories: CategoryNode[]
}

/** 获取所有 Schema 及其分类树 */
export function getSchemaTree() {
  return get<ProductSchemaVO[]>('/api/product-schemas/tree')
}

/** 获取单个 Schema 及其分类树 */
export function getSchemaByCode(schemaCode: string) {
  return get<ProductSchemaVO>(`/api/product-schemas/${schemaCode}`)
}
