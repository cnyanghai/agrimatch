import { http, type Result } from './http'

/**
 * 期货合约响应
 */
export interface FuturesContractResponse {
  id: number
  exchangeCode: string
  exchangeName: string
  productCode: string
  productName: string
  contractCode: string
  contractName: string
  deliveryMonth: string
  lastPrice: number | null
  prevClose: number | null
  changePrice: number | null
  changePercent: number | null
  openPrice: number | null
  highPrice: number | null
  lowPrice: number | null
  volume: number | null
  priceUpdateTime: string | null
  daysToDelivery: number | null
  isTrading: boolean | null
}

/**
 * 产品品种
 */
export interface FuturesProduct {
  productCode: string
  productName: string
  exchangeCode: string
  exchangeName: string
}

/**
 * 获取活跃合约列表
 */
export async function listFuturesContracts(productCode?: string) {
  const { data } = await http.get<Result<FuturesContractResponse[]>>('/api/futures/contracts', {
    params: productCode ? { productCode } : undefined
  })
  return data
}

/**
 * 获取单个合约详情
 */
export async function getFuturesContract(contractCode: string) {
  const { data } = await http.get<Result<FuturesContractResponse>>(`/api/futures/contracts/${contractCode}`)
  return data
}

/**
 * 批量获取合约价格
 */
export async function batchGetFuturesPrices(contractCodes: string[]) {
  const { data } = await http.post<Result<Record<string, FuturesContractResponse>>>('/api/futures/prices/batch', contractCodes)
  return data
}

/**
 * 获取产品品种列表
 */
export async function listFuturesProducts() {
  const { data } = await http.get<Result<FuturesProduct[]>>('/api/futures/products')
  return data
}

/**
 * 手动触发价格同步
 */
export async function syncFuturesPrices() {
  const { data } = await http.post<Result<void>>('/api/futures/sync')
  return data
}
