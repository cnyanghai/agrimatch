import { get, post } from '../utils/request'

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

export interface JdRedeemDetailResponse {
  id: number
  pointsCost: number
  faceValue: number
  cardCode?: string
  status: number
  adminRemark?: string
  createTime?: string
  fulfillTime?: string
}

export function getPointsMe() {
  return get<PointsMeResponse>('/api/points/me')
}

export function rechargePoints(points: number) {
  return post<PointsMeResponse>('/api/points/recharge', { points })
}

export function redeemPoints(points: number) {
  return post<PointsMeResponse>('/api/points/redeem', { points })
}

export function listPointsTx() {
  return get<PointsTxResponse[]>('/api/points/tx')
}

export function giftPoints(toUserId: number, points: number, remark?: string) {
  return post<PointsMeResponse>('/api/points/gift', { toUserId, points, remark })
}

export function redeemJdCard(faceValue: number) {
  return post<any>('/api/points/redeem/jd', { faceValue })
}

export function listMyJdRedeems() {
  return get<JdRedeemDetailResponse[]>('/api/points/jd-redeems/mine')
}
