import { http, type Result } from './http'

export interface ProductNode {
  id: number
  parentId: number
  name: string
  children?: ProductNode[]
}

function pruneByName(list: ProductNode[] | undefined, forbiddenName: string): ProductNode[] {
  if (!list?.length) return []
  const out: ProductNode[] = []
  for (const n of list) {
    // 统一过滤：删除“香肠”相关品类（按名称包含判断，避免不同命名如：香肠制品）
    if ((n.name ?? '').includes(forbiddenName)) continue
    const children = pruneByName(n.children, forbiddenName)
    out.push({ ...n, children: children.length ? children : undefined })
  }
  return out
}

export interface ProductCreateRequest {
  parentId?: number
  name: string
}

export async function getProductTree() {
  const { data } = await http.get<Result<ProductNode[]>>('/api/products/tree')
  if (data.code === 0 && data.data) {
    data.data = pruneByName(data.data, '香肠')
  }
  return data
}

export async function searchProducts(keyword: string) {
  const { data } = await http.get<Result<ProductNode[]>>('/api/products/search', { params: { keyword } })
  if (data.code === 0 && data.data) {
    data.data = pruneByName(data.data, '香肠')
  }
  return data
}

export async function createCustomProduct(req: ProductCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/products/custom', req)
  return data
}

export interface ProductParamResponse {
  id: number
  productId: number
  paramName: string
  paramType: number    // 0输入 1下拉
  paramGroup?: string  // 参数分组：quality/biology/logistics/trade
  unit?: string        // 单位
  placeholder?: string // 输入提示
  required: boolean
  sort?: number
  options?: string[]
}

export async function getProductParams(productId: number) {
  const { data } = await http.get<Result<ProductParamResponse[]>>(`/api/products/${productId}/params`)
  return data
}

export async function addProductParamOption(paramId: number, option: string) {
  const { data } = await http.post<Result<string[]>>(`/api/products/params/${paramId}/options`, { option })
  return data
}


