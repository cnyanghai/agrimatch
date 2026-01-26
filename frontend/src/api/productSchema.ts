import { http, type Result } from './http'

/**
 * 产品业态
 */
export interface ProductSchema {
  id: number
  schemaCode: string   // feed, poultry, meat, other
  schemaName: string   // 饲料原料, 禽蛋种苗, 畜禽肉类, 其他品类
  description?: string
  icon?: string
  sort?: number
}

/**
 * 品类节点（带业态信息）
 */
export interface CategoryNode {
  id: number
  parentId: number
  name: string
  hasParams?: boolean        // 是否有预设参数
  allowCustomName?: boolean  // 是否允许自定义名称
  children?: CategoryNode[]
}

/**
 * 业态及其分类树
 */
export interface ProductSchemaVO {
  id: number
  schemaCode: string
  schemaName: string
  description?: string
  icon?: string
  sort?: number
  categories: CategoryNode[]
}

/**
 * 获取所有业态列表（不含分类树）
 */
export async function getProductSchemas() {
  const { data } = await http.get<Result<ProductSchema[]>>('/api/product-schemas')
  return data
}

/**
 * 获取业态及其分类树（供发布页面和供求大厅使用）
 */
export async function getSchemaTree() {
  const { data } = await http.get<Result<ProductSchemaVO[]>>('/api/product-schemas/tree')
  return data
}

/**
 * 根据业态代码获取分类树
 */
export async function getSchemaByCode(schemaCode: string) {
  const { data } = await http.get<Result<ProductSchemaVO>>(`/api/product-schemas/${schemaCode}`)
  return data
}
