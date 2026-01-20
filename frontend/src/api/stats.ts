import { http, type Result } from './http'

export interface StatsResponse {
  userCount: number
  requirementCount: number
  supplyCount: number
  supplierCount: number
  buyerCount: number
}

export async function getPlatformStats() {
  const { data } = await http.get<Result<StatsResponse>>('/api/home/stats')
  return data
}

