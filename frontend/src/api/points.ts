import { http, type Result } from './http'

export interface PointsMeResponse {
  pointsBalance: number
  cnyBalance: number
}

export interface PointsTxResponse {
  id: number
  txType: string
  pointsDelta: number
  cnyDelta: number
  remark?: string
  createTime?: string
}

export async function getPointsMe() {
  const { data } = await http.get<Result<PointsMeResponse>>('/api/points/me')
  return data
}

export async function rechargePoints(points: number) {
  const { data } = await http.post<Result<PointsMeResponse>>('/api/points/recharge', { points })
  return data
}

export async function redeemPoints(points: number) {
  const { data } = await http.post<Result<PointsMeResponse>>('/api/points/redeem', { points })
  return data
}

export async function listPointsTx() {
  const { data } = await http.get<Result<PointsTxResponse[]>>('/api/points/tx')
  return data
}

// 赠送积分给其他用户
export async function giftPoints(toUserId: number, points: number, remark?: string) {
  const { data } = await http.post<Result<PointsMeResponse>>('/api/points/gift', { 
    toUserId, 
    points, 
    remark 
  })
  return data
}


