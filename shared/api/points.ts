/**
 * Points module - Types and API path constants.
 */

// ==================== Types ====================

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
  status: number    // 0待发卡 1已发卡 2已失败
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

// ==================== API Path Constants ====================

export const POINTS_API = {
  /** GET - Get my points balance */
  ME: '/api/points/me',
  /** POST - Recharge points */
  RECHARGE: '/api/points/recharge',
  /** POST - Redeem points */
  REDEEM: '/api/points/redeem',
  /** GET - List points transactions */
  TX: '/api/points/tx',
  /** POST - Gift points to another user */
  GIFT: '/api/points/gift',
  /** POST - Redeem JD card */
  REDEEM_JD: '/api/points/redeem/jd',
  /** GET - My JD redeems */
  MY_JD_REDEEMS: '/api/points/jd-redeems/mine',
  /** GET - Admin: list JD redeems */
  ADMIN_JD_REDEEMS: '/api/admin/jd-redeems',
  /** POST - Admin: fulfill JD redeem: /api/admin/jd-redeems/:id/fulfill */
  ADMIN_FULFILL_JD: (id: number) => `/api/admin/jd-redeems/${id}/fulfill`,
  /** POST - Admin: fail JD redeem: /api/admin/jd-redeems/:id/fail */
  ADMIN_FAIL_JD: (id: number) => `/api/admin/jd-redeems/${id}/fail`,
} as const
