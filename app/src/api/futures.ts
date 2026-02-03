import { get, post } from '../utils/request'

export interface FuturesContractResponse {
  id: number
  exchangeCode: string
  exchangeName: string
  productCode: string
  productName: string
  contractCode: string
  contractName: string
  deliveryMonth?: string
  lastPrice?: number
  prevClose?: number
  changePrice?: number
  changePercent?: number
  openPrice?: number
  highPrice?: number
  lowPrice?: number
  volume?: number
  priceUpdateTime?: string
  daysToDelivery?: number
  isTrading?: boolean
}

export interface FuturesProduct {
  code: string
  name: string
}

/** 获取期货合约列表（可按品种过滤） */
export function listFuturesContracts(productCode?: string) {
  return get<FuturesContractResponse[]>('/api/futures/contracts', productCode ? { productCode } : undefined)
}

/** 获取单个合约详情 */
export function getFuturesContract(contractCode: string) {
  return get<FuturesContractResponse>(`/api/futures/contracts/${contractCode}`)
}

/** 批量获取价格 */
export function batchGetPrices(contractCodes: string[]) {
  return post<Record<string, FuturesContractResponse>>('/api/futures/prices/batch', contractCodes)
}

/** 获取可选品种列表 */
export function listFuturesProducts() {
  return get<FuturesProduct[]>('/api/futures/products')
}
