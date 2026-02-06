import { get, post } from '../utils/request'

export interface ProductNode {
  id: number
  name: string
  parentId?: number
  level?: number
  children?: ProductNode[]
}

export interface ProductParam {
  id: number
  productId: number
  paramName: string
  paramType?: string
  options?: string[]
  required?: boolean
  unit?: string
}

/** Get product category tree */
export function getProductTree() {
  return get<ProductNode[]>('/api/products/tree')
}

/** Search products */
export function searchProducts(keyword: string) {
  return get<ProductNode[]>('/api/products/search', { keyword })
}

/** Create custom product */
export function createCustomProduct(data: { name: string; parentId?: number }) {
  return post<number>('/api/products/custom', data)
}

/** Get product parameters */
export function getProductParams(productId: number) {
  return get<ProductParam[]>(`/api/products/${productId}/params`)
}

/** Add option to a parameter */
export function addParamOption(paramId: number, option: string) {
  return post<void>(`/api/products/params/${paramId}/options`, { option })
}
