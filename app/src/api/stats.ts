import { get } from '../utils/request'

export interface StatsResponse {
  userCount: number
  requirementCount: number
  supplyCount: number
  supplierCount: number
  buyerCount: number
  dealCount?: number
}

export function getPlatformStats() {
  return get<StatsResponse>('/api/home/stats')
}
