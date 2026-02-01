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

// ================= 京东卡兑换 =================

export interface JdRedeemResponse {
  redeemId: number
  cardCode?: string
  faceValue: number
  pointsCost: number
  newPointsBalance: number
}

export interface JdRedeemDetailResponse {
  id: number
  pointsCost: number
  faceValue: number
  cardCode?: string
  status: number // 0待发卡 1已发卡 2已失败
  adminRemark?: string
  createTime?: string
  fulfillTime?: string
}

export interface AdminJdRedeemResponse {
  id: number
  userId: number
  userName: string
  nickName?: string
  pointsCost: number
  faceValue: number
  cardCode?: string
  status: number
  adminUserId?: number
  adminRemark?: string
  createTime?: string
  fulfillTime?: string
}

// 用户：兑换京东卡
export async function redeemJdCard(faceValue: number) {
  const { data } = await http.post<Result<JdRedeemResponse>>('/api/points/redeem/jd', { faceValue })
  return data
}

// 用户：我的兑换记录
export async function listMyJdRedeems() {
  const { data } = await http.get<Result<JdRedeemDetailResponse[]>>('/api/points/jd-redeems/mine')
  return data
}

// 管理端：查列表
export async function listAdminJdRedeems(status?: number) {
  const params = status != null ? { status } : {}
  const { data } = await http.get<Result<AdminJdRedeemResponse[]>>('/api/admin/jd-redeems', { params })
  return data
}

// 管理端：发卡
export async function fulfillJdRedeem(id: number, cardCode: string) {
  const { data } = await http.post<Result<void>>(`/api/admin/jd-redeems/${id}/fulfill`, { cardCode })
  return data
}

// 管理端：拒绝
export async function failJdRedeem(id: number, remark?: string) {
  const { data } = await http.post<Result<void>>(`/api/admin/jd-redeems/${id}/fail`, { remark })
  return data
}


